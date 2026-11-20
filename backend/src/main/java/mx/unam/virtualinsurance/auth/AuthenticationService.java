package mx.unam.virtualinsurance.auth;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;
import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

	private static final String PHOTO_URL = "/clientes/me/foto";

	private final AuthenticationManager authenticationManager;
	private final ClientRepository clientRepository;
	private final SecuritySessionService securitySessionService;

	public AuthenticationService(
		AuthenticationManager authenticationManager,
		ClientRepository clientRepository,
		SecuritySessionService securitySessionService
	) {
		this.authenticationManager = authenticationManager;
		this.clientRepository = clientRepository;
		this.securitySessionService = securitySessionService;
	}

	@Transactional(readOnly = true)
	public AuthenticationStatusResponse login(
		LoginRequest loginRequest,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String email = loginRequest.email().trim().toLowerCase(Locale.ROOT);

		try {
			var authenticationRequest =
				UsernamePasswordAuthenticationToken.unauthenticated(email, loginRequest.password());
			var authentication = authenticationManager.authenticate(authenticationRequest);
			ClientPrincipal principal = (ClientPrincipal) authentication.getPrincipal();
			securitySessionService.authenticate(principal, request, response);
			return findAuthenticatedClient(principal.clientId());
		} catch (AuthenticationException exception) {
			throw invalidCredentials();
		}
	}

	@Transactional(readOnly = true)
	public AuthenticationStatusResponse findAuthenticatedClient(Long clientId) {
		Client client = clientRepository.findById(clientId)
			.orElseThrow(this::invalidSession);

		SessionClientResponse response = new SessionClientResponse(
			client.getClientId(),
			client.getName(),
			client.getPaternalSurname(),
			client.getMaternalSurname(),
			client.getEmail(),
			PHOTO_URL
		);

		return new AuthenticationStatusResponse(true, response);
	}

	private ApiException invalidCredentials() {
		return new ApiException(
			HttpStatus.UNAUTHORIZED,
			"INVALID_CREDENTIALS",
			"No fue posible iniciar sesion",
			"El correo o la contrasena son incorrectos.",
			"invalid-credentials"
		);
	}

	private ApiException invalidSession() {
		return new ApiException(
			HttpStatus.UNAUTHORIZED,
			"INVALID_SESSION",
			"La sesion no es valida",
			"No existe un cliente para la sesion actual.",
			"invalid-session"
		);
	}
}
