package mx.unam.virtualinsurance.catalog.gender;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GenderRepository extends JpaRepository<Gender, Long> {

	List<Gender> findAllByOrderByGeneroIdAsc();
}
