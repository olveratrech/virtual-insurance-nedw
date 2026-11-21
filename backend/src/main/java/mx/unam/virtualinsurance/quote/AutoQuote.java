package mx.unam.virtualinsurance.quote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "COTIZACION_AUTO")
public class AutoQuote {

	@Id
	@Column(name = "COTIZACION_ID", nullable = false)
	private Long quoteId;

	@Column(name = "MARCA", nullable = false, length = 40)
	private String brand;

	@Column(name = "MODELO", nullable = false, length = 40)
	private String model;

	@Column(name = "ANIO", nullable = false)
	private Integer year;

	@Column(name = "CATEGORIA", nullable = false, length = 10)
	private String category;

	@Column(name = "PLACAS", nullable = false, length = 20)
	private String plates;

	@Column(name = "NUMERO_LICENCIA", nullable = false, length = 40)
	private String licenseNumber;

	@Column(name = "EDAD_CONDUCTOR", nullable = false)
	private Integer driverAge;

	@Column(name = "PUNTOS_NEGATIVOS", nullable = false)
	private Integer negativePoints;

	@Column(name = "ANTECEDENTES_PENALES", nullable = false, length = 1)
	private String criminalRecord;

	protected AutoQuote() {
	}

	public AutoQuote(
		Long quoteId,
		String brand,
		String model,
		Integer year,
		String category,
		String plates,
		String licenseNumber,
		Integer driverAge,
		Integer negativePoints,
		boolean criminalRecord
	) {
		this.quoteId = quoteId;
		this.brand = brand;
		this.model = model;
		this.year = year;
		this.category = category;
		this.plates = plates;
		this.licenseNumber = licenseNumber;
		this.driverAge = driverAge;
		this.negativePoints = negativePoints;
		this.criminalRecord = criminalRecord ? "S" : "N";
	}

	public String getBrand() { return brand; }
	public String getModel() { return model; }
	public Integer getYear() { return year; }
	public String getCategory() { return category; }
	public String getPlates() { return plates; }
	public String getLicenseNumber() { return licenseNumber; }
	public Integer getDriverAge() { return driverAge; }
	public Integer getNegativePoints() { return negativePoints; }
	public boolean hasCriminalRecord() { return "S".equals(criminalRecord); }
}
