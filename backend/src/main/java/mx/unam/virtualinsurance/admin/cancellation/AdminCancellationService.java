package mx.unam.virtualinsurance.admin.cancellation;

import java.util.Locale;
import java.util.Set;

import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCancellationService {

	private static final Set<String> VALID_STATUSES =
		Set.of("SOLICITADA", "APROBADA", "RECHAZADA");

	private final AdminCancellationRepository cancellationRepository;

	public AdminCancellationService(AdminCancellationRepository cancellationRepository) {
		this.cancellationRepository = cancellationRepository;
	}

	@Transactional(readOnly = true)
	public AdminCancellationListResponse findAll(String requestedStatus) {
		String status = normalizeStatus(requestedStatus);
		return new AdminCancellationListResponse(cancellationRepository.findAllByStatus(status));
	}

	@Transactional(readOnly = true)
	public AdminCancellationResponse findById(Long cancellationId) {
		return cancellationRepository.findById(cancellationId)
			.orElseThrow(this::cancellationNotFound);
	}

	@Transactional
	public AdminCancellationResponse resolve(
		Long internalUserId,
		Long cancellationId,
		AdminCancellationResolutionRequest request
	) {
		String currentStatus = cancellationRepository.findStatusForUpdate(cancellationId)
			.orElseThrow(this::cancellationNotFound);

		if (!"SOLICITADA".equals(currentStatus)) {
			throw new ApiException(
				HttpStatus.CONFLICT,
				"CANCELLATION_ALREADY_RESOLVED",
				"La solicitud ya fue resuelta",
				"Una solicitud de cancelacion solo puede resolverse una vez.",
				"conflict"
			);
		}

		cancellationRepository.resolve(
			cancellationId,
			request.decision(),
			request.observation().trim(),
			internalUserId
		);
		return cancellationRepository.findById(cancellationId)
			.orElseThrow(this::cancellationNotFound);
	}

	private String normalizeStatus(String requestedStatus) {
		String status = requestedStatus.trim().toUpperCase(Locale.ROOT);
		if (!VALID_STATUSES.contains(status)) {
			throw new ApiException(
				HttpStatus.UNPROCESSABLE_CONTENT,
				"VALIDATION_ERROR",
				"El filtro no es valido",
				"El estatus debe ser SOLICITADA, APROBADA o RECHAZADA.",
				"validation-error"
			);
		}
		return status;
	}

	private ApiException cancellationNotFound() {
		return new ApiException(
			HttpStatus.NOT_FOUND,
			"CANCELLATION_REQUEST_NOT_FOUND",
			"La solicitud no existe",
			"No existe una solicitud de cancelacion con el identificador proporcionado.",
			"not-found"
		);
	}
}
