package mx.unam.virtualinsurance.policy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyService {

	private final JdbcTemplate jdbcTemplate;

	public PolicyService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(readOnly = true)
	public PolicyResponse findPolicy(Long clientId, Long policyId) {
		PolicySummary summary;
		try {
			summary = jdbcTemplate.queryForObject(
				"""
				select p.poliza_id, p.folio, p.vigencia_desde, p.vigencia_hasta,
				       ep.clave estatus, c.tipo_seguro, mp.clave modalidad,
				       i.importe, i.codigo_autorizacion,
				       ca.marca, ca.modelo, ca.anio,
				       cl.nombre, cl.apellido_paterno, cl.apellido_materno
				  from poliza p
				  join estatus_poliza ep on ep.estatus_poliza_id = p.estatus_poliza_id
				  join intento_pago i on i.intento_pago_id = p.intento_pago_id
				  join modalidad_pago mp on mp.modalidad_pago_id = p.modalidad_pago_id
				  join cotizacion c on c.cotizacion_id = i.cotizacion_id
				  join cliente cl on cl.cliente_id = c.cliente_id
				  left join cotizacion_auto ca on ca.cotizacion_id = c.cotizacion_id
				 where p.poliza_id = ?
				   and c.cliente_id = ?
				""",
				(rs, rowNumber) -> new PolicySummary(
					rs.getLong("poliza_id"),
					rs.getString("folio"),
					rs.getDate("vigencia_desde").toLocalDate(),
					rs.getDate("vigencia_hasta").toLocalDate(),
					rs.getString("estatus"),
					rs.getString("tipo_seguro"),
					rs.getString("modalidad"),
					rs.getBigDecimal("importe"),
					rs.getString("codigo_autorizacion"),
					rs.getString("marca"),
					rs.getString("modelo"),
					rs.getObject("anio", Integer.class),
					fullName(
						rs.getString("nombre"),
						rs.getString("apellido_paterno"),
						rs.getString("apellido_materno")
					)
				),
				policyId,
				clientId
			);
		} catch (EmptyResultDataAccessException exception) {
			throw policyNotFound();
		}

		List<PolicyDocumentResponse> documents = jdbcTemplate.query(
			"""
			select documento_poliza_id, tipo_documento, nombre_archivo
			  from documento_poliza
			 where poliza_id = ?
			 order by documento_poliza_id
			""",
			(rs, rowNumber) -> new PolicyDocumentResponse(
				rs.getLong("documento_poliza_id"),
				rs.getString("tipo_documento"),
				documentName(rs.getString("tipo_documento")),
				"PDF",
				"/polizas/%d/documentos/%d/contenido".formatted(
					summary.id(), rs.getLong("documento_poliza_id")
				)
			),
			policyId
		);

		boolean auto = "A".equals(summary.insuranceType());
		PolicyInsuredResponse insured = auto
			? new PolicyInsuredResponse(
				"Vehiculo asegurado",
				"%s %s - %d".formatted(summary.brand(), summary.model(), summary.year())
			)
			: new PolicyInsuredResponse("Persona asegurada", summary.clientName());
		LocalDate nextPaymentDate = "MONTHLY".equals(summary.paymentMode())
			? summary.validFrom().plusMonths(1)
			: null;

		return new PolicyResponse(
			summary.id(),
			summary.number(),
			summary.status(),
			List.of("ACTIVA", "CANCELACION_SOLICITADA").contains(summary.status()),
			auto ? "AUTO" : "MEDICAL",
			auto ? "Seguro de auto" : "Seguro medico",
			auto ? "Cobertura amplia" : "Cobertura medica individual",
			summary.validFrom(),
			summary.validUntil(),
			insured,
			new PolicyPaymentResponse(
				summary.paymentMode(),
				nextPaymentDate,
				summary.lastPaymentAmount(),
				"APLICADO",
				summary.authorizationCode(),
				"MXN"
			),
			documents
		);
	}

	private String fullName(String name, String paternalSurname, String maternalSurname) {
		return java.util.stream.Stream.of(name, paternalSurname, maternalSurname)
			.filter(value -> value != null && !value.isBlank())
			.collect(java.util.stream.Collectors.joining(" "));
	}

	private String documentName(String type) {
		return "POLICY_PDF".equals(type) ? "Poliza de seguro" : "Comprobante de pago";
	}

	private ApiException policyNotFound() {
		return new ApiException(
			HttpStatus.NOT_FOUND,
			"POLICY_NOT_FOUND",
			"La poliza no existe",
			"No existe una poliza accesible con el identificador proporcionado.",
			"not-found"
		);
	}

	private record PolicySummary(
		Long id,
		String number,
		LocalDate validFrom,
		LocalDate validUntil,
		String status,
		String insuranceType,
		String paymentMode,
		BigDecimal lastPaymentAmount,
		String authorizationCode,
		String brand,
		String model,
		Integer year,
		String clientName
	) {
	}
}

