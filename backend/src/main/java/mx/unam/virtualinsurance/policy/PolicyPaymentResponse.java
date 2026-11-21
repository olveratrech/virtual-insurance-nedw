package mx.unam.virtualinsurance.policy;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyPaymentResponse(
	String mode,
	LocalDate nextPaymentDate,
	BigDecimal lastPaymentAmount,
	String lastPaymentStatus,
	String authorizationCode,
	String currency
) {
}

