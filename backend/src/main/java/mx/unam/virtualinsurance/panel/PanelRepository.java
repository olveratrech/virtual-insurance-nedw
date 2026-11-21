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
			select resource_type, resource_id, destination_id, tipo_seguro, referencia,
			       estatus, costo_anual, vigencia_hasta, fecha_actividad,
			       modalidad_pago, motivo_rechazo
			  from (
			        select 'QUOTE' resource_type,
			               c.cotizacion_id resource_id,
			               c.cotizacion_id destination_id,
			               c.tipo_seguro,
			               to_char(c.numero_cotizacion, 'FM0000') referencia,
			               ec.clave estatus,
			               c.costo_calculado costo_anual,
			               cast(null as date) vigencia_hasta,
			               c.fecha_cotizacion fecha_actividad,
			               cast(null as varchar2(10)) modalidad_pago,
			               cast(null as varchar2(500)) motivo_rechazo
			          from cotizacion c
			          join estatus_cotizacion ec
			            on ec.estatus_cotizacion_id = c.estatus_cotizacion_id
			         where c.cliente_id = ?
			        union all
			        select 'POLICY' resource_type,
			               p.poliza_id resource_id,
			               p.poliza_id destination_id,
			               c.tipo_seguro,
			               p.folio referencia,
			               ep.clave estatus,
			               cast(null as number) costo_anual,
			               p.vigencia_hasta,
			               (
			                   select max(h.fecha_cambio)
			                     from historico_estatus_poliza h
			                    where h.poliza_id = p.poliza_id
			               ) fecha_actividad,
			               cast(null as varchar2(10)) modalidad_pago,
			               cast(null as varchar2(500)) motivo_rechazo
			          from poliza p
			          join estatus_poliza ep
			            on ep.estatus_poliza_id = p.estatus_poliza_id
			          join intento_pago i on i.intento_pago_id = p.intento_pago_id
			          join cotizacion c on c.cotizacion_id = i.cotizacion_id
			         where c.cliente_id = ?
			        union all
			        select 'PAYMENT' resource_type,
			               i.intento_pago_id resource_id,
			               coalesce(p.poliza_id, c.cotizacion_id) destination_id,
			               c.tipo_seguro,
			               to_char(i.numero_pago) referencia,
			               ei.clave estatus,
			               i.importe costo_anual,
			               cast(null as date) vigencia_hasta,
			               i.fecha_pago fecha_actividad,
			               mp.clave modalidad_pago,
			               i.motivo_rechazo
			          from intento_pago i
			          join cotizacion c on c.cotizacion_id = i.cotizacion_id
			          join estatus_intento_pago ei
			            on ei.estatus_intento_pago_id = i.estatus_intento_pago_id
			          join modalidad_pago mp on mp.modalidad_pago_id = i.modalidad_pago_id
			          left join poliza p on p.intento_pago_id = i.intento_pago_id
			         where c.cliente_id = ?
			       )
			 order by fecha_actividad desc, resource_type desc, resource_id desc
			""",
			(rs, rowNumber) -> new ActivityRow(
				rs.getString("resource_type"),
				rs.getLong("resource_id"),
				rs.getLong("destination_id"),
				rs.getString("tipo_seguro"),
				rs.getString("referencia"),
				rs.getString("estatus"),
				rs.getBigDecimal("costo_anual"),
				rs.getDate("vigencia_hasta") == null
					? null
					: rs.getDate("vigencia_hasta").toLocalDate(),
				rs.getString("modalidad_pago"),
				rs.getString("motivo_rechazo")
			),
			clientId,
			clientId,
			clientId
		);
	}

	public record ActivityRow(
		String resourceType,
		Long resourceId,
		Long destinationId,
		String insuranceType,
		String reference,
		String status,
		BigDecimal annualCost,
		LocalDate validUntil,
		String paymentMode,
		String rejectionReason
	) {
	}
}
