package mx.unam.virtualinsurance.payment;

public record GatewayPaymentResult(
	boolean approved,
	String responseCode,
	String publicMessage,
	String authorizationCode,
	String lastFour
) {
	public static GatewayPaymentResult approved(String authorizationCode, String lastFour) {
		return new GatewayPaymentResult(
			true,
			"APPROVED",
			"El pago fue aprobado por la pasarela academica.",
			authorizationCode,
			lastFour
		);
	}

	public static GatewayPaymentResult rejected(String code, String message, String lastFour) {
		return new GatewayPaymentResult(false, code, message, null, lastFour);
	}
}

