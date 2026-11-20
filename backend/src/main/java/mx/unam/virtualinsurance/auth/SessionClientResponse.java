package mx.unam.virtualinsurance.auth;

public record SessionClientResponse(
	Long id,
	String name,
	String paternalSurname,
	String maternalSurname,
	String email,
	String photoUrl
) {
}
