package mx.unam.virtualinsurance.admin.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternalUserRepository extends JpaRepository<InternalUser, Long> {

	@EntityGraph(attributePaths = "roles")
	Optional<InternalUser> findByEmail(String email);

	@EntityGraph(attributePaths = "roles")
	Optional<InternalUser> findByInternalUserId(Long internalUserId);
}
