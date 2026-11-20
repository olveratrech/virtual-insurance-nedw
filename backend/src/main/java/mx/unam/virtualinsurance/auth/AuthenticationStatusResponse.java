package mx.unam.virtualinsurance.auth;

public record AuthenticationStatusResponse(
	boolean authenticated,
	SessionClientResponse client
) {
}
