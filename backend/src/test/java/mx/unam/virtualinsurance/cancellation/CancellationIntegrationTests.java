package mx.unam.virtualinsurance.cancellation;

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
class CancellationIntegrationTests {

	private static final String ACADEMIC_EMAIL = "emmanuel.demo@virtual-insurance.test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void createsCancellationAndUpdatesPolicyAndHistory() throws Exception {
		Long policyId = createPolicy();
		int historyBefore = historyCount(policyId);

		mockMvc.perform(post("/polizas/{policyId}/solicitudes-cancelacion", policyId)
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(validRequestJson()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.requestNumber").value(org.hamcrest.Matchers.startsWith("CAN-")))
			.andExpect(jsonPath("$.policyId").value(policyId))
			.andExpect(jsonPath("$.status").value("SOLICITADA"))
			.andExpect(jsonPath("$.reason").value("FINANCIAL"))
			.andExpect(jsonPath("$.comments").value("Solicitud academica de prueba"))
			.andExpect(jsonPath("$.coverageActive").value(true))
			.andExpect(jsonPath("$.policyStatus").value("CANCELACION_SOLICITADA"));

		assertThat(policyStatus(policyId)).isEqualTo("CANCELACION_SOLICITADA");
		assertThat(historyCount(policyId)).isEqualTo(historyBefore + 1);
		assertThat(jdbcTemplate.queryForObject(
			"select count(*) from solicitud_cancelacion where poliza_id = ? and motivo = 'FINANCIAL'",
			Integer.class,
			policyId
		)).isEqualTo(1);
	}

	@Test
	void rejectsDuplicatePendingCancellation() throws Exception {
		Long policyId = createPolicy();
		requestCancellation(policyId).andExpect(status().isCreated());

		requestCancellation(policyId)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("CANCELLATION_ALREADY_PENDING"));
	}

	@Test
	void hidesPolicyOwnedByAnotherClient() throws Exception {
		Long policyId = createPolicy();

		mockMvc.perform(post("/polizas/{policyId}/solicitudes-cancelacion", policyId)
			.with(authenticatedClient(999999999L, "otro@virtual-insurance.test"))
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(validRequestJson()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
	}

	@Test
	void returnsNotFoundForUnknownPolicy() throws Exception {
		requestCancellation(999999999L)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
	}

	@Test
	void rejectsPolicyThatIsNotActive() throws Exception {
		Long policyId = createPolicy();
		jdbcTemplate.update(
			"""
			update poliza
			   set estatus_poliza_id = (
			       select estatus_poliza_id from estatus_poliza where clave = 'VENCIDA'
			   )
			 where poliza_id = ?
			""",
			policyId
		);

		requestCancellation(policyId)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("POLICY_NOT_ACTIVE"));
	}

	@Test
	void validatesPastDateInvalidReasonLongCommentsAndConfirmation() throws Exception {
		Long policyId = createPolicy();
		String longComments = "x".repeat(501);
		String invalidJson = """
			{
			  "reason":"UNKNOWN",
			  "requestedDate":"%s",
			  "comments":"%s",
			  "confirmed":false
			}
			""".formatted(LocalDate.now().minusDays(1), longComments);

		mockMvc.perform(post("/polizas/{policyId}/solicitudes-cancelacion", policyId)
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(invalidJson))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors.length()").value(4));
	}

	@Test
	void rejectsFutureRequestedDate() throws Exception {
		Long policyId = createPolicy();
		String futureDateJson = """
			{
			  "reason":"FINANCIAL",
			  "requestedDate":"%s",
			  "comments":null,
			  "confirmed":true
			}
			""".formatted(LocalDate.now().plusDays(1));

		mockMvc.perform(post("/polizas/{policyId}/solicitudes-cancelacion", policyId)
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(futureDateJson))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("requestedDate"));
	}

	@Test
	void rejectsRequestWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/polizas/1/solicitudes-cancelacion")
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(validRequestJson()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsRequestWithoutCsrf() throws Exception {
		Long policyId = createPolicy();

		mockMvc.perform(post("/polizas/{policyId}/solicitudes-cancelacion", policyId)
			.with(authenticatedAcademicClient())
			.contentType(MediaType.APPLICATION_JSON)
			.content(validRequestJson()))
			.andExpect(status().isForbidden());
	}

	private org.springframework.test.web.servlet.ResultActions requestCancellation(Long policyId)
		throws Exception {
		return mockMvc.perform(post("/polizas/{policyId}/solicitudes-cancelacion", policyId)
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(validRequestJson()));
	}

	private Long createPolicy() throws Exception {
		MvcResult quoteResult = mockMvc.perform(post("/cotizaciones/auto")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
				  "brand":"Mazda","model":"3","year":2026,"category":"SEDAN",
				  "plates":"CAN123A","licenseNumber":"CAN-LIC-1","driverAge":26,
				  "negativePoints":0,"criminalRecord":false
				}
				"""))
			.andExpect(status().isCreated())
			.andReturn();
		Number quoteId = JsonPath.read(quoteResult.getResponse().getContentAsString(), "$.id");

		MvcResult paymentResult = mockMvc.perform(post(
			"/cotizaciones/{quoteId}/intentos-pago", quoteId.longValue()
		).with(authenticatedAcademicClient())
			.with(csrf())
			.header("Idempotency-Key", UUID.randomUUID().toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
				  "coverageStart":"%s","paymentMode":"MONTHLY",
				  "cardholderName":"Cliente Academico","cardNumber":"4242424242421111",
				  "expiration":"12/30","cvv":"123","acceptsTerms":true
				}
				""".formatted(LocalDate.now().plusDays(1))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("APROBADO"))
			.andReturn();
		Number policyId = JsonPath.read(paymentResult.getResponse().getContentAsString(), "$.policyId");
		return policyId.longValue();
	}

	private String validRequestJson() {
		return """
			{
			  "reason":"FINANCIAL",
			  "requestedDate":"%s",
			  "comments":"Solicitud academica de prueba",
			  "confirmed":true
			}
			""".formatted(LocalDate.now());
	}

	private String policyStatus(Long policyId) {
		return jdbcTemplate.queryForObject(
			"""
			select ep.clave from poliza p
			join estatus_poliza ep on ep.estatus_poliza_id = p.estatus_poliza_id
			where p.poliza_id = ?
			""",
			String.class,
			policyId
		);
	}

	private int historyCount(Long policyId) {
		return jdbcTemplate.queryForObject(
			"select count(*) from historico_estatus_poliza where poliza_id = ?",
			Integer.class,
			policyId
		);
	}

	private RequestPostProcessor authenticatedAcademicClient() {
		Client client = clientRepository.findByEmail(ACADEMIC_EMAIL).orElseThrow();
		return authenticatedClient(client.getClientId(), client.getEmail());
	}

	private RequestPostProcessor authenticatedClient(Long clientId, String email) {
		ClientPrincipal principal = new ClientPrincipal(clientId, email, "unused");
		var token = UsernamePasswordAuthenticationToken.authenticated(
			principal, null, principal.getAuthorities()
		);
		return authentication(token);
	}
}
