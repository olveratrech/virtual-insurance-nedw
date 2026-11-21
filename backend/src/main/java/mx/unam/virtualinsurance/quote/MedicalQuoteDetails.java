package mx.unam.virtualinsurance.quote;

public record MedicalQuoteDetails(
	Integer age,
	boolean hypertension,
	boolean glucoseOutOfRange,
	boolean cholesterolOutOfRange,
	boolean smoker,
	boolean alcoholAddiction
) {
}
