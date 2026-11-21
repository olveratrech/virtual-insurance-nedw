package mx.unam.virtualinsurance.admin.cancellation;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AdminCancellationResponse(
	Long id,
	String requestNumber,
	String status,
	String reason,
	LocalDate requestedDate,
	String comments,
	OffsetDateTime createdAt,
	AdminCancellationPolicyResponse policy,
	AdminCancellationClientResponse client,
	AdminCancellationResolutionResponse resolution
) {
}
