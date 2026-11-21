package mx.unam.virtualinsurance.profile;

import java.util.Locale;

import mx.unam.virtualinsurance.auth.PhotoValidator;
import mx.unam.virtualinsurance.catalog.gender.Gender;
import mx.unam.virtualinsurance.catalog.gender.GenderRepository;
import mx.unam.virtualinsurance.catalog.gender.GenderResponse;
import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;
import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileService {

	private static final String PHOTO_URL = "/clientes/me/foto";

	private final ClientRepository clientRepository;
	private final GenderRepository genderRepository;
	private final PasswordEncoder passwordEncoder;
	private final PhotoValidator photoValidator;

	public ProfileService(
		ClientRepository clientRepository,
		GenderRepository genderRepository,
		PasswordEncoder passwordEncoder,
		PhotoValidator photoValidator
	) {
		this.clientRepository = clientRepository;
		this.genderRepository = genderRepository;
		this.passwordEncoder = passwordEncoder;
		this.photoValidator = photoValidator;
	}

	@Transactional(readOnly = true)
	public ProfileResponse findProfile(Long clientId) {
		return toResponse(findClient(clientId));
	}

	@Transactional
	public ProfileResponse updateProfile(Long clientId, ProfileUpdateRequest request) {
		Client client = findClient(clientId);
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		boolean changesEmail = !email.equals(client.getEmail());

		if (changesEmail) {
			if (request.currentPassword() == null
				|| !passwordEncoder.matches(request.currentPassword(), client.getPasswordHash())) {
				throw invalidPassword();
			}
			if (clientRepository.existsByEmailAndClientIdNot(email, clientId)) {
				throw emailConflict();
			}
		}

		Gender gender = request.genderId() == null
			? null
			: genderRepository.findById(request.genderId())
				.orElseThrow(() -> validation("El genero seleccionado no existe."));

		client.updateProfile(
			request.name().trim(),
			request.paternalSurname().trim(),
			normalizeOptional(request.maternalSurname()),
			email,
			gender
		);

		try {
			clientRepository.flush();
		} catch (DataIntegrityViolationException exception) {
			throw emailConflict();
		}
		return toResponse(client);
	}

	@Transactional
	public ProfileResponse updatePhoto(Long clientId, MultipartFile photo) {
		byte[] content = photoValidator.validateAndRead(photo);
		if (content == null) {
			throw validation("Selecciona una fotografia JPEG o PNG.");
		}
		Client client = findClient(clientId);
		client.updatePhoto(content);
		return toResponse(client);
	}

	@Transactional
	public ProfileResponse deletePhoto(Long clientId) {
		Client client = findClient(clientId);
		client.updatePhoto(null);
		return toResponse(client);
	}

	@Transactional(readOnly = true)
	public PhotoContent findPhoto(Long clientId) {
		Client client = findClient(clientId);
		byte[] content = client.getPhoto();
		if (content == null) {
			throw new ApiException(
				HttpStatus.NOT_FOUND,
				"PROFILE_PHOTO_NOT_FOUND",
				"Fotografia no encontrada",
				"El cliente no tiene una fotografia registrada.",
				"not-found"
			);
		}
		String mediaType = content.length >= 8 && content[0] == (byte) 0x89 ? "image/png" : "image/jpeg";
		return new PhotoContent(content, mediaType);
	}

	private Client findClient(Long clientId) {
		return clientRepository.findById(clientId).orElseThrow(() -> new ApiException(
			HttpStatus.UNAUTHORIZED,
			"INVALID_SESSION",
			"La sesion no es valida",
			"No existe un cliente para la sesion actual.",
			"invalid-session"
		));
	}

	private ProfileResponse toResponse(Client client) {
		return new ProfileResponse(
			client.getClientId(),
			client.getName(),
			client.getPaternalSurname(),
			client.getMaternalSurname(),
			client.getBirthDate(),
			client.getSex(),
			client.getGender() == null ? null : GenderResponse.from(client.getGender()),
			client.getFiscalIdentifier(),
			client.getEmail(),
			client.hasPhoto() ? PHOTO_URL : null
		);
	}

	private String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private ApiException validation(String detail) {
		return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "VALIDATION_ERROR", "Datos invalidos", detail, "validation-error");
	}

	private ApiException invalidPassword() {
		return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CURRENT_PASSWORD", "No fue posible actualizar el correo", "La contrasena actual es incorrecta.", "invalid-credentials");
	}

	private ApiException emailConflict() {
		return new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "El correo ya existe", "Ya existe una cuenta con el correo proporcionado.", "conflict");
	}

	public record PhotoContent(byte[] content, String mediaType) {
		public PhotoContent {
			content = content.clone();
		}
	}
}
