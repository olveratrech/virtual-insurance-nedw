package mx.unam.virtualinsurance.panel;

import mx.unam.virtualinsurance.auth.ClientPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/panel")
public class PanelController {

	private final PanelService panelService;

	public PanelController(PanelService panelService) {
		this.panelService = panelService;
	}

	@GetMapping
	public PanelResponse getPanel(@AuthenticationPrincipal ClientPrincipal principal) {
		return panelService.getPanel(principal.clientId());
	}
}
