package mx.unam.virtualinsurance.admin.cancellation;

import java.time.OffsetDateTime;

public record AdminCancellationResolutionResponse(
	OffsetDateTime resolvedAt,
	String observation,
	Long resolvedById,
	String resolvedByName
) {
}
