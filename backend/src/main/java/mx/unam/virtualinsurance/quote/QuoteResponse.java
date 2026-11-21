package mx.unam.virtualinsurance.quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record QuoteResponse(
	Long id,
	String quoteNumber,
	String type,
	String status,
	OffsetDateTime quotedAt,
	LocalDate validUntil,
	String productName,
	String coverageName,
	BigDecimal annualCost,
	BigDecimal monthlyCost,
	String currency,
	List<String> benefits,
	String rejectionReason,
	Object details
) {
	public QuoteResponse {
		benefits = List.copyOf(benefits);
	}
}
