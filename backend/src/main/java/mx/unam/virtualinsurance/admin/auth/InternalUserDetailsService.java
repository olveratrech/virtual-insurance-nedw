package mx.unam.virtualinsurance.admin.auth;

import java.util.Locale;
import java.util.Set;

import mx.unam.virtualinsurance.admin.user.InternalRole;
import mx.unam.virtualinsurance.admin.user.InternalUser;
import mx.unam.virtualinsurance.admin.user.InternalUserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class InternalUserDetailsService implements UserDetailsService {

	private static final Set<String> AUTHORIZED_ROLES =
		Set.of("ADMIN", "CANCELLATION_REVIEWER");

	private final InternalUserRepository internalUserRepository;

	public InternalUserDetailsService(InternalUserRepository internalUserRepository) {
		this.internalUserRepository = internalUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		InternalUser user = internalUserRepository.findByEmail(normalizedEmail)
			.orElseThrow(() -> new UsernameNotFoundException("Usuario interno no encontrado"));
		var roles = user.getRoles().stream()
			.map(InternalRole::getKey)
			.filter(AUTHORIZED_ROLES::contains)
			.sorted()
			.toList();

		return new InternalUserPrincipal(
			user.getInternalUserId(),
			user.getName(),
			user.getEmail(),
			user.getPasswordHash(),
			user.isActive() && !roles.isEmpty(),
			roles
		);
	}
}
