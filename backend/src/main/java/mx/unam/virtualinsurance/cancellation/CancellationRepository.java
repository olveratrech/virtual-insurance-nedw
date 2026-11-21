package mx.unam.virtualinsurance.cancellation;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CancellationRepository {

	private final JdbcTemplate jdbcTemplate;

	public CancellationRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<OwnedPolicy> findOwnedPolicyForUpdate(Long policyId, Long clientId) {
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(
				"""
				select p.poliza_id, ep.clave estatus
				  from poliza p
				  join estatus_poliza ep on ep.estatus_poliza_id = p.estatus_poliza_id
				  join intento_pago i on i.intento_pago_id = p.intento_pago_id
				  join cotizacion c on c.cotizacion_id = i.cotizacion_id
				 where p.poliza_id = ?
				   and c.cliente_id = ?
				   for update of p.estatus_poliza_id
				""",
				(rs, rowNumber) -> new OwnedPolicy(
					rs.getLong("poliza_id"), rs.getString("estatus")
				),
				policyId,
				clientId
			));
		} catch (EmptyResultDataAccessException exception) {
			return Optional.empty();
		}
	}

	public boolean hasPendingRequest(Long policyId) {
		Integer count = jdbcTemplate.queryForObject(
			"""
			select count(*)
			  from solicitud_cancelacion sc
			  join estatus_cancelacion ec
			    on ec.estatus_cancelacion_id = sc.estatus_cancelacion_id
			 where sc.poliza_id = ?
			   and ec.clave = 'SOLICITADA'
			""",
			Integer.class,
			policyId
		);
		return count != null && count > 0;
	}

	public Long create(
		Long policyId,
		String requestNumber,
		String reason,
		LocalDate requestedDate,
		String comments,
		OffsetDateTime createdAt
	) {
		Long requestedStatusId = jdbcTemplate.queryForObject(
			"select estatus_cancelacion_id from estatus_cancelacion where clave = 'SOLICITADA'",
			Long.class
		);
		jdbcTemplate.update(
			"""
			insert into solicitud_cancelacion (
			    poliza_id, folio, motivo, fecha_solicitada, comentarios,
			    estatus_cancelacion_id, fecha_creacion
			) values (?, ?, ?, ?, ?, ?, ?)
			""",
			policyId,
			requestNumber,
			reason,
			java.sql.Date.valueOf(requestedDate),
			comments,
			requestedStatusId,
			java.sql.Timestamp.from(createdAt.toInstant())
		);
		return jdbcTemplate.queryForObject(
			"select solicitud_cancelacion_id from solicitud_cancelacion where folio = ?",
			Long.class,
			requestNumber
		);
	}

	public record OwnedPolicy(Long id, String status) {
	}
}
