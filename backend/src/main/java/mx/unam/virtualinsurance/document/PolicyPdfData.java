package mx.unam.virtualinsurance.document;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyPdfData(
	Long policyId,
	String policyNumber,
	String status,
	String type,
	String productName,
	String coverageName,
	String clientName,
	String insuredLabel,
	String insuredValue,
	LocalDate validFrom,
	LocalDate validUntil,
	BigDecimal annualCost,
	BigDecimal tax,
	String paymentMode,
	BigDecimal paymentAmount,
	String authorizationCode,
	String invoiceNumber,
	String cancellationNumber,
	String cancellationStatus,
	LocalDate cancellationRequestedDate,
	LocalDate cancellationResolvedDate,
	String cancellationResolutionObservation
) {
}

