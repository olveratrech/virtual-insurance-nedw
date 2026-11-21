package mx.unam.virtualinsurance.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
	Long attemptId,
	String status,
	String responseCode,
	String message,
	String authorizationCode,
	String lastFour,
	BigDecimal amount,
	String currency,
	Long policyId,
	String policyNumber,
	LocalDate coverageStart,
	LocalDate coverageEnd
) {
}

