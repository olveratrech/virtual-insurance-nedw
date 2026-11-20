package mx.unam.virtualinsurance.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record ClientPrincipal(
	Long clientId,
	String email,
	String passwordHash
) implements UserDetails {

	private static final List<GrantedAuthority> AUTHORITIES =
		List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AUTHORITIES;
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}
}
