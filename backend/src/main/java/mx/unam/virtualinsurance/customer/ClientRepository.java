package mx.unam.virtualinsurance.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndClientIdNot(String email, Long clientId);

	boolean existsByFiscalIdentifier(String fiscalIdentifier);

	Optional<Client> findByEmail(String email);
}
