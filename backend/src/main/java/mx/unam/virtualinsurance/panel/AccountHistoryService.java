package mx.unam.virtualinsurance.panel;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountHistoryService {

	private final PanelRepository panelRepository;
	private final PanelService panelService;

	public AccountHistoryService(PanelRepository panelRepository, PanelService panelService) {
		this.panelRepository = panelRepository;
		this.panelService = panelService;
	}

	@Transactional(readOnly = true)
	public AccountHistoryResponse getHistory(Long clientId) {
		List<PanelActivityResponse> activity = panelRepository.findRecentActivity(clientId)
			.stream()
			.map(panelService::mapActivity)
			.toList();
		return new AccountHistoryResponse(
			byType(activity, "QUOTE"),
			byType(activity, "POLICY"),
			byType(activity, "PAYMENT")
		);
	}

	private List<PanelActivityResponse> byType(List<PanelActivityResponse> activity, String type) {
		return activity.stream().filter(item -> type.equals(item.resourceType())).toList();
	}
}
