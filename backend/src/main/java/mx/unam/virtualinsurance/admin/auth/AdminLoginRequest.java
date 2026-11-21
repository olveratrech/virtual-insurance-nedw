package mx.unam.virtualinsurance.admin.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminLoginRequest(
	@NotBlank @Email @Size(max = 250) String email,
	@NotBlank @Size(max = 72) String password
) {
	public AdminLoginRequest {
		if (email != null) {
			email = email.trim();
		}
	}
}
