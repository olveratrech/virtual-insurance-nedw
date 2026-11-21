package mx.unam.virtualinsurance.profile;

import java.time.LocalDate;

import mx.unam.virtualinsurance.catalog.gender.GenderResponse;

public record ProfileResponse(
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
