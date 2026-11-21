package mx.unam.virtualinsurance.panel;

import mx.unam.virtualinsurance.auth.ClientPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cuenta")
public class AccountHistoryController {

	private final AccountHistoryService accountHistoryService;

	public AccountHistoryController(AccountHistoryService accountHistoryService) {
		this.accountHistoryService = accountHistoryService;
	}

	@GetMapping("/historial")
	public AccountHistoryResponse getHistory(@AuthenticationPrincipal ClientPrincipal principal) {
		return accountHistoryService.getHistory(principal.clientId());
	}
}
