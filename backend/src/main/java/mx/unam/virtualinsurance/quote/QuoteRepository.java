package mx.unam.virtualinsurance.quote;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

	Optional<Quote> findByQuoteIdAndClientClientId(Long quoteId, Long clientId);

	@Query(value = "select seq_numero_cotizacion.nextval from dual", nativeQuery = true)
	Integer nextQuoteNumber();
}
