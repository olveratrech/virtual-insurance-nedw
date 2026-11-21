package mx.unam.virtualinsurance.payment;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
	@NotNull @FutureOrPresent LocalDate coverageStart,
	@NotBlank @Pattern(regexp = "MONTHLY|ANNUAL") String paymentMode,
	@NotBlank @Size(max = 100) String cardholderName,
	@NotBlank @Pattern(regexp = "\\d{16}") String cardNumber,
	@NotBlank @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}") String expiration,
	@NotBlank @Pattern(regexp = "\\d{3}") String cvv,
	@AssertTrue boolean acceptsTerms
) {
}

