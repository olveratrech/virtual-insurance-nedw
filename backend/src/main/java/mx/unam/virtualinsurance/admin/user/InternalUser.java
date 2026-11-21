package mx.unam.virtualinsurance.admin.user;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "USUARIO_INTERNO")
public class InternalUser {

	@Id
	@Column(name = "USUARIO_INTERNO_ID", nullable = false)
	private Long internalUserId;

	@Column(name = "NOMBRE", nullable = false, length = 120)
	private String name;

	@Column(name = "EMAIL", nullable = false, length = 250)
	private String email;

	@Column(name = "CONTRASENA", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "ACTIVO", nullable = false, length = 1)
	private String active;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "USUARIO_INTERNO_ROL",
		joinColumns = @JoinColumn(name = "USUARIO_INTERNO_ID"),
		inverseJoinColumns = @JoinColumn(name = "ROL_INTERNO_ID")
	)
	private Set<InternalRole> roles = new LinkedHashSet<>();

	protected InternalUser() {
	}

	public Long getInternalUserId() {
		return internalUserId;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public boolean isActive() {
		return "S".equals(active);
	}

	public Set<InternalRole> getRoles() {
		return Set.copyOf(roles);
	}
}
