package mx.unam.virtualinsurance.catalog.gender;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalogos/generos")
public class GenderController {

	private final GenderService genderService;

	public GenderController(GenderService genderService) {
		this.genderService = genderService;
	}

	@GetMapping
	public GenderListResponse findAll() {
		return genderService.findAll();
	}
}
