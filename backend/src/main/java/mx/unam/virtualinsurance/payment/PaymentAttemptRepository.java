package mx.unam.virtualinsurance.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

	@Query("""
		select attempt
		  from PaymentAttempt attempt
		  join fetch attempt.quote quote
		  join fetch attempt.status status
		 where attempt.idempotencyKey = :idempotencyKey
		   and quote.quoteId = :quoteId
		   and quote.client.clientId = :clientId
		""")
	Optional<PaymentAttempt> findOwnedByIdempotencyKey(String idempotencyKey, Long quoteId, Long clientId);
}

