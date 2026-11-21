package mx.unam.virtualinsurance.profile;

import jakarta.validation.Valid;

import mx.unam.virtualinsurance.auth.ClientPrincipal;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ProfileController {

	private final ProfileService profileService;

	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping("/cuenta/perfil")
	public ProfileResponse getProfile(@AuthenticationPrincipal ClientPrincipal principal) {
		return profileService.findProfile(principal.clientId());
	}

	@PutMapping("/cuenta/perfil")
	public ProfileResponse updateProfile(
		@AuthenticationPrincipal ClientPrincipal principal,
		@RequestBody @Valid ProfileUpdateRequest request
	) {
		return profileService.updateProfile(principal.clientId(), request);
	}

	@PutMapping(path = "/cuenta/perfil/fotografia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ProfileResponse updatePhoto(
		@AuthenticationPrincipal ClientPrincipal principal,
		@RequestPart("photo") MultipartFile photo
	) {
		return profileService.updatePhoto(principal.clientId(), photo);
	}

	@DeleteMapping("/cuenta/perfil/fotografia")
	public ProfileResponse deletePhoto(@AuthenticationPrincipal ClientPrincipal principal) {
		return profileService.deletePhoto(principal.clientId());
	}

	@GetMapping("/clientes/me/foto")
	public ResponseEntity<byte[]> getPhoto(@AuthenticationPrincipal ClientPrincipal principal) {
		ProfileService.PhotoContent photo = profileService.findPhoto(principal.clientId());
		return ResponseEntity.ok()
			.cacheControl(CacheControl.noStore())
			.contentType(MediaType.parseMediaType(photo.mediaType()))
			.body(photo.content());
	}
}
