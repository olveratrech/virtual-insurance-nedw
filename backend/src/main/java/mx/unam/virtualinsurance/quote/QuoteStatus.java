package mx.unam.virtualinsurance.quote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ESTATUS_COTIZACION")
public class QuoteStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ESTATUS_COTIZACION_ID", nullable = false)
	private Long quoteStatusId;

	@Column(name = "DESCRIPCION", nullable = false, length = 100)
	private String description;

	@Column(name = "CLAVE", nullable = false, length = 20)
	private String key;

	protected QuoteStatus() {
	}

	public Long getQuoteStatusId() {
		return quoteStatusId;
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}
}
