package mx.unam.virtualinsurance.admin.cancellation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminCancellationRepository {

	private static final String BASE_QUERY = """
		select sc.solicitud_cancelacion_id, sc.folio, ec.clave estatus,
		       sc.motivo, sc.fecha_solicitada, sc.comentarios, sc.fecha_creacion,
		       sc.fecha_resolucion, sc.observacion_resolucion,
		       p.poliza_id, p.folio folio_poliza, ep.clave estatus_poliza,
		       c.tipo_seguro,
		       cl.cliente_id, cl.nombre, cl.apellido_paterno, cl.apellido_materno,
		       cl.email,
		       ui.usuario_interno_id, ui.nombre nombre_responsable
		  from solicitud_cancelacion sc
		  join estatus_cancelacion ec
		    on ec.estatus_cancelacion_id = sc.estatus_cancelacion_id
		  join poliza p on p.poliza_id = sc.poliza_id
		  join estatus_poliza ep on ep.estatus_poliza_id = p.estatus_poliza_id
		  join intento_pago ip on ip.intento_pago_id = p.intento_pago_id
		  join cotizacion c on c.cotizacion_id = ip.cotizacion_id
		  join cliente cl on cl.cliente_id = c.cliente_id
		  left join usuario_interno ui
		    on ui.usuario_interno_id = sc.resuelta_por_usuario_id
		""";

	private final JdbcTemplate jdbcTemplate;

	public AdminCancellationRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<AdminCancellationResponse> findAllByStatus(String status) {
		return jdbcTemplate.query(
			BASE_QUERY + " where ec.clave = ? order by sc.fecha_creacion desc, sc.solicitud_cancelacion_id desc",
			this::mapResponse,
			status
		);
	}

	public Optional<AdminCancellationResponse> findById(Long cancellationId) {
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(
				BASE_QUERY + " where sc.solicitud_cancelacion_id = ?",
				this::mapResponse,
				cancellationId
			));
		} catch (EmptyResultDataAccessException exception) {
			return Optional.empty();
		}
	}

	public Optional<String> findStatusForUpdate(Long cancellationId) {
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(
				"""
				select ec.clave
				  from solicitud_cancelacion sc
				  join estatus_cancelacion ec
				    on ec.estatus_cancelacion_id = sc.estatus_cancelacion_id
				 where sc.solicitud_cancelacion_id = ?
				 for update of sc.estatus_cancelacion_id
				""",
				String.class,
				cancellationId
			));
		} catch (EmptyResultDataAccessException exception) {
			return Optional.empty();
		}
	}

	public void resolve(
		Long cancellationId,
		String decision,
		String observation,
		Long internalUserId
	) {
		jdbcTemplate.update(
			"""
			update solicitud_cancelacion
			   set estatus_cancelacion_id = (
			           select estatus_cancelacion_id
			             from estatus_cancelacion
			            where clave = ?
			       ),
			       observacion_resolucion = ?,
			       resuelta_por_usuario_id = ?
			 where solicitud_cancelacion_id = ?
			""",
			decision,
			observation,
			internalUserId,
			cancellationId
		);
	}

	private AdminCancellationResponse mapResponse(ResultSet rs, int rowNumber) throws SQLException {
		String insuranceType = rs.getString("tipo_seguro");
		Long resolverId = rs.getObject("usuario_interno_id", Long.class);
		AdminCancellationResolutionResponse resolution = resolverId == null
			? null
			: new AdminCancellationResolutionResponse(
				toOffsetDateTime(rs, "fecha_resolucion"),
				rs.getString("observacion_resolucion"),
				resolverId,
				rs.getString("nombre_responsable")
			);

		return new AdminCancellationResponse(
			rs.getLong("solicitud_cancelacion_id"),
			rs.getString("folio"),
			rs.getString("estatus"),
			rs.getString("motivo"),
			rs.getDate("fecha_solicitada").toLocalDate(),
			rs.getString("comentarios"),
			toOffsetDateTime(rs, "fecha_creacion"),
			new AdminCancellationPolicyResponse(
				rs.getLong("poliza_id"),
				rs.getString("folio_poliza"),
				rs.getString("estatus_poliza"),
				"A".equals(insuranceType) ? "AUTO" : "MEDICAL",
				"A".equals(insuranceType) ? "Seguro de auto" : "Seguro medico"
			),
			new AdminCancellationClientResponse(
				rs.getLong("cliente_id"),
				fullName(
					rs.getString("nombre"),
					rs.getString("apellido_paterno"),
					rs.getString("apellido_materno")
				),
				rs.getString("email")
			),
			resolution
		);
	}

	private OffsetDateTime toOffsetDateTime(ResultSet rs, String column) throws SQLException {
		var timestamp = rs.getTimestamp(column);
		return timestamp == null
			? null
			: timestamp.toLocalDateTime().atZone(ZoneId.systemDefault()).toOffsetDateTime();
	}

	private String fullName(String name, String paternalSurname, String maternalSurname) {
		return java.util.stream.Stream.of(name, paternalSurname, maternalSurname)
			.filter(value -> value != null && !value.isBlank())
			.collect(java.util.stream.Collectors.joining(" "));
	}
}
