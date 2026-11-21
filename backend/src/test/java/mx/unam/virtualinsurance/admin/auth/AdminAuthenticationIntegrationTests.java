package mx.unam.virtualinsurance.admin.auth;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthenticationIntegrationTests {

	private static final String EMAIL = "admin.cancelaciones@virtual-insurance.test";
	private static final String CLIENT_EMAIL = "emmanuel.demo@virtual-insurance.test";
	private static final String PASSWORD = "Virtual2026!";
	private static final String INVALID_CREDENTIALS_DETAIL =
		"El correo o la contrasena son incorrectos.";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void logsInWithValidCredentialsAndReturnsInternalRoles() throws Exception {
		MvcResult result = mockMvc.perform(adminLoginRequest(EMAIL, PASSWORD).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authenticated").value(true))
			.andExpect(jsonPath("$.user.email").value(EMAIL))
			.andExpect(jsonPath("$.user.name").value("Administrador academico"))
			.andExpect(jsonPath("$.user.roles[0]").value("ADMIN"))
			.andExpect(jsonPath("$.user.roles[1]").value("CANCELLATION_REVIEWER"))
			.andReturn();

		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mockMvc.perform(get("/admin/auth/me").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.email").value(EMAIL));
	}

	@Test
	void normalizesEmailBeforeAuthentication() throws Exception {
		mockMvc.perform(adminLoginRequest(
			"  ADMIN.CANCELACIONES@VIRTUAL-INSURANCE.TEST  ",
			PASSWORD
		).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.email").value(EMAIL));
	}

	@Test
	void rejectsIncorrectPasswordWithGenericResponse() throws Exception {
		mockMvc.perform(adminLoginRequest(EMAIL, "Contrasena-incorrecta").with(csrf()))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_ADMIN_CREDENTIALS"))
			.andExpect(jsonPath("$.detail").value(INVALID_CREDENTIALS_DETAIL));
	}

	@Test
	void rejectsUnknownEmailWithSameGenericResponse() throws Exception {
		mockMvc.perform(adminLoginRequest("no-existe@virtual-insurance.test", PASSWORD).with(csrf()))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_ADMIN_CREDENTIALS"))
			.andExpect(jsonPath("$.detail").value(INVALID_CREDENTIALS_DETAIL));
	}

	@Test
	@Transactional
	@Rollback
	void rejectsInactiveInternalUser() throws Exception {
		jdbcTemplate.update(
			"update usuario_interno set activo = 'N' where email = ?",
			EMAIL
		);

		mockMvc.perform(adminLoginRequest(EMAIL, PASSWORD).with(csrf()))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_ADMIN_CREDENTIALS"))
			.andExpect(jsonPath("$.detail").value(INVALID_CREDENTIALS_DETAIL));
	}

	@Test
	void rejectsLoginWithoutCsrfToken() throws Exception {
		mockMvc.perform(adminLoginRequest(EMAIL, PASSWORD))
			.andExpect(status().isForbidden());
	}

	@Test
	void logsOutAndInvalidatesAdministrativeSession() throws Exception {
		MvcResult loginResult = mockMvc.perform(adminLoginRequest(EMAIL, PASSWORD).with(csrf()))
			.andExpect(status().isOk())
			.andReturn();
		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(post("/admin/auth/logout").session(session).with(csrf()))
			.andExpect(status().isNoContent())
			.andExpect(cookie().maxAge("JSESSIONID", 0));

		assertThat(session.isInvalid()).isTrue();
		mockMvc.perform(get("/admin/auth/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void clientSessionCannotAccessAdministrativeEndpoints() throws Exception {
		MockHttpSession session = loginClient();

		mockMvc.perform(get("/admin/auth/me").session(session))
			.andExpect(status().isForbidden());
	}

	@Test
	void administrativeSessionCannotAccessClientEndpoints() throws Exception {
		MockHttpSession session = loginAdmin();

		mockMvc.perform(get("/panel").session(session))
			.andExpect(status().isForbidden());
	}

	private MockHttpSession loginAdmin() throws Exception {
		MvcResult result = mockMvc.perform(adminLoginRequest(EMAIL, PASSWORD).with(csrf()))
			.andExpect(status().isOk())
			.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	private MockHttpSession loginClient() throws Exception {
		MvcResult result = mockMvc.perform(
			loginRequest("/auth/login", CLIENT_EMAIL, PASSWORD).with(csrf())
		)
			.andExpect(status().isOk())
			.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder adminLoginRequest(
		String email,
		String password
	) {
		return loginRequest("/admin/auth/login", email, password);
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(
		String path,
		String email,
		String password
	) {
		String body = """
			{
			  "email": "%s",
			  "password": "%s"
			}
			""".formatted(email, password);

		return post(path)
			.contentType(MediaType.APPLICATION_JSON)
			.content(body);
	}
}
