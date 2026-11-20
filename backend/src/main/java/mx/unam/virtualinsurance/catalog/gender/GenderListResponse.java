package mx.unam.virtualinsurance.catalog.gender;

import java.util.List;

public record GenderListResponse(List<GenderResponse> items) {

	public GenderListResponse {
		items = List.copyOf(items);
	}
}
