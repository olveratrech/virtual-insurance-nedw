package mx.unam.virtualinsurance.cancellation;

import jakarta.validation.Valid;
import mx.unam.virtualinsurance.auth.ClientPrincipal;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/polizas/{policyId}/solicitudes-cancelacion")
public class CancellationController {

	private final CancellationService cancellationService;

	public CancellationController(CancellationService cancellationService) {
		this.cancellationService = cancellationService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CancellationResponse requestCancellation(
		@AuthenticationPrincipal ClientPrincipal principal,
		@PathVariable Long policyId,
		@Valid @RequestBody CancellationRequest request
	) {
		return cancellationService.requestCancellation(principal.clientId(), policyId, request);
	}
}
