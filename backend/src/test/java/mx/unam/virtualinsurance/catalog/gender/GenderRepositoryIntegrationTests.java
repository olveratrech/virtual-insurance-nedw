package mx.unam.virtualinsurance.catalog.gender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GenderRepositoryIntegrationTests {

	@Autowired
	private GenderRepository genderRepository;

	@Test
	void findsAllSeededGendersOrderedById() {
		var genders = genderRepository.findAllByOrderByGeneroIdAsc();

		assertThat(genders).hasSize(5);
		assertThat(genders)
			.extracting(Gender::getKey)
			.containsExactlyInAnyOrderElementsOf(List.of(
				"MUJER",
				"HOMBRE",
				"NO_BINARIO",
				"OTRO",
				"PREFIERE_NO_DECIR"
			));
		assertThat(genders)
			.extracting(Gender::getGeneroId)
			.isSorted();
	}
}
