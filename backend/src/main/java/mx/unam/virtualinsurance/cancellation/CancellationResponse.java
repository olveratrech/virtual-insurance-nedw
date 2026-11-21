package mx.unam.virtualinsurance.cancellation;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CancellationResponse(
	Long id,
	String requestNumber,
	Long policyId,
	String status,
	String reason,
	LocalDate requestedDate,
	String comments,
	OffsetDateTime createdAt,
	boolean coverageActive,
	String policyStatus
) {
}
