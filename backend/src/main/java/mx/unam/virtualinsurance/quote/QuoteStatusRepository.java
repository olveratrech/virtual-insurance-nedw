package mx.unam.virtualinsurance.quote;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteStatusRepository extends JpaRepository<QuoteStatus, Long> {

	Optional<QuoteStatus> findByKey(String key);
}
