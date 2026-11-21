package mx.unam.virtualinsurance.admin.cancellation;

import jakarta.validation.Valid;

import mx.unam.virtualinsurance.admin.auth.InternalUserPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/solicitudes-cancelacion")
public class AdminCancellationController {

	private final AdminCancellationService cancellationService;

	public AdminCancellationController(AdminCancellationService cancellationService) {
		this.cancellationService = cancellationService;
	}

	@GetMapping
	public AdminCancellationListResponse findAll(
		@RequestParam(defaultValue = "SOLICITADA") String status
	) {
		return cancellationService.findAll(status);
	}

	@GetMapping("/{cancellationId}")
	public AdminCancellationResponse findById(@PathVariable Long cancellationId) {
		return cancellationService.findById(cancellationId);
	}

	@PutMapping("/{cancellationId}/resolucion")
	public AdminCancellationResponse resolve(
		@AuthenticationPrincipal InternalUserPrincipal principal,
		@PathVariable Long cancellationId,
		@Valid @RequestBody AdminCancellationResolutionRequest request
	) {
		return cancellationService.resolve(principal.internalUserId(), cancellationId, request);
	}
}
