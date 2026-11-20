package mx.unam.virtualinsurance.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import mx.unam.virtualinsurance.catalog.gender.GenderRepository;
import mx.unam.virtualinsurance.customer.ClientRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrationIntegrationTests {

	private static final String EMAIL = "registro.e2e@virtual-insurance.test";
	private static final String FISCAL_ID = "REGISTROE2E001";
	private static final String PASSWORD = "Contrasena-segura-2026";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private GenderRepository genderRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void registersClientHashesPasswordStoresPhotoAndCreatesSession() throws Exception {
		MvcResult result = mockMvc.perform(registrationRequest(
			registrationJson(EMAIL, FISCAL_ID, adultBirthDate(), true, null),
			pngPhoto()
		).with(csrf()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value(EMAIL))
			.andExpect(jsonPath("$.fiscalIdentifier").value(FISCAL_ID))
			.andExpect(jsonPath("$.photoUrl").value("/clientes/me/foto"))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andReturn();

		var client = clientRepository.findByEmail(EMAIL).orElseThrow();
		assertThat(client.getPasswordHash()).isNotEqualTo(PASSWORD);
		assertThat(passwordEncoder.matches(PASSWORD, client.getPasswordHash())).isTrue();
		assertThat(client.getPhoto()).containsExactly(pngBytes());

		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mockMvc.perform(get("/auth/me").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authenticated").value(true))
			.andExpect(jsonPath("$.client.id").value(client.getClientId()))
			.andExpect(jsonPath("$.client.email").value(EMAIL));
	}

	@Test
	void rejectsDuplicateEmail() throws Exception {
		mockMvc.perform(registrationRequest(
			registrationJson(
				"emmanuel.demo@virtual-insurance.test",
				"FISCAL-UNICO-E2E",
				adultBirthDate(),
				true,
				null
			),
			pngPhoto()
		).with(csrf()))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void rejectsUnderageClient() throws Exception {
		mockMvc.perform(registrationRequest(
			registrationJson(EMAIL, FISCAL_ID, LocalDate.now().minusYears(17), true, null),
			pngPhoto()
		).with(csrf()))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void rejectsContentThatDoesNotMatchDeclaredPhotoType() throws Exception {
		MockMultipartFile invalidPhoto = new MockMultipartFile(
			"photo",
			"foto.png",
			"image/png",
			"no-es-una-imagen".getBytes(StandardCharsets.UTF_8)
		);

		mockMvc.perform(registrationRequest(
			registrationJson(EMAIL, FISCAL_ID, adultBirthDate(), true, null),
			invalidPhoto
		).with(csrf()))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("UNSUPPORTED_FILE"));
	}

	@Test
	void rejectsTermsNotAccepted() throws Exception {
		mockMvc.perform(registrationRequest(
			registrationJson(EMAIL, FISCAL_ID, adultBirthDate(), false, null),
			pngPhoto()
		).with(csrf()))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("acceptsTerms"));
	}

	@Test
	void rejectsUnknownGender() throws Exception {
		mockMvc.perform(registrationRequest(
			registrationJson(EMAIL, FISCAL_ID, adultBirthDate(), true, 999999999L),
			pngPhoto()
		).with(csrf()))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void rejectsRegistrationWithoutCsrfToken() throws Exception {
		mockMvc.perform(registrationRequest(
			registrationJson(EMAIL, FISCAL_ID, adultBirthDate(), true, null),
			pngPhoto()
		))
			.andExpect(status().isForbidden());
	}

	private org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
		registrationRequest(String json, MockMultipartFile photo) {
		MockMultipartFile data = new MockMultipartFile(
			"data",
			"data.json",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8)
		);
		return multipart("/auth/registro").file(data).file(photo);
	}

	private String registrationJson(
		String email,
		String fiscalIdentifier,
		LocalDate birthDate,
		boolean acceptsTerms,
		Long genderId
	) {
		Long resolvedGenderId = genderId;
		if (resolvedGenderId == null) {
			resolvedGenderId = genderRepository.findByKey("HOMBRE").orElseThrow().getGeneroId();
		}

		return """
			{
			  "name": "Cliente",
			  "paternalSurname": "Registro",
			  "maternalSurname": "E2E",
			  "birthDate": "%s",
			  "sex": "M",
			  "genderId": %d,
			  "fiscalIdentifier": "%s",
			  "email": "%s",
			  "password": "%s",
			  "acceptsTerms": %s
			}
			""".formatted(
			birthDate,
			resolvedGenderId,
			fiscalIdentifier,
			email,
			PASSWORD,
			acceptsTerms
		);
	}

	private LocalDate adultBirthDate() {
		return LocalDate.now().minusYears(25);
	}

	private MockMultipartFile pngPhoto() {
		return new MockMultipartFile("photo", "foto.png", "image/png", pngBytes());
	}

	private byte[] pngBytes() {
		return new byte[] {
			(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
			0x00, 0x00, 0x00, 0x00
		};
	}
}
