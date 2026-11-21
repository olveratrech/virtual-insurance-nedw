package mx.unam.virtualinsurance.quote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuoteIntegrationTests {

	private static final String ACADEMIC_EMAIL = "emmanuel.demo@virtual-insurance.test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private AutoQuoteRepository autoQuoteRepository;

	@Autowired
	private MedicalQuoteRepository medicalQuoteRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void createsApprovedAutoQuoteWithSubtypeAndHistory() throws Exception {
		MvcResult result = mockMvc.perform(post("/cotizaciones/auto")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(autoJson(0, false)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.type").value("AUTO"))
			.andExpect(jsonPath("$.status").value("APROBADA"))
			.andExpect(jsonPath("$.annualCost").value(12480.00))
			.andExpect(jsonPath("$.monthlyCost").value(1040.00))
			.andExpect(jsonPath("$.contracted").value(false))
			.andExpect(jsonPath("$.policyId").value(org.hamcrest.Matchers.nullValue()))
			.andExpect(jsonPath("$.policyStatus").value(org.hamcrest.Matchers.nullValue()))
			.andExpect(jsonPath("$.quoteNumber").value(org.hamcrest.Matchers.matchesPattern("[1-9][0-9]{3}")))
			.andExpect(jsonPath("$.details.plates").value("ABC123A"))
			.andReturn();

		Long quoteId = quoteId(result);
		assertThat(autoQuoteRepository.existsById(quoteId)).isTrue();
		assertHistoryCreated(quoteId, "APROBADA");

		mockMvc.perform(get("/cotizaciones/{quoteId}", quoteId)
			.with(authenticatedAcademicClient()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(quoteId))
			.andExpect(jsonPath("$.status").value("APROBADA"));
	}

	@Test
	void rejectsAutoQuoteWithTwelveNegativePoints() throws Exception {
		MvcResult result = mockMvc.perform(post("/cotizaciones/auto")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(autoJson(12, false)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RECHAZADA"))
			.andExpect(jsonPath("$.annualCost").value(org.hamcrest.Matchers.nullValue()))
			.andExpect(jsonPath("$.benefits").isEmpty())
			.andExpect(jsonPath("$.rejectionReason").isNotEmpty())
			.andReturn();

		assertHistoryCreated(quoteId(result), "RECHAZADA");
	}

	@Test
	void rejectsAutoQuoteWithCriminalRecord() throws Exception {
		mockMvc.perform(post("/cotizaciones/auto")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(autoJson(0, true)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RECHAZADA"));
	}

	@Test
	void createsApprovedMedicalQuoteWithSubtypeAndHistory() throws Exception {
		MvcResult result = mockMvc.perform(post("/cotizaciones/medicas")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(medicalJson(35, false, false, false, false, false)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.type").value("MEDICAL"))
			.andExpect(jsonPath("$.status").value("APROBADA"))
			.andExpect(jsonPath("$.annualCost").value(18600.00))
			.andExpect(jsonPath("$.monthlyCost").value(1550.00))
			.andReturn();

		Long quoteId = quoteId(result);
		assertThat(medicalQuoteRepository.existsById(quoteId)).isTrue();
		assertHistoryCreated(quoteId, "APROBADA");
	}

	@Test
	void rejectsMedicalQuoteWithThreeActiveFactors() throws Exception {
		mockMvc.perform(post("/cotizaciones/medicas")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(medicalJson(40, true, true, true, false, false)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RECHAZADA"))
			.andExpect(jsonPath("$.annualCost").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	void rejectsMedicalQuoteWhenAgeIsAboveSeventy() throws Exception {
		mockMvc.perform(post("/cotizaciones/medicas")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(medicalJson(71, false, false, false, false, false)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RECHAZADA"));
	}

	@Test
	void rejectsVehicleYearAfterNextCalendarYear() throws Exception {
		int invalidYear = java.time.Year.now().getValue() + 2;
		String body = autoJson(0, false).replace("2026", String.valueOf(invalidYear));

		mockMvc.perform(post("/cotizaciones/auto")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void rejectsQuoteCreationWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/cotizaciones/auto")
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(autoJson(0, false)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void hidesQuoteFromAnotherClient() throws Exception {
		MvcResult result = mockMvc.perform(post("/cotizaciones/auto")
			.with(authenticatedAcademicClient())
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(autoJson(0, false)))
			.andExpect(status().isCreated())
			.andReturn();

		mockMvc.perform(get("/cotizaciones/{quoteId}", quoteId(result))
			.with(authenticatedClient(999999999L, "otro@virtual-insurance.test")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("QUOTE_NOT_FOUND"));
	}

	private RequestPostProcessor authenticatedAcademicClient() {
		Client client = clientRepository.findByEmail(ACADEMIC_EMAIL).orElseThrow();
		return authenticatedClient(client.getClientId(), client.getEmail());
	}

	private RequestPostProcessor authenticatedClient(Long clientId, String email) {
		ClientPrincipal principal = new ClientPrincipal(clientId, email, "hash-no-utilizado");
		var token = UsernamePasswordAuthenticationToken.authenticated(
			principal,
			null,
			principal.getAuthorities()
		);
		return authentication(token);
	}

	private Long quoteId(MvcResult result) throws Exception {
		Number value = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		return value.longValue();
	}

	private void assertHistoryCreated(Long quoteId, String expectedStatus) {
		Integer count = jdbcTemplate.queryForObject("""
			select count(*)
			  from historico_estatus_cotizacion h
			  join estatus_cotizacion e
			    on e.estatus_cotizacion_id = h.estatus_cotizacion_id
			 where h.cotizacion_id = ?
			   and h.numero_cambio = 1
			   and e.clave = ?
			""", Integer.class, quoteId, expectedStatus);
		assertThat(count).isEqualTo(1);
	}

	private String autoJson(int negativePoints, boolean criminalRecord) {
		return """
			{
			  "brand": "Mazda",
			  "model": "3",
			  "year": 2026,
			  "category": "SEDAN",
			  "plates": "abc123a",
			  "licenseNumber": "lic-12345",
			  "driverAge": 26,
			  "negativePoints": %d,
			  "criminalRecord": %s
			}
			""".formatted(negativePoints, criminalRecord);
	}

	private String medicalJson(
		int age,
		boolean hypertension,
		boolean glucoseOutOfRange,
		boolean cholesterolOutOfRange,
		boolean smoker,
		boolean alcoholAddiction
	) {
		return """
			{
			  "age": %d,
			  "hypertension": %s,
			  "glucoseOutOfRange": %s,
			  "cholesterolOutOfRange": %s,
			  "smoker": %s,
			  "alcoholAddiction": %s
			}
			""".formatted(
			age,
			hypertension,
			glucoseOutOfRange,
			cholesterolOutOfRange,
			smoker,
			alcoholAddiction
		);
	}
}
