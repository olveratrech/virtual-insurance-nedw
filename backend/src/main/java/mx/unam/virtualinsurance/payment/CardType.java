package mx.unam.virtualinsurance.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TIPO_TARJETA")
public class CardType {

	@Id
	@Column(name = "TIPO_TARJETA_ID", nullable = false)
	private Long id;

	@Column(name = "CLAVE", nullable = false, length = 20)
	private String key;

	protected CardType() {
	}
}

