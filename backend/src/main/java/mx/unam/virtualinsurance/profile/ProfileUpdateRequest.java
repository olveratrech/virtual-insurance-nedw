package mx.unam.virtualinsurance.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
	@NotBlank @Size(max = 40) String name,
	@NotBlank @Size(max = 40) String paternalSurname,
	@Size(max = 40) String maternalSurname,
	@Positive Long genderId,
	@NotBlank @Email @Size(max = 250) String email,
	@Size(max = 72) String currentPassword
) {
}
