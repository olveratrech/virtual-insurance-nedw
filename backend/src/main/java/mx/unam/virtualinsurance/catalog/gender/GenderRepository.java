package mx.unam.virtualinsurance.catalog.gender;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GenderRepository extends JpaRepository<Gender, Long> {

	List<Gender> findAllByOrderByGeneroIdAsc();

	Optional<Gender> findByKey(String key);
}
