package mx.unam.virtualinsurance.auth;

record RegistrationResult(
	RegistrationResponse response,
	ClientPrincipal principal
) {
}
