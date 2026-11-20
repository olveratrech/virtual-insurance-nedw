package mx.unam.virtualinsurance.auth;

import java.util.Locale;

import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClientUserDetailsService implements UserDetailsService {

	private final ClientRepository clientRepository;

	public ClientUserDetailsService(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		Client client = clientRepository.findByEmail(normalizedEmail)
			.orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado"));

		return new ClientPrincipal(
			client.getClientId(),
			client.getEmail(),
			client.getPasswordHash()
		);
	}
}
