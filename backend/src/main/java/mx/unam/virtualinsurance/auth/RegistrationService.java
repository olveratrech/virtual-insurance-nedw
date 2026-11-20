package mx.unam.virtualinsurance.auth;

import java.time.LocalDate;
import java.util.Locale;

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
public class RegistrationService {

	private static final String PHOTO_URL = "/clientes/me/foto";

	private final ClientRepository clientRepository;
	private final GenderRepository genderRepository;
	private final PasswordEncoder passwordEncoder;
	private final PhotoValidator photoValidator;

	public RegistrationService(
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

	@Transactional
	public RegistrationResult register(RegistrationRequest request, MultipartFile photo) {
		validateAdult(request.birthDate());

		String email = request.email().trim().toLowerCase(Locale.ROOT);
		String fiscalIdentifier = request.fiscalIdentifier().trim().toUpperCase(Locale.ROOT);
		validateUniqueFields(email, fiscalIdentifier);

		Gender gender = findGender(request.genderId());
		byte[] photoContent = photoValidator.validateAndRead(photo);
		String passwordHash = passwordEncoder.encode(request.password());

		Client client = new Client(
			normalizeRequired(request.name()),
			normalizeRequired(request.paternalSurname()),
			normalizeOptional(request.maternalSurname()),
			request.birthDate(),
			request.sex(),
			fiscalIdentifier,
			email,
			passwordHash,
			photoContent,
			gender
		);

		try {
			clientRepository.saveAndFlush(client);
		} catch (DataIntegrityViolationException exception) {
			throw mapUniqueConstraint(exception);
		}

		return new RegistrationResult(
			toRegistrationResponse(client),
			new ClientPrincipal(client.getClientId(), client.getEmail(), client.getPasswordHash())
		);
	}

	private void validateAdult(LocalDate birthDate) {
		if (birthDate.isAfter(LocalDate.now().minusYears(18))) {
			throw validation("El cliente debe ser mayor de edad.");
		}
	}

	private void validateUniqueFields(String email, String fiscalIdentifier) {
		if (clientRepository.existsByEmail(email)) {
			throw conflict(
				"EMAIL_ALREADY_EXISTS",
				"Ya existe una cuenta con el correo proporcionado."
			);
		}
		if (clientRepository.existsByFiscalIdentifier(fiscalIdentifier)) {
			throw conflict(
				"FISCAL_ID_ALREADY_EXISTS",
				"Ya existe una cuenta con el identificador fiscal proporcionado."
			);
		}
	}

	private Gender findGender(Long genderId) {
		if (genderId == null) {
			return null;
		}
		return genderRepository.findById(genderId)
			.orElseThrow(() -> validation("El genero seleccionado no existe."));
	}

	private RegistrationResponse toRegistrationResponse(Client client) {
		GenderResponse gender = client.getGender() == null
			? null
			: GenderResponse.from(client.getGender());

		return new RegistrationResponse(
			client.getClientId(),
			client.getName(),
			client.getPaternalSurname(),
			client.getMaternalSurname(),
			client.getBirthDate(),
			client.getSex(),
			gender,
			client.getFiscalIdentifier(),
			client.getEmail(),
			client.hasPhoto() ? PHOTO_URL : null
		);
	}

	private String normalizeRequired(String value) {
		return value.trim();
	}

	private String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private ApiException mapUniqueConstraint(DataIntegrityViolationException exception) {
		String message = rootMessage(exception).toUpperCase(Locale.ROOT);
		if (message.contains("UK_CLIENTE_EMAIL")) {
			return conflict("EMAIL_ALREADY_EXISTS", "El correo ya fue registrado.");
		}
		if (message.contains("UK_CLIENTE_IDENTIFICADOR_FISCAL")) {
			return conflict(
				"FISCAL_ID_ALREADY_EXISTS",
				"El identificador fiscal ya fue registrado."
			);
		}
		throw exception;
	}

	private String rootMessage(Throwable exception) {
		Throwable current = exception;
		while (current.getCause() != null) {
			current = current.getCause();
		}
		return current.getMessage() == null ? "" : current.getMessage();
	}

	private ApiException validation(String detail) {
		return new ApiException(
			HttpStatus.UNPROCESSABLE_CONTENT,
			"VALIDATION_ERROR",
			"La solicitud contiene datos invalidos",
			detail,
			"validation-error"
		);
	}

	private ApiException conflict(String code, String detail) {
		return new ApiException(
			HttpStatus.CONFLICT,
			code,
			"El cliente ya existe",
			detail,
			"conflict"
		);
	}
}
