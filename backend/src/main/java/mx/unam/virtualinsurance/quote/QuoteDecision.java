package mx.unam.virtualinsurance.quote;

import java.math.BigDecimal;
import java.util.List;

public record QuoteDecision(
	boolean approved,
	BigDecimal annualCost,
	String rejectionReason,
	String productName,
	String coverageName,
	List<String> benefits
) {
	public QuoteDecision {
		benefits = List.copyOf(benefits);
	}

	public static QuoteDecision approved(
		BigDecimal annualCost,
		String productName,
		String coverageName,
		List<String> benefits
	) {
		return new QuoteDecision(
			true,
			annualCost,
			null,
			productName,
			coverageName,
			benefits
		);
	}

	public static QuoteDecision rejected(
		String rejectionReason,
		String productName,
		String coverageName
	) {
		return new QuoteDecision(
			false,
			null,
			rejectionReason,
			productName,
			coverageName,
			List.of()
		);
	}
}
