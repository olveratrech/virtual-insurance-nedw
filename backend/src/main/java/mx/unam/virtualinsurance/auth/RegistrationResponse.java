package mx.unam.virtualinsurance.auth;

import java.time.LocalDate;

import mx.unam.virtualinsurance.catalog.gender.GenderResponse;

public record RegistrationResponse(
	Long id,
	String name,
	String paternalSurname,
	String maternalSurname,
	LocalDate birthDate,
	String sex,
	GenderResponse gender,
	String fiscalIdentifier,
	String email,
	String photoUrl
) {
}
