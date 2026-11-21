package mx.unam.virtualinsurance.admin.auth;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mx.unam.virtualinsurance.admin.user.InternalRole;
import mx.unam.virtualinsurance.admin.user.InternalUser;
import mx.unam.virtualinsurance.admin.user.InternalUserRepository;
import mx.unam.virtualinsurance.auth.SecuritySessionService;
import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthenticationService {

	private final AuthenticationManager authenticationManager;
	private final InternalUserRepository internalUserRepository;
	private final SecuritySessionService securitySessionService;

	public AdminAuthenticationService(
		@Qualifier("adminAuthenticationManager") AuthenticationManager authenticationManager,
		InternalUserRepository internalUserRepository,
		SecuritySessionService securitySessionService
	) {
		this.authenticationManager = authenticationManager;
		this.internalUserRepository = internalUserRepository;
		this.securitySessionService = securitySessionService;
	}

	@Transactional(readOnly = true)
	public AdminAuthenticationStatusResponse login(
		AdminLoginRequest loginRequest,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String email = loginRequest.email().trim().toLowerCase(Locale.ROOT);

		try {
			var authenticationRequest =
				UsernamePasswordAuthenticationToken.unauthenticated(email, loginRequest.password());
			var authentication = authenticationManager.authenticate(authenticationRequest);
			InternalUserPrincipal principal = (InternalUserPrincipal) authentication.getPrincipal();
			securitySessionService.authenticate(principal, request, response);
			return findAuthenticatedUser(principal.internalUserId());
		} catch (AuthenticationException exception) {
			throw invalidCredentials();
		}
	}

	@Transactional(readOnly = true)
	public AdminAuthenticationStatusResponse findAuthenticatedUser(Long internalUserId) {
		InternalUser user = internalUserRepository.findByInternalUserId(internalUserId)
			.filter(InternalUser::isActive)
			.orElseThrow(this::invalidSession);

		InternalUserResponse response = new InternalUserResponse(
			user.getInternalUserId(),
			user.getName(),
			user.getEmail(),
			user.getRoles().stream().map(InternalRole::getKey).sorted().toList()
		);

		return new AdminAuthenticationStatusResponse(true, response);
	}

	private ApiException invalidCredentials() {
		return new ApiException(
			HttpStatus.UNAUTHORIZED,
			"INVALID_ADMIN_CREDENTIALS",
			"No fue posible iniciar sesion administrativa",
			"El correo o la contrasena son incorrectos.",
			"invalid-admin-credentials"
		);
	}

	private ApiException invalidSession() {
		return new ApiException(
			HttpStatus.UNAUTHORIZED,
			"INVALID_ADMIN_SESSION",
			"La sesion administrativa no es valida",
			"No existe un usuario interno activo para la sesion actual.",
			"invalid-admin-session"
		);
	}
}
