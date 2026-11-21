package mx.unam.virtualinsurance.document;

import java.util.List;

import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyDocumentService {

	private static final String POLICY_PDF = "POLICY_PDF";
	private static final String PAYMENT_RECEIPT = "PAYMENT_RECEIPT";

	private final JdbcTemplate jdbcTemplate;
	private final PolicyDocumentGenerator generator;

	public PolicyDocumentService(JdbcTemplate jdbcTemplate, PolicyDocumentGenerator generator) {
		this.jdbcTemplate = jdbcTemplate;
		this.generator = generator;
	}

	public void generateAndStoreDocuments(Long policyId) {
		PolicyPdfData data = loadPolicyData(policyId, null);
		GeneratedDocument policy = generator.generatePolicy(data);
		GeneratedDocument receipt = generator.generateReceipt(data);
		upsertDocument(policyId, POLICY_PDF, policy);
		upsertDocument(policyId, PAYMENT_RECEIPT, receipt);
		jdbcTemplate.update(
			"""
			update intento_pago
			   set comprobante_pdf = ?
			 where intento_pago_id = (
			       select intento_pago_id from poliza where poliza_id = ?
			 )
			""",
			receipt.content(), policyId
		);
	}

	@Transactional
	public PolicyDocumentDownload download(Long clientId, Long policyId, Long documentId) {
		String documentType;
		try {
			documentType = jdbcTemplate.queryForObject(
				"""
				select d.tipo_documento
				  from documento_poliza d
				  join poliza p on p.poliza_id = d.poliza_id
				  join intento_pago i on i.intento_pago_id = p.intento_pago_id
				  join cotizacion c on c.cotizacion_id = i.cotizacion_id
				 where d.documento_poliza_id = ?
				   and p.poliza_id = ?
				   and c.cliente_id = ?
				""",
				String.class,
				documentId, policyId, clientId
			);
		} catch (EmptyResultDataAccessException exception) {
			throw documentNotFound();
		}

		PolicyPdfData data = loadPolicyData(policyId, clientId);
		GeneratedDocument generated = POLICY_PDF.equals(documentType)
			? generator.generatePolicy(data)
			: generator.generateReceipt(data);
		jdbcTemplate.update(
			"""
			update documento_poliza
			   set nombre_archivo = ?, contenido = ?, tipo_mime = ?, fecha_generacion = systimestamp
			 where documento_poliza_id = ?
			""",
			generated.filename(), generated.content(), generated.mediaType(), documentId
		);

		return new PolicyDocumentDownload(
			generated.filename(), generated.mediaType(), generated.content()
		);
	}

	private void upsertDocument(Long policyId, String type, GeneratedDocument document) {
		int updated = jdbcTemplate.update(
			"""
			update documento_poliza
			   set nombre_archivo = ?, contenido = ?, tipo_mime = ?, fecha_generacion = systimestamp
			 where poliza_id = ? and tipo_documento = ?
			""",
			document.filename(), document.content(), document.mediaType(), policyId, type
		);
		if (updated == 0) {
			jdbcTemplate.update(
				"""
				insert into documento_poliza (
				    poliza_id, tipo_documento, nombre_archivo, contenido,
				    tipo_mime, fecha_generacion
				) values (?, ?, ?, ?, ?, systimestamp)
				""",
				policyId, type, document.filename(), document.content(), document.mediaType()
			);
		}
	}

	private PolicyPdfData loadPolicyData(Long policyId, Long clientId) {
		CancellationPdfData cancellation = loadLatestCancellation(policyId);
		String ownershipClause = clientId == null ? "" : " and c.cliente_id = ?";
		List<Object> arguments = clientId == null ? List.of(policyId) : List.of(policyId, clientId);
		try {
			return jdbcTemplate.queryForObject(
				"""
				select p.poliza_id, p.folio, p.vigencia_desde, p.vigencia_hasta,
				       p.costo_seguro, p.iva_importe, p.folio_factura,
				       ep.clave estatus, c.tipo_seguro, mp.clave modalidad,
				       i.importe, i.codigo_autorizacion,
				       ca.marca, ca.modelo, ca.anio, ca.placas,
				       cl.nombre, cl.apellido_paterno, cl.apellido_materno
				  from poliza p
				  join estatus_poliza ep on ep.estatus_poliza_id = p.estatus_poliza_id
				  join intento_pago i on i.intento_pago_id = p.intento_pago_id
				  join modalidad_pago mp on mp.modalidad_pago_id = p.modalidad_pago_id
				  join cotizacion c on c.cotizacion_id = i.cotizacion_id
				  join cliente cl on cl.cliente_id = c.cliente_id
				  left join cotizacion_auto ca on ca.cotizacion_id = c.cotizacion_id
				 where p.poliza_id = ?
				""" + ownershipClause,
				(rs, rowNumber) -> {
					boolean auto = "A".equals(rs.getString("tipo_seguro"));
					String clientName = fullName(
						rs.getString("nombre"),
						rs.getString("apellido_paterno"),
						rs.getString("apellido_materno")
					);
					return new PolicyPdfData(
						rs.getLong("poliza_id"),
						rs.getString("folio"),
						rs.getString("estatus"),
						auto ? "AUTO" : "MEDICAL",
						auto ? "Seguro de auto" : "Seguro medico",
						auto ? "Cobertura amplia" : "Cobertura medica individual",
						clientName,
						auto ? "Vehiculo asegurado" : "Persona asegurada",
						auto
							? "%s %s - %d - Placas: %s".formatted(
								rs.getString("marca"), rs.getString("modelo"), rs.getInt("anio"),
								rs.getString("placas")
							)
							: clientName,
						rs.getDate("vigencia_desde").toLocalDate(),
						rs.getDate("vigencia_hasta").toLocalDate(),
						rs.getBigDecimal("costo_seguro"),
						rs.getBigDecimal("iva_importe"),
						rs.getString("modalidad"),
						rs.getBigDecimal("importe"),
						rs.getString("codigo_autorizacion"),
						rs.getString("folio_factura"),
						cancellation == null ? null : cancellation.number(),
						cancellation == null ? null : cancellation.status(),
						cancellation == null ? null : cancellation.requestedDate(),
						cancellation == null ? null : cancellation.resolvedDate(),
						cancellation == null ? null : cancellation.observation()
					);
				},
				arguments.toArray()
			);
		} catch (EmptyResultDataAccessException exception) {
			throw documentNotFound();
		}
	}

	private CancellationPdfData loadLatestCancellation(Long policyId) {
		List<CancellationPdfData> cancellations = jdbcTemplate.query(
			"""
			select sc.folio, ec.clave estatus, sc.fecha_solicitada,
			       sc.fecha_resolucion, sc.observacion_resolucion
			  from solicitud_cancelacion sc
			  join estatus_cancelacion ec
			    on ec.estatus_cancelacion_id = sc.estatus_cancelacion_id
			 where sc.poliza_id = ?
			 order by sc.solicitud_cancelacion_id desc
			 fetch first 1 row only
			""",
			(rs, rowNumber) -> new CancellationPdfData(
				rs.getString("folio"),
				rs.getString("estatus"),
				rs.getDate("fecha_solicitada").toLocalDate(),
				rs.getTimestamp("fecha_resolucion") == null
					? null
					: rs.getTimestamp("fecha_resolucion").toLocalDateTime().toLocalDate(),
				rs.getString("observacion_resolucion")
			),
			policyId
		);
		return cancellations.isEmpty() ? null : cancellations.getFirst();
	}

	private String fullName(String name, String paternalSurname, String maternalSurname) {
		return java.util.stream.Stream.of(name, paternalSurname, maternalSurname)
			.filter(value -> value != null && !value.isBlank())
			.collect(java.util.stream.Collectors.joining(" "));
	}

	private ApiException documentNotFound() {
		return new ApiException(
			HttpStatus.NOT_FOUND,
			"POLICY_DOCUMENT_NOT_FOUND",
			"El documento no existe",
			"No existe un documento accesible con los identificadores proporcionados.",
			"not-found"
		);
	}

	private record CancellationPdfData(
		String number,
		String status,
		java.time.LocalDate requestedDate,
		java.time.LocalDate resolvedDate,
		String observation
	) {
	}
}

