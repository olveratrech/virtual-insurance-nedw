package mx.unam.virtualinsurance.auth;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
	@NotBlank @Size(max = 40) String name,
	@NotBlank @Size(max = 40) String paternalSurname,
	@Size(max = 40) String maternalSurname,
	@NotNull @Past LocalDate birthDate,
	@NotBlank @Pattern(regexp = "M|F|X") String sex,
	@Positive Long genderId,
	@NotBlank @Size(max = 40) String fiscalIdentifier,
	@NotBlank @Email @Size(max = 250) String email,
	@NotBlank @Size(min = 8, max = 72) String password,
	@AssertTrue boolean acceptsTerms
) {
}
