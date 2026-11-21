package mx.unam.virtualinsurance.admin.auth;

import java.util.List;

public record InternalUserResponse(
	Long id,
	String name,
	String email,
	List<String> roles
) {
}
