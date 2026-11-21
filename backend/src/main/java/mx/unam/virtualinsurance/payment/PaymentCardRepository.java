package mx.unam.virtualinsurance.payment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
}

