package mx.unam.virtualinsurance.quote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "COTIZACION_MEDICA")
public class MedicalQuote {

	@Id
	@Column(name = "COTIZACION_ID", nullable = false)
	private Long quoteId;

	@Column(name = "EDAD_ASEGURADO", nullable = false)
	private Integer age;

	@Column(name = "HIPERTENSION", nullable = false, length = 1)
	private String hypertension;

	@Column(name = "GLUCOSA_FUERA_RANGO", nullable = false, length = 1)
	private String glucoseOutOfRange;

	@Column(name = "COLESTEROL_FUERA_RANGO", nullable = false, length = 1)
	private String cholesterolOutOfRange;

	@Column(name = "FUMADOR", nullable = false, length = 1)
	private String smoker;

	@Column(name = "ADICCION_ALCOHOL", nullable = false, length = 1)
	private String alcoholAddiction;

	protected MedicalQuote() {
	}

	public MedicalQuote(
		Long quoteId,
		Integer age,
		boolean hypertension,
		boolean glucoseOutOfRange,
		boolean cholesterolOutOfRange,
		boolean smoker,
		boolean alcoholAddiction
	) {
		this.quoteId = quoteId;
		this.age = age;
		this.hypertension = flag(hypertension);
		this.glucoseOutOfRange = flag(glucoseOutOfRange);
		this.cholesterolOutOfRange = flag(cholesterolOutOfRange);
		this.smoker = flag(smoker);
		this.alcoholAddiction = flag(alcoholAddiction);
	}

	private String flag(boolean value) {
		return value ? "S" : "N";
	}

	public Integer getAge() { return age; }
	public boolean hasHypertension() { return "S".equals(hypertension); }
	public boolean hasGlucoseOutOfRange() { return "S".equals(glucoseOutOfRange); }
	public boolean hasCholesterolOutOfRange() { return "S".equals(cholesterolOutOfRange); }
	public boolean isSmoker() { return "S".equals(smoker); }
	public boolean hasAlcoholAddiction() { return "S".equals(alcoholAddiction); }
}
