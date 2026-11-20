package mx.unam.virtualinsurance.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTests {

	private static final String EMAIL = "emmanuel.demo@virtual-insurance.test";
	private static final String PASSWORD = "Virtual2026!";
	private static final String INVALID_CREDENTIALS_DETAIL =
		"El correo o la contrasena son incorrectos.";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void logsInWithValidCredentialsAndCreatesSession() throws Exception {
		MvcResult result = mockMvc.perform(loginRequest(EMAIL, PASSWORD).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authenticated").value(true))
			.andExpect(jsonPath("$.client.email").value(EMAIL))
			.andExpect(jsonPath("$.client.photoUrl").value("/clientes/me/foto"))
			.andReturn();

		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mockMvc.perform(get("/auth/me").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.client.email").value(EMAIL));
	}

	@Test
	void rejectsIncorrectPasswordWithGenericResponse() throws Exception {
		mockMvc.perform(loginRequest(EMAIL, "Contrasena-incorrecta").with(csrf()))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
			.andExpect(jsonPath("$.detail").value(INVALID_CREDENTIALS_DETAIL));
	}

	@Test
	void rejectsUnknownEmailWithSameGenericResponse() throws Exception {
		mockMvc.perform(loginRequest("no-existe@virtual-insurance.test", PASSWORD).with(csrf()))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
			.andExpect(jsonPath("$.detail").value(INVALID_CREDENTIALS_DETAIL));
	}

	@Test
	void rejectsLoginWithoutCsrfToken() throws Exception {
		mockMvc.perform(loginRequest(EMAIL, PASSWORD))
			.andExpect(status().isForbidden());
	}

	@Test
	void normalizesEmailBeforeAuthentication() throws Exception {
		mockMvc.perform(loginRequest("  EMMANUEL.DEMO@VIRTUAL-INSURANCE.TEST  ", PASSWORD).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.client.email").value(EMAIL));
	}

	@Test
	void logsOutAndInvalidatesSession() throws Exception {
		MvcResult loginResult = mockMvc.perform(loginRequest(EMAIL, PASSWORD).with(csrf()))
			.andExpect(status().isOk())
			.andReturn();
		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(post("/auth/logout").session(session).with(csrf()))
			.andExpect(status().isNoContent())
			.andExpect(cookie().maxAge("JSESSIONID", 0));

		assertThat(session.isInvalid()).isTrue();
	}

	@Test
	void rejectsSessionCheckAfterLogout() throws Exception {
		MvcResult loginResult = mockMvc.perform(loginRequest(EMAIL, PASSWORD).with(csrf()))
			.andExpect(status().isOk())
			.andReturn();
		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(post("/auth/logout").session(session).with(csrf()))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/auth/me"))
			.andExpect(status().isUnauthorized());
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(
		String email,
		String password
	) {
		String body = """
			{
			  "email": "%s",
			  "password": "%s"
			}
			""".formatted(email, password);

		return post("/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body);
	}
}
