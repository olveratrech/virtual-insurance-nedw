package mx.unam.virtualinsurance.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class SecuritySessionService {

	private final SecurityContextRepository securityContextRepository;

	public SecuritySessionService(SecurityContextRepository securityContextRepository) {
		this.securityContextRepository = securityContextRepository;
	}

	public void authenticate(
		UserDetails principal,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		if (request.getSession(false) != null) {
			request.changeSessionId();
		}

		var authentication = UsernamePasswordAuthenticationToken.authenticated(
			principal,
			null,
			principal.getAuthorities()
		);
		var context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);
	}
}
