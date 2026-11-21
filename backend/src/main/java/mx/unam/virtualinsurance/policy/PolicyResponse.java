package mx.unam.virtualinsurance.policy;

import java.time.LocalDate;
import java.util.List;

public record PolicyResponse(
	Long id,
	String policyNumber,
	String status,
	boolean coverageActive,
	String type,
	String productName,
	String coverageName,
	LocalDate validFrom,
	LocalDate validUntil,
	PolicyInsuredResponse insured,
	PolicyPaymentResponse payment,
	List<PolicyDocumentResponse> documents
) {
	public PolicyResponse {
		documents = List.copyOf(documents);
	}
}

