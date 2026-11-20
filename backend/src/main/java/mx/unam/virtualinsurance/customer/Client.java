package mx.unam.virtualinsurance.customer;

import java.time.LocalDate;

import mx.unam.virtualinsurance.catalog.gender.Gender;

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
@Table(name = "CLIENTE")
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CLIENTE_ID", nullable = false)
	private Long clientId;

	@Column(name = "NOMBRE", nullable = false, length = 40)
	private String name;

	@Column(name = "APELLIDO_PATERNO", nullable = false, length = 40)
	private String paternalSurname;

	@Column(name = "APELLIDO_MATERNO", length = 40)
	private String maternalSurname;

	@Column(name = "FECHA_NACIMIENTO", nullable = false)
	private LocalDate birthDate;

	@Column(name = "SEXO", nullable = false, length = 1)
	private String sex;

	@Column(name = "IDENTIFICADOR_FISCAL", nullable = false, length = 40)
	private String fiscalIdentifier;

	@Column(name = "EMAIL", nullable = false, length = 250)
	private String email;

	@Column(name = "CONTRASENA", nullable = false, length = 255)
	private String passwordHash;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "FOTO", nullable = false)
	private byte[] photo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "GENERO_ID")
	private Gender gender;

	protected Client() {
	}

	public Client(
		String name,
		String paternalSurname,
		String maternalSurname,
		LocalDate birthDate,
		String sex,
		String fiscalIdentifier,
		String email,
		String passwordHash,
		byte[] photo,
		Gender gender
	) {
		this.name = name;
		this.paternalSurname = paternalSurname;
		this.maternalSurname = maternalSurname;
		this.birthDate = birthDate;
		this.sex = sex;
		this.fiscalIdentifier = fiscalIdentifier;
		this.email = email;
		this.passwordHash = passwordHash;
		this.photo = photo.clone();
		this.gender = gender;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getName() {
		return name;
	}

	public String getPaternalSurname() {
		return paternalSurname;
	}

	public String getMaternalSurname() {
		return maternalSurname;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public String getSex() {
		return sex;
	}

	public String getFiscalIdentifier() {
		return fiscalIdentifier;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public byte[] getPhoto() {
		return photo.clone();
	}

	public Gender getGender() {
		return gender;
	}
}
