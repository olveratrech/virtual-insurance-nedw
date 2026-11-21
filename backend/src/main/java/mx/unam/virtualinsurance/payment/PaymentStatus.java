package mx.unam.virtualinsurance.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ESTATUS_INTENTO_PAGO")
public class PaymentStatus {

	@Id
	@Column(name = "ESTATUS_INTENTO_PAGO_ID", nullable = false)
	private Long id;

	@Column(name = "CLAVE", nullable = false, length = 20)
	private String key;

	protected PaymentStatus() {
	}

	public String getKey() {
		return key;
	}
}

