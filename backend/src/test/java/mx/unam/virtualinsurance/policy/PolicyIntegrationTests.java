package mx.unam.virtualinsurance.policy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;

import mx.unam.virtualinsurance.auth.ClientPrincipal;
import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

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
class PolicyIntegrationTests {

	private static final String ACADEMIC_EMAIL = "emmanuel.demo@virtual-insurance.test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

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

	@Test
	void downloadsOwnedPolicyAsRealPdfWithPolicyData() throws Exception {
		Long policyId = createPolicy("AUTO");
		Long documentId = documentId(policyId, "POLICY_PDF");
		String policyNumber = jdbcTemplate.queryForObject(
			"select folio from poliza where poliza_id = ?", String.class, policyId
		);

		MvcResult result = mockMvc.perform(get(
			"/polizas/{policyId}/documentos/{documentId}/contenido", policyId, documentId
		).with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andExpect(header().string("Content-Type", "application/pdf"))
			.andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
			.andReturn();

		byte[] content = result.getResponse().getContentAsByteArray();
		org.assertj.core.api.Assertions.assertThat(content).startsWith('%', 'P', 'D', 'F');
		try (var document = Loader.loadPDF(content)) {
			String text = new PDFTextStripper().getText(document);
			org.assertj.core.api.Assertions.assertThat(text)
				.contains("Poliza academica " + policyNumber)
				.contains("DOCUMENTO EXCLUSIVAMENTE ACADEMICO")
				.contains("Mazda 3 - 2026 - Placas: POL123A")
				.contains("Modalidad: Mensual")
				.contains("Version de plantilla: NEDW-2026.1");
		}
	}

	@Test
	void downloadsOwnedPaymentReceiptAsRealPdf() throws Exception {
		Long policyId = createPolicy("MEDICAL");
		Long documentId = documentId(policyId, "PAYMENT_RECEIPT");

		MvcResult result = mockMvc.perform(get(
			"/polizas/{policyId}/documentos/{documentId}/contenido", policyId, documentId
		).with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andReturn();

		try (var document = Loader.loadPDF(result.getResponse().getContentAsByteArray())) {
			String text = new PDFTextStripper().getText(document);
			org.assertj.core.api.Assertions.assertThat(text)
				.contains("Comprobante academico de pago")
				.contains("Seguro medico")
				.contains("Autorizacion simulada");
		}
	}

	@Test
	void downloadsPolicyWithLocalizedPendingCancellationAndRequestNumber() throws Exception {
		Long policyId = createPolicy("AUTO");
		Long documentId = documentId(policyId, "POLICY_PDF");

		MvcResult cancellationResult = mockMvc.perform(post(
			"/polizas/{policyId}/solicitudes-cancelacion", policyId
		).with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
				  "reason":"FINANCIAL",
				  "requestedDate":"%s",
				  "comments":"Solicitud para verificar el PDF",
				  "confirmed":true
				}
				""".formatted(LocalDate.now())))
			.andExpect(status().isCreated())
			.andReturn();
		String cancellationNumber = JsonPath.read(
			cancellationResult.getResponse().getContentAsString(), "$.requestNumber"
		);

		MvcResult downloadResult = mockMvc.perform(get(
			"/polizas/{policyId}/documentos/{documentId}/contenido", policyId, documentId
		).with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andReturn();

		try (var document = Loader.loadPDF(downloadResult.getResponse().getContentAsByteArray())) {
			String text = new PDFTextStripper().getText(document);
			org.assertj.core.api.Assertions.assertThat(text)
				.contains("Estatus: Cancelacion solicitada")
				.contains("Folio de cancelacion: " + cancellationNumber)
				.doesNotContain("Estatus: CANCELACION_SOLICITADA");
		}
	}

	@Test
	void hidesDocumentFromAnotherClient() throws Exception {
		Long policyId = createPolicy("AUTO");
		Long documentId = documentId(policyId, "POLICY_PDF");

		mockMvc.perform(get(
			"/polizas/{policyId}/documentos/{documentId}/contenido", policyId, documentId
		).with(authenticatedClient(999999999L, "otro@virtual-insurance.test")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("POLICY_DOCUMENT_NOT_FOUND"));
	}

	@Test
	void returnsNotFoundForDocumentOutsidePolicy() throws Exception {
		Long firstPolicyId = createPolicy("AUTO");
		Long secondPolicyId = createPolicy("MEDICAL");
		Long documentId = documentId(secondPolicyId, "POLICY_PDF");

		mockMvc.perform(get(
			"/polizas/{policyId}/documentos/{documentId}/contenido", firstPolicyId, documentId
		).with(authenticatedAcademicClient()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("POLICY_DOCUMENT_NOT_FOUND"));
	}

	@Test
	void rejectsDocumentDownloadWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/polizas/1/documentos/1/contenido"))
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

	private Long documentId(Long policyId, String type) {
		return jdbcTemplate.queryForObject(
			"select documento_poliza_id from documento_poliza where poliza_id = ? and tipo_documento = ?",
			Long.class,
			policyId,
			type
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

