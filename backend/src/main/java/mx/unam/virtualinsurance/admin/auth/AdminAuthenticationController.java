package mx.unam.virtualinsurance.admin.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthenticationController {

	private final AdminAuthenticationService authenticationService;

	public AdminAuthenticationController(AdminAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/login")
	public AdminAuthenticationStatusResponse login(
		@RequestBody @Valid AdminLoginRequest loginRequest,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		return authenticationService.login(loginRequest, request, response);
	}

	@GetMapping("/me")
	public AdminAuthenticationStatusResponse me(
		@AuthenticationPrincipal InternalUserPrincipal principal
	) {
		return authenticationService.findAuthenticatedUser(principal.internalUserId());
	}
}
