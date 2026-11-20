package mx.unam.virtualinsurance.auth;

public record CsrfResponse(
	String headerName,
	String parameterName,
	String token
) {
}
