package mx.unam.virtualinsurance.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;
import mx.unam.virtualinsurance.quote.Quote;
import mx.unam.virtualinsurance.quote.QuoteRepository;
import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

	private static final String PENDING = "PENDIENTE";
	private static final String APPROVED = "APROBADO";
	private static final String REJECTED = "RECHAZADO";

	private final QuoteRepository quoteRepository;
	private final ClientRepository clientRepository;
	private final PaymentAttemptRepository attemptRepository;
	private final PaymentStatusRepository statusRepository;
	private final PaymentModeRepository modeRepository;
	private final PaymentCardRepository cardRepository;
	private final CardTypeRepository cardTypeRepository;
	private final PaymentGateway paymentGateway;
	private final CardCryptoService cardCryptoService;
	private final JdbcTemplate jdbcTemplate;

	public PaymentService(
		QuoteRepository quoteRepository,
		ClientRepository clientRepository,
		PaymentAttemptRepository attemptRepository,
		PaymentStatusRepository statusRepository,
		PaymentModeRepository modeRepository,
		PaymentCardRepository cardRepository,
		CardTypeRepository cardTypeRepository,
		PaymentGateway paymentGateway,
		CardCryptoService cardCryptoService,
		JdbcTemplate jdbcTemplate
	) {
		this.quoteRepository = quoteRepository;
		this.clientRepository = clientRepository;
		this.attemptRepository = attemptRepository;
		this.statusRepository = statusRepository;
		this.modeRepository = modeRepository;
		this.cardRepository = cardRepository;
		this.cardTypeRepository = cardTypeRepository;
		this.paymentGateway = paymentGateway;
		this.cardCryptoService = cardCryptoService;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public synchronized PaymentResponse pay(
		Long clientId,
		Long quoteId,
		String idempotencyKey,
		PaymentRequest request
	) {
		validateIdempotencyKey(idempotencyKey);
		PaymentAttempt previous = attemptRepository
			.findOwnedByIdempotencyKey(idempotencyKey, clientId)
			.orElse(null);
		if (previous != null) {
			return responseFromExisting(previous, request.coverageStart());
		}

		Quote quote = quoteRepository.findByQuoteIdAndClientClientId(quoteId, clientId)
			.orElseThrow(this::quoteNotFound);
		validatePayableQuote(quote, request.coverageStart());
		if (policyExistsForQuote(quoteId)) {
			throw problem(
				HttpStatus.CONFLICT,
				"QUOTE_ALREADY_PAID",
				"La cotizacion ya fue contratada",
				"La cotizacion ya tiene una poliza emitida.",
				"conflict"
			);
		}

		Client client = clientRepository.findById(clientId).orElseThrow(this::invalidSession);
		PaymentMode mode = modeRepository.findByKey(request.paymentMode())
			.orElseThrow(() -> new IllegalStateException("Falta la modalidad de pago."));
		PaymentStatus pending = status(PENDING);
		int expirationMonth = Integer.parseInt(request.expiration().substring(0, 2));
		int expirationYear = 2000 + Integer.parseInt(request.expiration().substring(3, 5));
		PaymentCard card = cardRepository.saveAndFlush(new PaymentCard(
			client,
			cardTypeRepository.findByKey("CREDIT")
				.orElseThrow(() -> new IllegalStateException("Falta el tipo de tarjeta CREDIT.")),
			cardCryptoService.encrypt(request.cardNumber()),
			expirationYear,
			expirationMonth
		));

		BigDecimal amount = calculateAmount(quote, request.paymentMode());
		long paymentNumber = jdbcTemplate.queryForObject(
			"select seq_numero_pago.nextval from dual",
			Long.class
		);
		PaymentAttempt attempt = attemptRepository.saveAndFlush(new PaymentAttempt(
			quote,
			card,
			mode,
			paymentNumber,
			OffsetDateTime.now(),
			amount,
			pending,
			idempotencyKey
		));

		GatewayPaymentResult gatewayResult = paymentGateway.authorize(new GatewayPaymentCommand(
			request.cardNumber(),
			expirationMonth,
			expirationYear,
			request.cvv(),
			amount,
			paymentNumber
		));

		if (!gatewayResult.approved()) {
			attempt.reject(status(REJECTED), gatewayResult);
			attemptRepository.saveAndFlush(attempt);
			return toRejectedResponse(attempt, gatewayResult, request.coverageStart());
		}

		byte[] receipt = createAcademicPdf(
			"Comprobante de pago " + paymentNumber + " - " + gatewayResult.authorizationCode()
		);
		attempt.approve(status(APPROVED), receipt, gatewayResult);
		attemptRepository.saveAndFlush(attempt);

		PolicyResult policy = issuePolicy(attempt, quote, mode, request.coverageStart(), receipt);
		return new PaymentResponse(
			attempt.getId(),
			APPROVED,
			gatewayResult.responseCode(),
			gatewayResult.publicMessage(),
			gatewayResult.authorizationCode(),
			gatewayResult.lastFour(),
			amount,
			"MXN",
			policy.id(),
			policy.number(),
			policy.coverageStart(),
			policy.coverageEnd()
		);
	}

	private PolicyResult issuePolicy(
		PaymentAttempt attempt,
		Quote quote,
		PaymentMode mode,
		LocalDate coverageStart,
		byte[] receipt
	) {
		long sequence = jdbcTemplate.queryForObject(
			"select seq_folio_poliza.nextval from dual",
			Long.class
		);
		String policyNumber = "VI-%d-%06d".formatted(coverageStart.getYear(), sequence);
		String invoiceNumber = "FAC-%d-%06d".formatted(coverageStart.getYear(), sequence);
		LocalDate coverageEnd = coverageStart.plusYears(1);
		BigDecimal annualCost = quote.getCalculatedCost();
		BigDecimal tax = annualCost.multiply(new BigDecimal("0.16"))
			.setScale(2, RoundingMode.HALF_UP);
		Long activeStatusId = jdbcTemplate.queryForObject(
			"select estatus_poliza_id from estatus_poliza where clave = 'ACTIVA'",
			Long.class
		);
		String invoiceJson = "{\"folio\":\"%s\",\"moneda\":\"MXN\",\"total\":%s}"
			.formatted(invoiceNumber, annualCost.toPlainString());
		String invoiceXml = "<factura><folio>%s</folio><moneda>MXN</moneda><total>%s</total></factura>"
			.formatted(invoiceNumber, annualCost.toPlainString());

		jdbcTemplate.update(
			"""
			insert into poliza (
			    intento_pago_id, folio, vigencia_desde, vigencia_hasta,
			    costo_seguro, iva_importe, folio_factura, factura_json,
			    factura_xml, modalidad_pago_id, poliza_anterior_id, estatus_poliza_id
			) values (?, ?, ?, ?, ?, ?, ?, json(?), xmltype(?), ?, null, ?)
			""",
			attempt.getId(),
			policyNumber,
			java.sql.Date.valueOf(coverageStart),
			java.sql.Date.valueOf(coverageEnd),
			annualCost,
			tax,
			invoiceNumber,
			invoiceJson,
			invoiceXml,
			mode.getId(),
			activeStatusId
		);
		Long policyId = jdbcTemplate.queryForObject(
			"select poliza_id from poliza where intento_pago_id = ?",
			Long.class,
			attempt.getId()
		);
		byte[] policyPdf = createAcademicPdf("Poliza " + policyNumber);
		insertDocument(policyId, "POLICY_PDF", "poliza-" + policyNumber + ".pdf", policyPdf);
		insertDocument(policyId, "PAYMENT_RECEIPT", "comprobante-" + attempt.getPaymentNumber() + ".pdf", receipt);

		return new PolicyResult(policyId, policyNumber, coverageStart, coverageEnd);
	}

	private void insertDocument(Long policyId, String type, String filename, byte[] content) {
		jdbcTemplate.update(
			"""
			insert into documento_poliza (
			    poliza_id, tipo_documento, nombre_archivo, contenido,
			    tipo_mime, fecha_generacion
			) values (?, ?, ?, ?, 'application/pdf', systimestamp)
			""",
			policyId, type, filename, content
		);
	}

	private PaymentResponse responseFromExisting(PaymentAttempt attempt, LocalDate requestedStart) {
		if (APPROVED.equals(attempt.getStatus().getKey())) {
			return jdbcTemplate.queryForObject(
				"""
				select poliza_id, folio, vigencia_desde, vigencia_hasta
				  from poliza
				 where intento_pago_id = ?
				""",
				(rs, rowNumber) -> new PaymentResponse(
					attempt.getId(), APPROVED, attempt.getResponseCode(),
					"El pago ya habia sido aprobado; no se realizo un segundo cargo.",
					attempt.getAuthorizationCode(), null, attempt.getAmount(), "MXN",
					rs.getLong("poliza_id"), rs.getString("folio"),
					rs.getDate("vigencia_desde").toLocalDate(),
					rs.getDate("vigencia_hasta").toLocalDate()
				),
				attempt.getId()
			);
		}
		return new PaymentResponse(
			attempt.getId(), REJECTED, attempt.getResponseCode(), attempt.getRejectionReason(),
			null, null, attempt.getAmount(), "MXN", null, null, requestedStart, null
		);
	}

	private PaymentResponse toRejectedResponse(
		PaymentAttempt attempt,
		GatewayPaymentResult result,
		LocalDate coverageStart
	) {
		return new PaymentResponse(
			attempt.getId(), REJECTED, result.responseCode(), result.publicMessage(),
			null, result.lastFour(), attempt.getAmount(), "MXN",
			null, null, coverageStart, null
		);
	}

	private void validatePayableQuote(Quote quote, LocalDate coverageStart) {
		if (!"APROBADA".equals(quote.getStatus().getKey()) || quote.getCalculatedCost() == null) {
			throw problem(
				HttpStatus.CONFLICT, "QUOTE_NOT_PAYABLE", "La cotizacion no se puede pagar",
				"Solo una cotizacion aprobada puede iniciar un pago.", "conflict"
			);
		}
		if (quote.getValidUntil().isBefore(LocalDate.now())) {
			throw problem(
				HttpStatus.CONFLICT, "QUOTE_EXPIRED", "La cotizacion esta vencida",
				"Genera una nueva cotizacion antes de continuar.", "conflict"
			);
		}
		if (coverageStart.isAfter(quote.getValidUntil())) {
			throw problem(
				HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_COVERAGE_START",
				"El inicio de vigencia no es valido",
				"El inicio de vigencia debe estar dentro de la vigencia de la cotizacion.",
				"validation-error"
			);
		}
	}

	private BigDecimal calculateAmount(Quote quote, String mode) {
		return "ANNUAL".equals(mode)
			? quote.getCalculatedCost()
			: quote.getCalculatedCost().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
	}

	private boolean policyExistsForQuote(Long quoteId) {
		Integer count = jdbcTemplate.queryForObject(
			"""
			select count(*)
			  from poliza p
			  join intento_pago i on i.intento_pago_id = p.intento_pago_id
			 where i.cotizacion_id = ?
			""",
			Integer.class,
			quoteId
		);
		return count != null && count > 0;
	}

	private PaymentStatus status(String key) {
		return statusRepository.findByKey(key)
			.orElseThrow(() -> new IllegalStateException("Falta el estatus de pago: " + key));
	}

	private void validateIdempotencyKey(String key) {
		if (key == null || !key.matches("[A-Za-z0-9_-]{16,64}")) {
			throw problem(
				HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_IDEMPOTENCY_KEY",
				"La clave de idempotencia no es valida",
				"Envia Idempotency-Key con entre 16 y 64 caracteres seguros.",
				"validation-error"
			);
		}
	}

	private byte[] createAcademicPdf(String text) {
		String safeText = text.replace("(", "[").replace(")", "]");
		String pdf = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
			+ "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
			+ "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Contents 4 0 R>>endobj\n"
			+ "4 0 obj<</Length " + (safeText.length() + 28) + ">>stream\n"
			+ "BT 72 720 Td (" + safeText + ") Tj ET\nendstream\nendobj\n%%EOF";
		return pdf.getBytes(StandardCharsets.US_ASCII);
	}

	private ApiException quoteNotFound() {
		return problem(
			HttpStatus.NOT_FOUND, "QUOTE_NOT_FOUND", "La cotizacion no existe",
			"No existe una cotizacion accesible con el identificador proporcionado.", "not-found"
		);
	}

	private ApiException invalidSession() {
		return problem(
			HttpStatus.UNAUTHORIZED, "INVALID_SESSION", "La sesion no es valida",
			"No existe un cliente para la sesion actual.", "invalid-session"
		);
	}

	private ApiException problem(
		HttpStatus status,
		String code,
		String title,
		String detail,
		String type
	) {
		return new ApiException(status, code, title, detail, type);
	}

	private record PolicyResult(
		Long id,
		String number,
		LocalDate coverageStart,
		LocalDate coverageEnd
	) {
	}
}

