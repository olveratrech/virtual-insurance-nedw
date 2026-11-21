package mx.unam.virtualinsurance.policy;

import mx.unam.virtualinsurance.auth.ClientPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/polizas")
public class PolicyController {

	private final PolicyService policyService;

	public PolicyController(PolicyService policyService) {
		this.policyService = policyService;
	}

	@GetMapping("/{policyId}")
	public PolicyResponse findPolicy(
		@AuthenticationPrincipal ClientPrincipal principal,
		@PathVariable Long policyId
	) {
		return policyService.findPolicy(principal.clientId(), policyId);
	}
}

