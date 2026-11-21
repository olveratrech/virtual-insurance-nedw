package mx.unam.virtualinsurance.admin.cancellation;

public record AdminCancellationClientResponse(
	Long id,
	String fullName,
	String email
) {
}
