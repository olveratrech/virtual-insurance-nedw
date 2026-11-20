package mx.unam.virtualinsurance.auth;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/csrf")
public class CsrfController {

	@GetMapping
	public CsrfResponse csrf(CsrfToken csrfToken) {
		return new CsrfResponse(
			csrfToken.getHeaderName(),
			csrfToken.getParameterName(),
			csrfToken.getToken()
		);
	}
}
