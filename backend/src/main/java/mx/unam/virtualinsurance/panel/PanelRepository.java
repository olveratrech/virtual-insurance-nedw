package mx.unam.virtualinsurance.panel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PanelRepository {

	private final JdbcTemplate jdbcTemplate;

	public PanelRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<ActivityRow> findRecentActivity(Long clientId) {
		return jdbcTemplate.query(
			"""
			select resource_type, resource_id, tipo_seguro, referencia,
			       estatus, costo_anual, vigencia_hasta
			  from (
			        select 'QUOTE' resource_type,
			               c.cotizacion_id resource_id,
			               c.tipo_seguro,
			               to_char(c.numero_cotizacion, 'FM0000') referencia,
			               ec.clave estatus,
			               c.costo_calculado costo_anual,
			               cast(null as date) vigencia_hasta,
			               c.fecha_cotizacion fecha_actividad
			          from cotizacion c
			          join estatus_cotizacion ec
			            on ec.estatus_cotizacion_id = c.estatus_cotizacion_id
			         where c.cliente_id = ?
			        union all
			        select 'POLICY' resource_type,
			               p.poliza_id resource_id,
			               c.tipo_seguro,
			               p.folio referencia,
			               ep.clave estatus,
			               cast(null as number) costo_anual,
			               p.vigencia_hasta,
			               (
			                   select max(h.fecha_cambio)
			                     from historico_estatus_poliza h
			                    where h.poliza_id = p.poliza_id
			               ) fecha_actividad
			          from poliza p
			          join estatus_poliza ep
			            on ep.estatus_poliza_id = p.estatus_poliza_id
			          join intento_pago i on i.intento_pago_id = p.intento_pago_id
			          join cotizacion c on c.cotizacion_id = i.cotizacion_id
			         where c.cliente_id = ?
			       )
			 order by fecha_actividad desc, resource_type desc, resource_id desc
			 fetch first 20 rows only
			""",
			(rs, rowNumber) -> new ActivityRow(
				rs.getString("resource_type"),
				rs.getLong("resource_id"),
				rs.getString("tipo_seguro"),
				rs.getString("referencia"),
				rs.getString("estatus"),
				rs.getBigDecimal("costo_anual"),
				rs.getDate("vigencia_hasta") == null
					? null
					: rs.getDate("vigencia_hasta").toLocalDate()
			),
			clientId,
			clientId
		);
	}

	public record ActivityRow(
		String resourceType,
		Long resourceId,
		String insuranceType,
		String reference,
		String status,
		BigDecimal annualCost,
		LocalDate validUntil
	) {
	}
}
