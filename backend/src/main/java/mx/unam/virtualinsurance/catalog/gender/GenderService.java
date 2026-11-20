package mx.unam.virtualinsurance.catalog.gender;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenderService {

	private final GenderRepository genderRepository;

	public GenderService(GenderRepository genderRepository) {
		this.genderRepository = genderRepository;
	}

	@Transactional(readOnly = true)
	public GenderListResponse findAll() {
		var items = genderRepository.findAllByOrderByGeneroIdAsc()
			.stream()
			.map(GenderResponse::from)
			.toList();

		return new GenderListResponse(items);
	}
}
