package mx.unam.virtualinsurance.admin.cancellation;

import java.util.List;

public record AdminCancellationListResponse(
	List<AdminCancellationResponse> items
) {
	public AdminCancellationListResponse {
		items = List.copyOf(items);
	}
}
