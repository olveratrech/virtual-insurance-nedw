package mx.unam.virtualinsurance.policy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class PolicyIntegrationTests {

	private static final String ACADEMIC_EMAIL = "emmanuel.demo@virtual-insurance.test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Test
	void returnsOwnedAutoPolicyWithPaymentAndDocumentMetadata() throws Exception {
		Long policyId = createPolicy("AUTO");

		mockMvc.perform(get("/polizas/{policyId}", policyId)
			.with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(policyId))
			.andExpect(jsonPath("$.status").value("ACTIVA"))
			.andExpect(jsonPath("$.coverageActive").value(true))
			.andExpect(jsonPath("$.type").value("AUTO"))
			.andExpect(jsonPath("$.insured.label").value("Vehiculo asegurado"))
			.andExpect(jsonPath("$.insured.value").value("Mazda 3 - 2026"))
			.andExpect(jsonPath("$.payment.mode").value("MONTHLY"))
			.andExpect(jsonPath("$.payment.lastPaymentAmount").value(1040.00))
			.andExpect(jsonPath("$.payment.authorizationCode").isNotEmpty())
			.andExpect(jsonPath("$.documents.length()").value(2))
			.andExpect(jsonPath("$.documents[0].downloadUrl").isNotEmpty());
	}

	@Test
	void returnsOwnedMedicalPolicyWithClientAsInsured() throws Exception {
		Long policyId = createPolicy("MEDICAL");

		mockMvc.perform(get("/polizas/{policyId}", policyId)
			.with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.type").value("MEDICAL"))
			.andExpect(jsonPath("$.insured.label").value("Persona asegurada"))
			.andExpect(jsonPath("$.insured.value").value("Emmanuel Trejo Olvera"))
			.andExpect(jsonPath("$.productName").value("Seguro medico"));
	}

	@Test
	void hidesPolicyFromAnotherClient() throws Exception {
		Long policyId = createPolicy("AUTO");

		mockMvc.perform(get("/polizas/{policyId}", policyId)
			.with(authenticatedClient(999999999L, "otro@virtual-insurance.test")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
	}

	@Test
	void returnsNotFoundForUnknownPolicy() throws Exception {
		mockMvc.perform(get("/polizas/999999999")
			.with(authenticatedAcademicClient()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
	}

	@Test
	void rejectsPolicyQueryWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/polizas/1"))
			.andExpect(status().isUnauthorized());
	}

	private Long createPolicy(String type) throws Exception {
		Long quoteId = "MEDICAL".equals(type) ? createMedicalQuote() : createAutoQuote();
		MvcResult result = mockMvc.perform(post("/cotizaciones/{quoteId}/intentos-pago", quoteId)
			.with(authenticatedAcademicClient())
			.with(csrf())
			.header("Idempotency-Key", UUID.randomUUID().toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content(paymentJson()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("APROBADO"))
			.andReturn();
		Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.policyId");
		return id.longValue();
	}

	private Long createAutoQuote() throws Exception {
		return createQuote("/cotizaciones/auto", """
			{
			  "brand":"Mazda","model":"3","year":2026,"category":"SEDAN",
			  "plates":"POL123A","licenseNumber":"POL-LIC-1","driverAge":26,
			  "negativePoints":0,"criminalRecord":false
			}
			""");
	}

	private Long createMedicalQuote() throws Exception {
		return createQuote("/cotizaciones/medicas", """
			{
			  "age":35,"hypertension":false,"glucoseOutOfRange":false,
			  "cholesterolOutOfRange":false,"smoker":false,"alcoholAddiction":false
			}
			""");
	}

	private Long createQuote(String path, String body) throws Exception {
		MvcResult result = mockMvc.perform(post(path)
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isCreated())
			.andReturn();
		Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		return id.longValue();
	}

	private String paymentJson() {
		return """
			{
			  "coverageStart":"%s","paymentMode":"MONTHLY",
			  "cardholderName":"Cliente Academico","cardNumber":"4242424242421111",
			  "expiration":"12/30","cvv":"123","acceptsTerms":true
			}
			""".formatted(LocalDate.now().plusDays(1));
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

