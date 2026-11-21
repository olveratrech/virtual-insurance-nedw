package mx.unam.virtualinsurance.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import mx.unam.virtualinsurance.auth.ClientPrincipal;
import mx.unam.virtualinsurance.catalog.gender.GenderRepository;
import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileIntegrationTests {

	private static final String EMAIL = "emmanuel.demo@virtual-insurance.test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private GenderRepository genderRepository;

	@Test
	void returnsOwnedProfileWithContractualFields() throws Exception {
		mockMvc.perform(get("/cuenta/perfil").with(authenticatedClient()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Emmanuel"))
			.andExpect(jsonPath("$.birthDate").exists())
			.andExpect(jsonPath("$.sex").exists())
			.andExpect(jsonPath("$.fiscalIdentifier").exists())
			.andExpect(jsonPath("$.email").value(EMAIL));
	}

	@Test
	void updatesEditableProfileFieldsWithoutChangingContractualIdentity() throws Exception {
		Client before = clientRepository.findByEmail(EMAIL).orElseThrow();
		LocalDate birthDate = before.getBirthDate();
		String fiscalIdentifier = before.getFiscalIdentifier();
		Long genderId = genderRepository.findByKey("PREFIERE_NO_DECIR").orElseThrow().getGeneroId();

		mockMvc.perform(put("/cuenta/perfil")
			.with(authenticatedClient()).with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(profileJson("Emmanuel actualizado", EMAIL, genderId, null)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Emmanuel actualizado"))
			.andExpect(jsonPath("$.gender.id").value(genderId));

		Client updated = clientRepository.findById(before.getClientId()).orElseThrow();
		assertThat(updated.getBirthDate()).isEqualTo(birthDate);
		assertThat(updated.getFiscalIdentifier()).isEqualTo(fiscalIdentifier);
	}

	@Test
	void requiresCurrentPasswordToChangeEmail() throws Exception {
		mockMvc.perform(put("/cuenta/perfil")
			.with(authenticatedClient()).with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(profileJson("Emmanuel", "nuevo@example.test", null, "incorrecta")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CURRENT_PASSWORD"));

		assertThat(clientRepository.findByEmail(EMAIL)).isPresent();
	}

	@Test
	void rejectsEmailOwnedByAnotherClient() throws Exception {
		Client other = createClient();

		mockMvc.perform(put("/cuenta/perfil")
			.with(authenticatedClient()).with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(profileJson("Emmanuel", other.getEmail(), null, "Virtual2026!")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void updatesAndServesValidatedProfilePhoto() throws Exception {
		MockMultipartFile photo = new MockMultipartFile("photo", "perfil.png", "image/png", pngBytes());

		mockMvc.perform(multipart("/cuenta/perfil/fotografia").file(photo)
			.with(request -> { request.setMethod("PUT"); return request; })
			.with(authenticatedClient()).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.photoUrl").value("/clientes/me/foto"));

		mockMvc.perform(get("/clientes/me/foto").with(authenticatedClient()))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.IMAGE_PNG))
			.andExpect(content().bytes(pngBytes()));
	}

	@Test
	void deletesProfilePhoto() throws Exception {
		mockMvc.perform(delete("/cuenta/perfil/fotografia")
			.with(authenticatedClient()).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.photoUrl").value(nullValue()));

		mockMvc.perform(get("/clientes/me/foto").with(authenticatedClient()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PROFILE_PHOTO_NOT_FOUND"));
	}

	@Test
	void protectsProfileWritesWithAuthenticationAndCsrf() throws Exception {
		mockMvc.perform(put("/cuenta/perfil")
			.with(csrf()).contentType(MediaType.APPLICATION_JSON)
			.content(profileJson("Emmanuel", EMAIL, null, null)))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(put("/cuenta/perfil")
			.with(authenticatedClient()).contentType(MediaType.APPLICATION_JSON)
			.content(profileJson("Emmanuel", EMAIL, null, null)))
			.andExpect(status().isForbidden());
	}

	private String profileJson(String name, String email, Long genderId, String currentPassword) {
		return """
			{
			  "name":"%s","paternalSurname":"Trejo","maternalSurname":"Olvera",
			  "genderId":%s,"email":"%s","currentPassword":%s
			}
			""".formatted(
			name,
			genderId == null ? "null" : genderId,
			email,
			currentPassword == null ? "null" : "\"" + currentPassword + "\""
		);
	}

	private Client createClient() {
		String unique = UUID.randomUUID().toString().replace("-", "");
		return clientRepository.saveAndFlush(new Client(
			"Otro", "Cliente", null, LocalDate.of(1990, 1, 1), "X",
			("PROFILE" + unique).substring(0, 30).toUpperCase(),
			"profile-" + unique + "@example.test", "x".repeat(60), null, null
		));
	}

	private RequestPostProcessor authenticatedClient() {
		Client client = clientRepository.findByEmail(EMAIL).orElseThrow();
		ClientPrincipal principal = new ClientPrincipal(client.getClientId(), client.getEmail(), "unused");
		var token = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
		return authentication(token);
	}

	private byte[] pngBytes() {
		return new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00};
	}
}
