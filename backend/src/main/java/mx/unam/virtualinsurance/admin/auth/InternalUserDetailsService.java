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
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Service
public class InternalUserDetailsService implements UserDetailsService {

	private static final Set<String> AUTHORIZED_ROLES =
		Set.of("ADMIN", "CANCELLATION_REVIEWER");

	private final InternalUserRepository internalUserRepository;

	private final Environment environment;

	public InternalUserDetailsService(InternalUserRepository internalUserRepository, Environment environment) {
		this.internalUserRepository = internalUserRepository;
		this.environment = environment;
	}

	public boolean hasCurrentAccess(Long userId) {
		return internalUserRepository.findByInternalUserId(userId)
			.filter(this::isEligible)
			.filter(user -> user.getRoles().stream().map(InternalRole::getKey).anyMatch(AUTHORIZED_ROLES::contains))
			.isPresent();
	}

	private boolean isEligible(InternalUser user) {
		return user.isActive() && !(environment.acceptsProfiles(Profiles.of("public"))
			&& "admin.cancelaciones@virtual-insurance.test".equalsIgnoreCase(user.getEmail()));
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
			isEligible(user) && !roles.isEmpty(),
			roles
		);
	}
}
