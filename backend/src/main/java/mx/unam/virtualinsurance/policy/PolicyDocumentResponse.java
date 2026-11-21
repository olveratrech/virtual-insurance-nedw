package mx.unam.virtualinsurance.policy;

public record PolicyDocumentResponse(
	Long id,
	String type,
	String name,
	String format,
	String downloadUrl
) {
}

