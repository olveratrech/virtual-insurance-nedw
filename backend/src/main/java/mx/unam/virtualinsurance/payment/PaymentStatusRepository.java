package mx.unam.virtualinsurance.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Long> {

	Optional<PaymentStatus> findByKey(String key);
}

