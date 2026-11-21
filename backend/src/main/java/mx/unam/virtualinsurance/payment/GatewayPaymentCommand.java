package mx.unam.virtualinsurance.payment;

import java.math.BigDecimal;

public record GatewayPaymentCommand(
	String cardNumber,
	int expirationMonth,
	int expirationYear,
	String securityCode,
	BigDecimal amount,
	long paymentNumber
) {
}

