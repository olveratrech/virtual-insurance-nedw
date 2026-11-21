package mx.unam.virtualinsurance.quote;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MedicalQuoteRequest(
	@NotNull @Min(18) @Max(100) Integer age,
	@NotNull Boolean hypertension,
	@NotNull Boolean glucoseOutOfRange,
	@NotNull Boolean cholesterolOutOfRange,
	@NotNull Boolean smoker,
	@NotNull Boolean alcoholAddiction
) {
}
