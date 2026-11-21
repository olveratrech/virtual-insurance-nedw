package mx.unam.virtualinsurance.quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import mx.unam.virtualinsurance.customer.Client;

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
@Table(name = "COTIZACION")
public class Quote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "COTIZACION_ID", nullable = false)
	private Long quoteId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CLIENTE_ID", nullable = false)
	private Client client;

	@Column(name = "NUMERO_COTIZACION", nullable = false, unique = true)
	private Integer quoteNumber;

	@Column(name = "FECHA_COTIZACION", nullable = false)
	private OffsetDateTime quotedAt;

	@Column(name = "VIGENCIA_COTIZACION", nullable = false)
	private LocalDate validUntil;

	@Column(name = "TIPO_SEGURO", nullable = false, length = 1)
	private String insuranceType;

	@Column(name = "COSTO_CALCULADO", precision = 14, scale = 2)
	private BigDecimal calculatedCost;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "RECHAZO_PDF")
	private byte[] rejectionPdf;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ESTATUS_COTIZACION_ID", nullable = false)
	private QuoteStatus status;

	protected Quote() {
	}

	public Quote(
		Client client,
		Integer quoteNumber,
		OffsetDateTime quotedAt,
		LocalDate validUntil,
		String insuranceType,
		BigDecimal calculatedCost,
		QuoteStatus status
	) {
		this.client = client;
		this.quoteNumber = quoteNumber;
		this.quotedAt = quotedAt;
		this.validUntil = validUntil;
		this.insuranceType = insuranceType;
		this.calculatedCost = calculatedCost;
		this.status = status;
	}

	public Long getQuoteId() {
		return quoteId;
	}

	public Integer getQuoteNumber() {
		return quoteNumber;
	}

	public OffsetDateTime getQuotedAt() {
		return quotedAt;
	}

	public LocalDate getValidUntil() {
		return validUntil;
	}

	public String getInsuranceType() {
		return insuranceType;
	}

	public BigDecimal getCalculatedCost() {
		return calculatedCost;
	}

	public QuoteStatus getStatus() {
		return status;
	}
}
