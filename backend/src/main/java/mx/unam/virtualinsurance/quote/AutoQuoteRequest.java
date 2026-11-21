package mx.unam.virtualinsurance.quote;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AutoQuoteRequest(
	@NotBlank @Size(max = 40) String brand,
	@NotBlank @Size(max = 40) String model,
	@NotNull @Min(1980) @Max(9999) Integer year,
	@NotBlank @Pattern(regexp = "SEDAN|SUV|PICKUP|OTHER") String category,
	@NotBlank @Size(max = 20) String plates,
	@NotBlank @Size(max = 40) String licenseNumber,
	@NotNull @Min(18) @Max(90) Integer driverAge,
	@NotNull @PositiveOrZero Integer negativePoints,
	@NotNull Boolean criminalRecord
) {
}
