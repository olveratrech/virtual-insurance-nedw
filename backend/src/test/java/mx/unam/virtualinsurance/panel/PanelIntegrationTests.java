package mx.unam.virtualinsurance.panel;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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
class PanelIntegrationTests {

	private static final String ACADEMIC_EMAIL = "emmanuel.demo@virtual-insurance.test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Test
	void returnsOwnedQuoteWithContractFieldsAndDestination() throws Exception {
		Long quoteId = createAutoQuote(authenticatedAcademicClient());

		mockMvc.perform(get("/panel").with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.clientName").value("Emmanuel"))
			.andExpect(jsonPath("$.recentActivity[0].id").value("quote-" + quoteId))
			.andExpect(jsonPath("$.recentActivity[0].resourceType").value("QUOTE"))
			.andExpect(jsonPath("$.recentActivity[0].resourceId").value(quoteId))
			.andExpect(jsonPath("$.recentActivity[0].typeLabel").value("Cotización de auto"))
			.andExpect(jsonPath("$.recentActivity[0].reference").value(org.hamcrest.Matchers.startsWith("#")))
			.andExpect(jsonPath("$.recentActivity[0].status").value("APROBADA"))
			.andExpect(jsonPath("$.recentActivity[0].detail").value(org.hamcrest.Matchers.containsString("MXN anuales")))
			.andExpect(jsonPath("$.recentActivity[0].destination").value("/cotizaciones/" + quoteId));
	}

	@Test
	void ordersNewPolicyBeforeItsQuote() throws Exception {
		Long policyId = createPolicy();

		mockMvc.perform(get("/panel").with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.recentActivity[0].id").value("policy-" + policyId))
			.andExpect(jsonPath("$.recentActivity[0].resourceType").value("POLICY"))
			.andExpect(jsonPath("$.recentActivity[0].status").value("ACTIVA"))
			.andExpect(jsonPath("$.recentActivity[0].detail").value(org.hamcrest.Matchers.startsWith("Vigencia hasta el ")))
			.andExpect(jsonPath("$.recentActivity[0].destination").value("/polizas/" + policyId));
	}

	@Test
	void reflectsPendingCancellationInPolicyActivity() throws Exception {
		Long policyId = createPolicy();
		mockMvc.perform(post("/polizas/{policyId}/solicitudes-cancelacion", policyId)
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
				  "reason":"NOT_NEEDED","requestedDate":"%s",
				  "comments":null,"confirmed":true
				}
				""".formatted(LocalDate.now().plusDays(2))))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/panel").with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andExpect(jsonPath(
				"$.recentActivity[?(@.id == 'policy-%s')].status".formatted(policyId)
			).value(hasItem("CANCELACION_SOLICITADA")));
	}

	@Test
	void returnsEmptyActivityForClientWithoutOperations() throws Exception {
		Client client = createClient("Vacio");

		mockMvc.perform(get("/panel").with(authenticatedClient(client)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.clientName").value("Vacio"))
			.andExpect(jsonPath("$.recentActivity").isEmpty());
	}

	@Test
	void excludesActivityOwnedByAnotherClient() throws Exception {
		Client otherClient = createClient("Otro");
		Long otherQuoteId = createAutoQuote(authenticatedClient(otherClient));

		mockMvc.perform(get("/panel").with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.recentActivity[*].id", not(hasItem("quote-" + otherQuoteId))));
	}

	@Test
	void rejectsPanelWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/panel"))
			.andExpect(status().isUnauthorized());
	}

	private Long createPolicy() throws Exception {
		Long quoteId = createAutoQuote(authenticatedAcademicClient());
		MvcResult result = mockMvc.perform(post(
			"/cotizaciones/{quoteId}/intentos-pago", quoteId
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
		Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.policyId");
		return id.longValue();
	}

	private Long createAutoQuote(RequestPostProcessor authentication) throws Exception {
		MvcResult result = mockMvc.perform(post("/cotizaciones/auto")
			.with(authentication)
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
				  "brand":"Mazda","model":"3","year":2026,"category":"SEDAN",
				  "plates":"PAN123A","licenseNumber":"PAN-LIC-1","driverAge":26,
				  "negativePoints":0,"criminalRecord":false
				}
				"""))
			.andExpect(status().isCreated())
			.andReturn();
		Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		return id.longValue();
	}

	private Client createClient(String name) {
		String unique = UUID.randomUUID().toString().replace("-", "");
		return clientRepository.saveAndFlush(new Client(
			name,
			"Prueba",
			null,
			LocalDate.of(1990, 1, 1),
			"X",
			("PANEL" + unique).substring(0, 30).toUpperCase(),
			"panel-" + unique + "@example.test",
			"x".repeat(60),
			null,
			null
		));
	}

	private RequestPostProcessor authenticatedAcademicClient() {
		Client client = clientRepository.findByEmail(ACADEMIC_EMAIL).orElseThrow();
		return authenticatedClient(client);
	}

	private RequestPostProcessor authenticatedClient(Client client) {
		ClientPrincipal principal = new ClientPrincipal(client.getClientId(), client.getEmail(), "unused");
		var token = UsernamePasswordAuthenticationToken.authenticated(
			principal, null, principal.getAuthorities()
		);
		return authentication(token);
	}
}
