package mx.unam.virtualinsurance.policy;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PolicyCancellationResponse(
	Long id,
	String requestNumber,
	String status,
	String reason,
	LocalDate requestedDate,
	String comments,
	OffsetDateTime createdAt,
	OffsetDateTime resolvedAt,
	String resolutionObservation
) {
}
