package mx.unam.virtualinsurance.admin.cancellation;

public record AdminCancellationPolicyResponse(
	Long id,
	String policyNumber,
	String status,
	String type,
	String productName
) {
}
