package mx.unam.virtualinsurance.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;

import mx.unam.virtualinsurance.auth.ClientPrincipal;
import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties =
	"app.payment.card-encryption-key=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
class PaymentIntegrationTests {

	private static final String ACADEMIC_EMAIL = "emmanuel.demo@virtual-insurance.test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void approvesPaymentDebitsBalanceAndIssuesPolicy() throws Exception {
		Long quoteId = createApprovedAutoQuote();
		var initialBalance = balance("1111");

		MvcResult result = pay(quoteId, "4242424242421111", "123", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("APROBADO"))
			.andExpect(jsonPath("$.responseCode").value("APPROVED"))
			.andExpect(jsonPath("$.authorizationCode").value(org.hamcrest.Matchers.matchesPattern("AUTH-[0-9]{10}")))
			.andExpect(jsonPath("$.lastFour").value("1111"))
			.andExpect(jsonPath("$.amount").value(1040.00))
			.andExpect(jsonPath("$.policyId").isNumber())
			.andReturn();

		Number policyId = JsonPath.read(result.getResponse().getContentAsString(), "$.policyId");
		assertThat(balance("1111")).isEqualByComparingTo(initialBalance.subtract(new java.math.BigDecimal("1040.00")));
		assertThat(count("select count(*) from documento_poliza where poliza_id = ?", policyId.longValue()))
			.isEqualTo(2);
		assertThat(count("select count(*) from historico_estatus_poliza where poliza_id = ?", policyId.longValue()))
			.isEqualTo(1);
	}

	@Test
	void approvesMastercardPayment() throws Exception {
		var initialBalance = balance("5100");

		pay(createApprovedAutoQuote(), "5105105105105100", "123", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("APROBADO"))
			.andExpect(jsonPath("$.lastFour").value("5100"));

		assertThat(balance("5100"))
			.isEqualByComparingTo(initialBalance.subtract(new java.math.BigDecimal("1040.00")));
	}

	@Test
	void approvesAmericanExpressPaymentWithFourDigitSecurityCode() throws Exception {
		var initialBalance = balance("8431");

		pay(createApprovedAutoQuote(), "371449635398431", "1234", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("APROBADO"))
			.andExpect(jsonPath("$.lastFour").value("8431"));

		assertThat(balance("8431"))
			.isEqualByComparingTo(initialBalance.subtract(new java.math.BigDecimal("1040.00")));
	}

	@Test
	void rejectsCardNumberThatDoesNotMatchSelectedBrand() throws Exception {
		mockMvc.perform(post("/cotizaciones/{quoteId}/intentos-pago", createApprovedAutoQuote())
			.with(authenticatedAcademicClient())
			.with(csrf())
			.header("Idempotency-Key", UUID.randomUUID().toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(paymentJson("MASTERCARD", "4242424242421111", "123", "12/30")))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("CARD_BRAND_MISMATCH"));
	}

	@Test
	void rejectsInsufficientFundsWithoutDebitingOrIssuingPolicy() throws Exception {
		Long quoteId = createApprovedAutoQuote();
		var initialBalance = balance("2222");

		MvcResult result = pay(quoteId, "4000000000002222", "123", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RECHAZADO"))
			.andExpect(jsonPath("$.responseCode").value("INSUFFICIENT_FUNDS"))
			.andExpect(jsonPath("$.policyId").value(org.hamcrest.Matchers.nullValue()))
			.andReturn();

		Number attemptId = JsonPath.read(result.getResponse().getContentAsString(), "$.attemptId");
		assertThat(balance("2222")).isEqualByComparingTo(initialBalance);
		assertThat(count("select count(*) from poliza where intento_pago_id = ?", attemptId.longValue()))
			.isZero();
	}

	@Test
	void rejectsBlockedCard() throws Exception {
		pay(createApprovedAutoQuote(), "4000000000003333", "123", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RECHAZADO"))
			.andExpect(jsonPath("$.responseCode").value("BLOCKED_CARD"));
	}

	@Test
	void rejectsProcessorDecline() throws Exception {
		pay(createApprovedAutoQuote(), "4000000000005555", "123", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.responseCode").value("PROCESSOR_DECLINED"));
	}

	@Test
	void rejectsExpiredInstrumentEvenWhenSubmittedExpirationLooksCurrent() throws Exception {
		pay(createApprovedAutoQuote(), "4000000000004444", "123", "01/25", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.responseCode").value("EXPIRED_CARD"));
	}

	@Test
	void acceptsExactBalanceAndLeavesItAtZero() throws Exception {
		jdbcTemplate.update(
			"update instrumento_pago_prueba set saldo_disponible = 1040 where ultimos_cuatro = '6666'"
		);
		pay(createApprovedAutoQuote(), "4242424242426666", "123", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("APROBADO"));

		assertThat(balance("6666")).isEqualByComparingTo("0.00");
	}

	@Test
	void rejectsInvalidSecurityCodeWithoutStoringIt() throws Exception {
		pay(createApprovedAutoQuote(), "4242424242421111", "999", UUID.randomUUID().toString())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.responseCode").value("INVALID_SECURITY_CODE"));

		assertThat(count("select count(*) from user_tab_columns where column_name in ('CVV', 'SECURITY_CODE')"))
			.isZero();
	}

	@Test
	void repeatsIdempotentPaymentWithoutSecondDebitOrPolicy() throws Exception {
		Long quoteId = createApprovedAutoQuote();
		String key = UUID.randomUUID().toString();
		var initialBalance = balance("1111");

		MvcResult first = pay(quoteId, "4242424242421111", "123", key)
			.andExpect(status().isCreated())
			.andReturn();
		MvcResult second = pay(quoteId, "4242424242421111", "123", key)
			.andExpect(status().isCreated())
			.andReturn();

		Number firstAttempt = JsonPath.read(first.getResponse().getContentAsString(), "$.attemptId");
		Number secondAttempt = JsonPath.read(second.getResponse().getContentAsString(), "$.attemptId");
		assertThat(secondAttempt.longValue()).isEqualTo(firstAttempt.longValue());
		assertThat(balance("1111")).isEqualByComparingTo(initialBalance.subtract(new java.math.BigDecimal("1040.00")));
		assertThat(count("select count(*) from poliza where intento_pago_id = ?", firstAttempt.longValue()))
			.isEqualTo(1);
	}

	@Test
	void rejectsPaymentWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/cotizaciones/1/intentos-pago")
			.with(csrf())
			.header("Idempotency-Key", UUID.randomUUID().toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(paymentJson("4242424242421111", "123")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void validatesTermsBeforeCreatingAttempt() throws Exception {
		String body = paymentJson("4242424242421111", "123")
			.replace("\"acceptsTerms\": true", "\"acceptsTerms\": false");
		mockMvc.perform(post("/cotizaciones/{quoteId}/intentos-pago", createApprovedAutoQuote())
			.with(authenticatedAcademicClient())
			.with(csrf())
			.header("Idempotency-Key", UUID.randomUUID().toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	private org.springframework.test.web.servlet.ResultActions pay(
		Long quoteId,
		String cardNumber,
		String cvv,
		String idempotencyKey
	) throws Exception {
		return pay(quoteId, cardNumber, cvv, "12/30", idempotencyKey);
	}

	private org.springframework.test.web.servlet.ResultActions pay(
		Long quoteId,
		String cardNumber,
		String cvv,
		String expiration,
		String idempotencyKey
	) throws Exception {
		return mockMvc.perform(post("/cotizaciones/{quoteId}/intentos-pago", quoteId)
			.with(authenticatedAcademicClient())
			.with(csrf())
			.header("Idempotency-Key", idempotencyKey)
			.contentType(MediaType.APPLICATION_JSON)
			.content(paymentJson(cardNumber, cvv, expiration)));
	}

	private Long createApprovedAutoQuote() throws Exception {
		MvcResult result = mockMvc.perform(post("/cotizaciones/auto")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
				  "brand":"Mazda","model":"3","year":2026,"category":"SEDAN",
				  "plates":"PAY123A","licenseNumber":"PAY-LIC-1","driverAge":26,
				  "negativePoints":0,"criminalRecord":false
				}
				"""))
			.andExpect(status().isCreated())
			.andReturn();
		Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		return id.longValue();
	}

	private String paymentJson(String cardNumber, String cvv) {
		return paymentJson(cardNumber, cvv, "12/30");
	}

	private String paymentJson(String cardNumber, String cvv, String expiration) {
		return paymentJson(cardBrand(cardNumber), cardNumber, cvv, expiration);
	}

	private String paymentJson(
		String cardBrand,
		String cardNumber,
		String cvv,
		String expiration
	) {
		return """
			{
			  "coverageStart":"%s",
			  "paymentMode":"MONTHLY",
			  "cardholderName":"Cliente Academico",
			  "cardBrand":"%s",
			  "cardNumber":"%s",
			  "expiration":"%s",
			  "cvv":"%s",
			  "acceptsTerms": true
			}
			""".formatted(
				LocalDate.now().plusDays(1),
				cardBrand,
				cardNumber,
				expiration,
				cvv
			);
	}

	private String cardBrand(String cardNumber) {
		if (cardNumber.startsWith("3")) return "AMEX";
		if (cardNumber.startsWith("5") || cardNumber.startsWith("2")) return "MASTERCARD";
		return "VISA";
	}

	private RequestPostProcessor authenticatedAcademicClient() {
		Client client = clientRepository.findByEmail(ACADEMIC_EMAIL).orElseThrow();
		ClientPrincipal principal = new ClientPrincipal(client.getClientId(), client.getEmail(), "unused");
		var token = UsernamePasswordAuthenticationToken.authenticated(
			principal, null, principal.getAuthorities()
		);
		return authentication(token);
	}

	private java.math.BigDecimal balance(String lastFour) {
		return jdbcTemplate.queryForObject(
			"select saldo_disponible from instrumento_pago_prueba where ultimos_cuatro = ?",
			java.math.BigDecimal.class,
			lastFour
		);
	}

	private int count(String sql, Object... arguments) {
		Integer value = jdbcTemplate.queryForObject(sql, Integer.class, arguments);
		return value == null ? 0 : value;
	}
}

