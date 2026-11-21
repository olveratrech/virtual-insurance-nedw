package mx.unam.virtualinsurance.quote;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalQuoteRepository extends JpaRepository<MedicalQuote, Long> {
}
