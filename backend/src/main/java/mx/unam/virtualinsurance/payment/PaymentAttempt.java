package mx.unam.virtualinsurance.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import mx.unam.virtualinsurance.quote.Quote;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "INTENTO_PAGO")
public class PaymentAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "INTENTO_PAGO_ID", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "COTIZACION_ID", nullable = false)
	private Quote quote;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TARJETA_ID", nullable = false)
	private PaymentCard card;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "MODALIDAD_PAGO_ID", nullable = false)
	private PaymentMode mode;

	@Column(name = "NUMERO_PAGO", nullable = false)
	private Long paymentNumber;

	@Column(name = "FECHA_PAGO", nullable = false)
	private OffsetDateTime paidAt;

	@Column(name = "IMPORTE", nullable = false, precision = 14, scale = 2)
	private BigDecimal amount;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "COMPROBANTE_PDF")
	private byte[] receiptPdf;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ESTATUS_INTENTO_PAGO_ID", nullable = false)
	private PaymentStatus status;

	@Column(name = "MOTIVO_RECHAZO", length = 500)
	private String rejectionReason;

	@Column(name = "IDEMPOTENCY_KEY", length = 64)
	private String idempotencyKey;

	@Column(name = "CODIGO_RESPUESTA", length = 40)
	private String responseCode;

	@Column(name = "CODIGO_AUTORIZACION", length = 40)
	private String authorizationCode;

	protected PaymentAttempt() {
	}

	public PaymentAttempt(
		Quote quote,
		PaymentCard card,
		PaymentMode mode,
		Long paymentNumber,
		OffsetDateTime paidAt,
		BigDecimal amount,
		PaymentStatus status,
		String idempotencyKey
	) {
		this.quote = quote;
		this.card = card;
		this.mode = mode;
		this.paymentNumber = paymentNumber;
		this.paidAt = paidAt;
		this.amount = amount;
		this.status = status;
		this.idempotencyKey = idempotencyKey;
	}

	public void approve(PaymentStatus approvedStatus, byte[] receipt, GatewayPaymentResult result) {
		this.status = approvedStatus;
		this.receiptPdf = receipt.clone();
		this.rejectionReason = null;
		this.responseCode = result.responseCode();
		this.authorizationCode = result.authorizationCode();
	}

	public void reject(PaymentStatus rejectedStatus, GatewayPaymentResult result) {
		this.status = rejectedStatus;
		this.receiptPdf = null;
		this.rejectionReason = result.publicMessage();
		this.responseCode = result.responseCode();
		this.authorizationCode = null;
	}

	public Long getId() {
		return id;
	}

	public Quote getQuote() {
		return quote;
	}

	public PaymentMode getMode() {
		return mode;
	}

	public Long getPaymentNumber() {
		return paymentNumber;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public String getAuthorizationCode() {
		return authorizationCode;
	}
}

