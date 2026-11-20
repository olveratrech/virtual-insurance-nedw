package mx.unam.virtualinsurance.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	private final RegistrationService registrationService;
	private final AuthenticationService authenticationService;
	private final SecuritySessionService securitySessionService;

	public AuthenticationController(
		RegistrationService registrationService,
		AuthenticationService authenticationService,
		SecuritySessionService securitySessionService
	) {
		this.registrationService = registrationService;
		this.authenticationService = authenticationService;
		this.securitySessionService = securitySessionService;
	}

	@PostMapping("/registro")
	public ResponseEntity<RegistrationResponse> register(
		@RequestPart("data") @Valid RegistrationRequest data,
		@RequestPart(value = "photo", required = false) MultipartFile photo,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		RegistrationResult result = registrationService.register(data, photo);
		securitySessionService.authenticate(result.principal(), request, response);
		return ResponseEntity.status(HttpStatus.CREATED).body(result.response());
	}

	@PostMapping("/login")
	public AuthenticationStatusResponse login(
		@RequestBody @Valid LoginRequest loginRequest,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		return authenticationService.login(loginRequest, request, response);
	}

	@GetMapping("/me")
	public AuthenticationStatusResponse me(@AuthenticationPrincipal ClientPrincipal principal) {
		return authenticationService.findAuthenticatedClient(principal.clientId());
	}
}
