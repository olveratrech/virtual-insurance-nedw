package mx.unam.virtualinsurance.quote;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

	Optional<Quote> findByQuoteIdAndClientClientId(Long quoteId, Long clientId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select q from Quote q where q.quoteId = :quoteId and q.client.clientId = :clientId")
	Optional<Quote> lockOwnedQuote(Long quoteId, Long clientId);

	@Query(value = "select seq_numero_cotizacion.nextval from dual", nativeQuery = true)
	Integer nextQuoteNumber();
}
