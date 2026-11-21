package mx.unam.virtualinsurance.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentModeRepository extends JpaRepository<PaymentMode, Long> {

	Optional<PaymentMode> findByKey(String key);
}

