package mx.unam.virtualinsurance.admin.auth;

public record AdminAuthenticationStatusResponse(
	boolean authenticated,
	InternalUserResponse user
) {
}
