package mx.unam.virtualinsurance.admin.cancellation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCancellationResolutionRequest(
	@NotBlank
	@Pattern(regexp = "APROBADA|RECHAZADA")
	String decision,
	@NotBlank @Size(max = 500)
	String observation
) {
}
