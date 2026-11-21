package mx.unam.virtualinsurance.payment;

import mx.unam.virtualinsurance.customer.Client;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TARJETA")
public class PaymentCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TARJETA_ID", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CLIENTE_ID", nullable = false)
	private Client client;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TIPO_TARJETA_ID", nullable = false)
	private CardType type;

	@Column(name = "NUMERO_TARJETA", nullable = false)
	private byte[] encryptedNumber;

	@Column(name = "ANIO_EXPIRACION", nullable = false)
	private Integer expirationYear;

	@Column(name = "MES_EXPIRACION", nullable = false)
	private Integer expirationMonth;

	protected PaymentCard() {
	}

	public PaymentCard(
		Client client,
		CardType type,
		byte[] encryptedNumber,
		Integer expirationYear,
		Integer expirationMonth
	) {
		this.client = client;
		this.type = type;
		this.encryptedNumber = encryptedNumber.clone();
		this.expirationYear = expirationYear;
		this.expirationMonth = expirationMonth;
	}
}

