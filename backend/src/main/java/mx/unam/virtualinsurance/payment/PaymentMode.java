package mx.unam.virtualinsurance.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "MODALIDAD_PAGO")
public class PaymentMode {

	@Id
	@Column(name = "MODALIDAD_PAGO_ID", nullable = false)
	private Long id;

	@Column(name = "CLAVE", nullable = false, length = 10)
	private String key;

	protected PaymentMode() {
	}

	public Long getId() {
		return id;
	}
}

