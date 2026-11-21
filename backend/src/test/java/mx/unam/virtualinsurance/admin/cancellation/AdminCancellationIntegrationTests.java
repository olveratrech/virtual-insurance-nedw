package mx.unam.virtualinsurance.admin.cancellation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;

import mx.unam.virtualinsurance.admin.auth.InternalUserPrincipal;
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
class AdminCancellationIntegrationTests {

	private static final String CLIENT_EMAIL = "emmanuel.demo@virtual-insurance.test";
	private static final String ADMIN_EMAIL = "admin.cancelaciones@virtual-insurance.test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void listsPendingRequestsByDefaultAndSupportsStatusFilter() throws Exception {
		Long cancellationId = createCancellation();

		MvcResult pendingResult = mockMvc.perform(get("/admin/solicitudes-cancelacion")
			.with(authenticatedAdmin()))
			.andExpect(status().isOk())
			.andReturn();
		assertThat(idsFrom(pendingResult)).contains(cancellationId.intValue());

		resolve(cancellationId, "RECHAZADA", "Se conserva la cobertura").andExpect(status().isOk());

		MvcResult rejectedResult = mockMvc.perform(get("/admin/solicitudes-cancelacion")
			.param("status", "RECHAZADA")
			.with(authenticatedAdmin()))
			.andExpect(status().isOk())
			.andReturn();
		assertThat(idsFrom(rejectedResult)).contains(cancellationId.intValue());
	}

	@Test
	void returnsAdministrativeCancellationDetail() throws Exception {
		Long cancellationId = createCancellation();

		mockMvc.perform(get("/admin/solicitudes-cancelacion/{id}", cancellationId)
			.with(authenticatedAdmin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(cancellationId))
			.andExpect(jsonPath("$.status").value("SOLICITADA"))
			.andExpect(jsonPath("$.reason").value("FINANCIAL"))
			.andExpect(jsonPath("$.policy.status").value("CANCELACION_SOLICITADA"))
			.andExpect(jsonPath("$.policy.type").value("AUTO"))
			.andExpect(jsonPath("$.client.email").value(CLIENT_EMAIL))
			.andExpect(jsonPath("$.resolution").doesNotExist());
	}

	@Test
	void approvesCancellationAndPersistsResolverPolicyAndHistory() throws Exception {
		Long cancellationId = createCancellation();
		Long policyId = policyId(cancellationId);
		int historyBefore = historyCount(policyId);
		Long adminId = adminId();

		resolve(cancellationId, "APROBADA", "  Procede la cancelacion inmediata  ")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("APROBADA"))
			.andExpect(jsonPath("$.policy.status").value("CANCELADA"))
			.andExpect(jsonPath("$.resolution.observation")
				.value("Procede la cancelacion inmediata"))
			.andExpect(jsonPath("$.resolution.resolvedById").value(adminId))
			.andExpect(jsonPath("$.resolution.resolvedByName")
				.value("Administrador academico"))
			.andExpect(jsonPath("$.resolution.resolvedAt").isNotEmpty());

		assertThat(policyStatus(policyId)).isEqualTo("CANCELADA");
		assertThat(historyCount(policyId)).isEqualTo(historyBefore + 1);
		assertThat(jdbcTemplate.queryForObject(
			"select resuelta_por_usuario_id from solicitud_cancelacion where solicitud_cancelacion_id = ?",
			Long.class,
			cancellationId
		)).isEqualTo(adminId);
	}

	@Test
	void rejectsCancellationAndReactivatesPolicy() throws Exception {
		Long cancellationId = createCancellation();
		Long policyId = policyId(cancellationId);

		resolve(cancellationId, "RECHAZADA", "No cumple los criterios academicos")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("RECHAZADA"))
			.andExpect(jsonPath("$.policy.status").value("ACTIVA"));

		assertThat(policyStatus(policyId)).isEqualTo("ACTIVA");
	}

	@Test
	void rejectsSecondResolution() throws Exception {
		Long cancellationId = createCancellation();
		resolve(cancellationId, "APROBADA", "Primera resolucion").andExpect(status().isOk());

		resolve(cancellationId, "RECHAZADA", "Segundo intento")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("CANCELLATION_ALREADY_RESOLVED"));
	}

	@Test
	void returnsNotFoundForUnknownCancellationDetail() throws Exception {
		mockMvc.perform(get("/admin/solicitudes-cancelacion/999999999")
			.with(authenticatedAdmin()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("CANCELLATION_REQUEST_NOT_FOUND"));
	}

	@Test
	void returnsNotFoundWhenResolvingUnknownCancellation() throws Exception {
		resolve(999999999L, "APROBADA", "No existe")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("CANCELLATION_REQUEST_NOT_FOUND"));
	}

	@Test
	void validatesDecisionAndObservation() throws Exception {
		Long cancellationId = createCancellation();

		mockMvc.perform(put(
			"/admin/solicitudes-cancelacion/{id}/resolucion", cancellationId
		).with(authenticatedAdmin())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{"decision":"DESCONOCIDA","observation":"   "}
				"""))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors.length()").value(2));
	}

	@Test
	void rejectsInvalidStatusFilter() throws Exception {
		mockMvc.perform(get("/admin/solicitudes-cancelacion")
			.param("status", "PENDIENTE")
			.with(authenticatedAdmin()))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void rejectsResolutionWithoutCsrf() throws Exception {
		Long cancellationId = createCancellation();

		mockMvc.perform(put(
			"/admin/solicitudes-cancelacion/{id}/resolucion", cancellationId
		).with(authenticatedAdmin())
			.contentType(MediaType.APPLICATION_JSON)
			.content(resolutionJson("APROBADA", "Sin token")))
			.andExpect(status().isForbidden());
	}

	@Test
	void rejectsAdministrativeQueryWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/admin/solicitudes-cancelacion"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsClientSessionOnAdministrativeEndpoints() throws Exception {
		mockMvc.perform(get("/admin/solicitudes-cancelacion")
			.with(authenticatedAcademicClient()))
			.andExpect(status().isForbidden());
	}

	private org.springframework.test.web.servlet.ResultActions resolve(
		Long cancellationId,
		String decision,
		String observation
	) throws Exception {
		return mockMvc.perform(put(
			"/admin/solicitudes-cancelacion/{id}/resolucion", cancellationId
		).with(authenticatedAdmin())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(resolutionJson(decision, observation)));
	}

	private String resolutionJson(String decision, String observation) {
		return """
			{"decision":"%s","observation":"%s"}
			""".formatted(decision, observation);
	}

	private Long createCancellation() throws Exception {
		Long policyId = createPolicy();
		MvcResult result = mockMvc.perform(post(
			"/polizas/{policyId}/solicitudes-cancelacion", policyId
		).with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
				  "reason":"FINANCIAL",
				  "requestedDate":"%s",
				  "comments":"Solicitud para revision administrativa",
				  "confirmed":true
				}
				""".formatted(LocalDate.now())))
			.andExpect(status().isCreated())
			.andReturn();
		Number cancellationId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		return cancellationId.longValue();
	}

	private Long createPolicy() throws Exception {
		MvcResult quoteResult = mockMvc.perform(post("/cotizaciones/auto")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
				  "brand":"Mazda","model":"3","year":2026,"category":"SEDAN",
				  "plates":"ADM123A","licenseNumber":"ADM-LIC-1","driverAge":26,
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

	@SuppressWarnings("unchecked")
	private List<Integer> idsFrom(MvcResult result) throws Exception {
		return JsonPath.read(result.getResponse().getContentAsString(), "$.items[*].id");
	}

	private Long policyId(Long cancellationId) {
		return jdbcTemplate.queryForObject(
			"select poliza_id from solicitud_cancelacion where solicitud_cancelacion_id = ?",
			Long.class,
			cancellationId
		);
	}

	private Long adminId() {
		return jdbcTemplate.queryForObject(
			"select usuario_interno_id from usuario_interno where email = ?",
			Long.class,
			ADMIN_EMAIL
		);
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

	private RequestPostProcessor authenticatedAdmin() {
		InternalUserPrincipal principal = new InternalUserPrincipal(
			adminId(),
			"Administrador academico",
			ADMIN_EMAIL,
			"unused",
			true,
			List.of("ADMIN", "CANCELLATION_REVIEWER")
		);
		var token = UsernamePasswordAuthenticationToken.authenticated(
			principal, null, principal.getAuthorities()
		);
		return authentication(token);
	}

	private RequestPostProcessor authenticatedAcademicClient() {
		Client client = clientRepository.findByEmail(CLIENT_EMAIL).orElseThrow();
		ClientPrincipal principal = new ClientPrincipal(
			client.getClientId(), client.getEmail(), "unused"
		);
		var token = UsernamePasswordAuthenticationToken.authenticated(
			principal, null, principal.getAuthorities()
		);
		return authentication(token);
	}
}
