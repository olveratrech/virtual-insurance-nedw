package mx.unam.virtualinsurance.quote;

public record AutoQuoteDetails(
	String brand,
	String model,
	Integer year,
	String category,
	String plates,
	String licenseNumber,
	Integer driverAge,
	Integer negativePoints,
	boolean criminalRecord
) {
}
