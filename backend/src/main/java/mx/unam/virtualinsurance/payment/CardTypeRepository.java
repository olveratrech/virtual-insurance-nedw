package mx.unam.virtualinsurance.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTypeRepository extends JpaRepository<CardType, Long> {

	Optional<CardType> findByKey(String key);
}

