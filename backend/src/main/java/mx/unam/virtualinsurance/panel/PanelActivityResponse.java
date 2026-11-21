package mx.unam.virtualinsurance.panel;

public record PanelActivityResponse(
	String id,
	String resourceType,
	Long resourceId,
	String typeLabel,
	String reference,
	String status,
	String detail,
	String destination
) {
}
