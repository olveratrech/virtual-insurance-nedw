package mx.unam.virtualinsurance.cancellation;

import java.time.OffsetDateTime;
import java.util.UUID;

import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancellationService {

	private final CancellationRepository cancellationRepository;

	public CancellationService(CancellationRepository cancellationRepository) {
		this.cancellationRepository = cancellationRepository;
	}

	@Transactional
	public CancellationResponse requestCancellation(
		Long clientId,
		Long policyId,
		CancellationRequest request
	) {
		CancellationRepository.OwnedPolicy policy = cancellationRepository
			.findOwnedPolicyForUpdate(policyId, clientId)
			.orElseThrow(this::policyNotFound);

		if (cancellationRepository.hasPendingRequest(policy.id())) {
			throw cancellationAlreadyPending();
		}
		if (!"ACTIVA".equals(policy.status())) {
			throw new ApiException(
				HttpStatus.CONFLICT,
				"POLICY_NOT_ACTIVE",
				"La poliza no esta activa",
				"Solo una poliza activa puede recibir una solicitud de cancelacion.",
				"conflict"
			);
		}

		String requestNumber = "CAN-" + UUID.randomUUID()
			.toString().replace("-", "").substring(0, 16).toUpperCase();
		OffsetDateTime createdAt = OffsetDateTime.now();
		Long cancellationId = cancellationRepository.create(
			policy.id(),
			requestNumber,
			request.reason(),
			request.requestedDate(),
			normalizeComments(request.comments()),
			createdAt
		);

		return new CancellationResponse(
			cancellationId,
			requestNumber,
			policy.id(),
			"SOLICITADA",
			request.reason(),
			request.requestedDate(),
			normalizeComments(request.comments()),
			createdAt,
			true,
			"CANCELACION_SOLICITADA"
		);
	}

	private String normalizeComments(String comments) {
		return comments == null || comments.isBlank() ? null : comments.trim();
	}

	private ApiException policyNotFound() {
		return new ApiException(
			HttpStatus.NOT_FOUND,
			"POLICY_NOT_FOUND",
			"La poliza no existe",
			"No existe una poliza accesible con el identificador proporcionado.",
			"not-found"
		);
	}

	private ApiException cancellationAlreadyPending() {
		return new ApiException(
			HttpStatus.CONFLICT,
			"CANCELLATION_ALREADY_PENDING",
			"La cancelacion ya fue solicitada",
			"La poliza ya tiene una solicitud de cancelacion pendiente.",
			"conflict"
		);
	}
}
