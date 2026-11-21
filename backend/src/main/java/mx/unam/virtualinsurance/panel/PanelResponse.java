package mx.unam.virtualinsurance.panel;

import java.util.List;

public record PanelResponse(
	String clientName,
	List<PanelActivityResponse> recentActivity
) {
	public PanelResponse {
		recentActivity = List.copyOf(recentActivity);
	}
}
