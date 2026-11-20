package mx.unam.virtualinsurance.catalog.gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "GENERO")
public class Gender {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "GENERO_ID", nullable = false)
	private Long generoId;

	@Column(name = "CLAVE", nullable = false, length = 20)
	private String key;

	@Column(name = "DESCRIPCION", nullable = false, length = 100)
	private String description;

	protected Gender() {
	}

	public Long getGeneroId() {
		return generoId;
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}
}
