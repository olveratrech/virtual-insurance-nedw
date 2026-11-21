package mx.unam.virtualinsurance.admin.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ROL_INTERNO")
public class InternalRole {

	@Id
	@Column(name = "ROL_INTERNO_ID", nullable = false)
	private Long internalRoleId;

	@Column(name = "CLAVE", nullable = false, length = 40)
	private String key;

	protected InternalRole() {
	}

	public String getKey() {
		return key;
	}
}
