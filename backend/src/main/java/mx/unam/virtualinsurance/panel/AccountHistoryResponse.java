package mx.unam.virtualinsurance.panel;

import java.util.List;

public record AccountHistoryResponse(
	List<PanelActivityResponse> quotes,
	List<PanelActivityResponse> policies,
	List<PanelActivityResponse> payments
) {
	public AccountHistoryResponse {
		quotes = List.copyOf(quotes);
		policies = List.copyOf(policies);
		payments = List.copyOf(payments);
	}
}
