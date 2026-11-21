package mx.unam.virtualinsurance.quote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;

import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;
import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteService {

	private static final String APPROVED = "APROBADA";
	private static final String REJECTED = "RECHAZADA";
	private static final String AUTO = "A";
	private static final String MEDICAL = "M";

	private final QuoteRepository quoteRepository;
	private final AutoQuoteRepository autoQuoteRepository;
	private final MedicalQuoteRepository medicalQuoteRepository;
	private final QuoteStatusRepository quoteStatusRepository;
	private final ClientRepository clientRepository;
	private final AutoQuoteStrategy autoQuoteStrategy;
	private final MedicalQuoteStrategy medicalQuoteStrategy;
	private final JdbcTemplate jdbcTemplate;

	public QuoteService(
		QuoteRepository quoteRepository,
		AutoQuoteRepository autoQuoteRepository,
		MedicalQuoteRepository medicalQuoteRepository,
		QuoteStatusRepository quoteStatusRepository,
		ClientRepository clientRepository,
		AutoQuoteStrategy autoQuoteStrategy,
		MedicalQuoteStrategy medicalQuoteStrategy,
		JdbcTemplate jdbcTemplate
	) {
		this.quoteRepository = quoteRepository;
		this.autoQuoteRepository = autoQuoteRepository;
		this.medicalQuoteRepository = medicalQuoteRepository;
		this.quoteStatusRepository = quoteStatusRepository;
		this.clientRepository = clientRepository;
		this.autoQuoteStrategy = autoQuoteStrategy;
		this.medicalQuoteStrategy = medicalQuoteStrategy;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public QuoteResponse createAutoQuote(Long clientId, AutoQuoteRequest request) {
		validateVehicleYear(request.year());
		QuoteDecision decision = autoQuoteStrategy.evaluate(request);
		Quote quote = createQuote(clientId, AUTO, decision);

		AutoQuote autoQuote = new AutoQuote(
			quote.getQuoteId(),
			normalize(request.brand()),
			normalize(request.model()),
			request.year(),
			request.category(),
			normalizeUppercase(request.plates()),
			normalizeUppercase(request.licenseNumber()),
			request.driverAge(),
			request.negativePoints(),
			request.criminalRecord()
		);
		autoQuoteRepository.saveAndFlush(autoQuote);

		return toResponse(quote, decision, toDetails(autoQuote));
	}

	@Transactional
	public QuoteResponse createMedicalQuote(Long clientId, MedicalQuoteRequest request) {
		QuoteDecision decision = medicalQuoteStrategy.evaluate(request);
		Quote quote = createQuote(clientId, MEDICAL, decision);

		MedicalQuote medicalQuote = new MedicalQuote(
			quote.getQuoteId(),
			request.age(),
			request.hypertension(),
			request.glucoseOutOfRange(),
			request.cholesterolOutOfRange(),
			request.smoker(),
			request.alcoholAddiction()
		);
		medicalQuoteRepository.saveAndFlush(medicalQuote);

		return toResponse(quote, decision, toDetails(medicalQuote));
	}

	@Transactional(readOnly = true)
	public QuoteResponse findQuote(Long clientId, Long quoteId) {
		Quote quote = quoteRepository.findByQuoteIdAndClientClientId(quoteId, clientId)
			.orElseThrow(this::quoteNotFound);

		if (AUTO.equals(quote.getInsuranceType())) {
			AutoQuote autoQuote = autoQuoteRepository.findById(quoteId)
				.orElseThrow(() -> incompleteSubtype(quoteId));
			AutoQuoteRequest request = new AutoQuoteRequest(
				autoQuote.getBrand(),
				autoQuote.getModel(),
				autoQuote.getYear(),
				autoQuote.getCategory(),
				autoQuote.getPlates(),
				autoQuote.getLicenseNumber(),
				autoQuote.getDriverAge(),
				autoQuote.getNegativePoints(),
				autoQuote.hasCriminalRecord()
			);
			return toResponse(quote, autoQuoteStrategy.evaluate(request), toDetails(autoQuote));
		}

		MedicalQuote medicalQuote = medicalQuoteRepository.findById(quoteId)
			.orElseThrow(() -> incompleteSubtype(quoteId));
		MedicalQuoteRequest request = new MedicalQuoteRequest(
			medicalQuote.getAge(),
			medicalQuote.hasHypertension(),
			medicalQuote.hasGlucoseOutOfRange(),
			medicalQuote.hasCholesterolOutOfRange(),
			medicalQuote.isSmoker(),
			medicalQuote.hasAlcoholAddiction()
		);
		return toResponse(quote, medicalQuoteStrategy.evaluate(request), toDetails(medicalQuote));
	}

	private Quote createQuote(Long clientId, String insuranceType, QuoteDecision decision) {
		Client client = clientRepository.findById(clientId)
			.orElseThrow(this::invalidSession);
		String statusKey = decision.approved() ? APPROVED : REJECTED;
		QuoteStatus status = quoteStatusRepository.findByKey(statusKey)
			.orElseThrow(() -> new IllegalStateException("Falta el estatus de cotizacion: " + statusKey));
		OffsetDateTime quotedAt = OffsetDateTime.now();

		Quote quote = new Quote(
			client,
			quoteRepository.nextQuoteNumber(),
			quotedAt,
			LocalDate.from(quotedAt).plusDays(30),
			insuranceType,
			decision.annualCost(),
			status
		);

		return quoteRepository.saveAndFlush(quote);
	}

	private QuoteResponse toResponse(Quote quote, QuoteDecision decision, Object details) {
		BigDecimal annualCost = quote.getCalculatedCost();
		BigDecimal monthlyCost = annualCost == null
			? null
			: annualCost.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
		ContractedPolicy contractedPolicy = findContractedPolicy(quote.getQuoteId());

		return new QuoteResponse(
			quote.getQuoteId(),
			String.format("%04d", quote.getQuoteNumber()),
			AUTO.equals(quote.getInsuranceType()) ? "AUTO" : "MEDICAL",
			quote.getStatus().getKey(),
			quote.getQuotedAt(),
			quote.getValidUntil(),
			decision.productName(),
			decision.coverageName(),
			annualCost,
			monthlyCost,
			"MXN",
			decision.approved() ? decision.benefits() : java.util.List.of(),
			decision.approved() ? null : decision.rejectionReason(),
			contractedPolicy != null,
			contractedPolicy == null ? null : contractedPolicy.policyId(),
			contractedPolicy == null ? null : contractedPolicy.status(),
			details
		);
	}

	private ContractedPolicy findContractedPolicy(Long quoteId) {
		return jdbcTemplate.query(
			"""
			select p.poliza_id, ep.clave
			  from poliza p
			  join intento_pago i on i.intento_pago_id = p.intento_pago_id
			  join estatus_poliza ep on ep.estatus_poliza_id = p.estatus_poliza_id
			 where i.cotizacion_id = ?
			""",
			(rs, rowNumber) -> new ContractedPolicy(rs.getLong("poliza_id"), rs.getString("clave")),
			quoteId
		).stream().findFirst().orElse(null);
	}

	private record ContractedPolicy(Long policyId, String status) {
	}

	private AutoQuoteDetails toDetails(AutoQuote quote) {
		return new AutoQuoteDetails(
			quote.getBrand(),
			quote.getModel(),
			quote.getYear(),
			quote.getCategory(),
			quote.getPlates(),
			quote.getLicenseNumber(),
			quote.getDriverAge(),
			quote.getNegativePoints(),
			quote.hasCriminalRecord()
		);
	}

	private MedicalQuoteDetails toDetails(MedicalQuote quote) {
		return new MedicalQuoteDetails(
			quote.getAge(),
			quote.hasHypertension(),
			quote.hasGlucoseOutOfRange(),
			quote.hasCholesterolOutOfRange(),
			quote.isSmoker(),
			quote.hasAlcoholAddiction()
		);
	}

	private void validateVehicleYear(Integer year) {
		if (year > Year.now().getValue() + 1) {
			throw new ApiException(
				HttpStatus.UNPROCESSABLE_CONTENT,
				"VALIDATION_ERROR",
				"La solicitud contiene datos invalidos",
				"El ano del vehiculo no puede ser posterior al siguiente ano calendario.",
				"validation-error"
			);
		}
	}

	private String normalize(String value) {
		return value.trim();
	}

	private String normalizeUppercase(String value) {
		return value.trim().toUpperCase(java.util.Locale.ROOT);
	}

	private ApiException quoteNotFound() {
		return new ApiException(
			HttpStatus.NOT_FOUND,
			"QUOTE_NOT_FOUND",
			"La cotizacion no existe",
			"No existe una cotizacion accesible con el identificador proporcionado.",
			"not-found"
		);
	}

	private ApiException invalidSession() {
		return new ApiException(
			HttpStatus.UNAUTHORIZED,
			"INVALID_SESSION",
			"La sesion no es valida",
			"No existe un cliente para la sesion actual.",
			"invalid-session"
		);
	}

	private IllegalStateException incompleteSubtype(Long quoteId) {
		return new IllegalStateException("La cotizacion " + quoteId + " no tiene el subtipo esperado.");
	}
}
