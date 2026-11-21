package mx.unam.virtualinsurance.cancellation;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CancellationRequest(
	@NotBlank
	@Pattern(regexp = "NOT_NEEDED|FINANCIAL|SERVICE|OTHER")
	String reason,
	@NotNull @FutureOrPresent LocalDate requestedDate,
	@Size(max = 500) String comments,
	@NotNull @AssertTrue Boolean confirmed
) {
}
