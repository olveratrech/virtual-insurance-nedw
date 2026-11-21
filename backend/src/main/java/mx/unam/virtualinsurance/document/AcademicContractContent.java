package mx.unam.virtualinsurance.document;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AcademicContractContent {

	public String version() {
		return "VI-POL-2026.2";
	}

	public String disclaimer() {
		return "IMPLEMENTACIÓN ACADÉMICA. Las cotizaciones, pólizas, pagos, tarjetas y "
			+ "operaciones mostradas son ficticias y no producen efectos legales, financieros ni "
			+ "aseguradores reales.";
	}

	public List<ContractSection> policySections(String policyType) {
		String coverage = "AUTO".equals(policyType)
			? "Durante el periodo de vigencia estipulado y sujeto al cumplimiento de los requisitos "
				+ "administrativos correspondientes, la cobertura ampara daños materiales del vehículo, "
				+ "responsabilidad civil por daños a terceros y asistencia vial derivados de accidentes "
				+ "cubiertos originados dentro de la vigencia. Para que esta cobertura surta efectos en una "
				+ "operación comercial real, los deducibles por evento, sumas aseguradas, límites de "
				+ "responsabilidad, servicios de asistencia y talleres en convenio deberán constar "
				+ "expresamente en condiciones particulares emitidas por una institución de seguros "
				+ "debidamente autorizada por la Comisión Nacional de Seguros y Fianzas (CNSF)."
			: "Durante el periodo de vigencia estipulado y sujeto al cumplimiento de los requisitos "
				+ "administrativos correspondientes, la cobertura ampara gastos médicos mayores, atención "
				+ "hospitalaria, honorarios médico-quirúrgicos, estudios auxiliares de diagnóstico, "
				+ "medicamentos intrahospitalarios y asistencia médica individual derivados de accidentes "
				+ "o enfermedades cubiertas originadas dentro de la vigencia. Para que esta cobertura surta "
				+ "efectos en una operación comercial real, los deducibles por evento, porcentajes de "
				+ "coaseguro, periodos de espera, tabuladores médicos, red hospitalaria y sumas aseguradas "
				+ "máximas deberán constar expresamente en condiciones particulares emitidas por una "
				+ "institución de seguros debidamente autorizada por la CNSF.";

		return List.of(
			new ContractSection(
				"1. Objeto del seguro",
				"La presente póliza de seguro tiene por objeto establecer los términos, alcances, "
					+ "límites y obligaciones bajo los cuales Virtual Insurance, en adelante la Compañía, "
					+ "asume el compromiso de indemnizar o cubrir los gastos derivados de los riesgos "
					+ "amparados en la carátula respecto del bien o la persona asegurada. La carátula de la "
					+ "póliza, el cuestionario de evaluación de riesgo, las declaraciones del contratante, "
					+ "los endosos posteriores y estas condiciones generales constituyen un contrato único "
					+ "e indivisible para efectos de consulta, interpretación y trazabilidad documental."
			),
			new ContractSection("2. Cobertura", coverage),
			new ContractSection(
				"3. Declaraciones del contratante",
				"El contratante declara bajo protesta de decir verdad que toda la información "
					+ "proporcionada en la solicitud de cotización y evaluación de riesgo, incluyendo datos "
					+ "de identificación, estado general, ubicación y características del riesgo, es verídica, "
					+ "exacta y completa conforme al escenario registrado en el sistema. Asimismo, manifiesta "
					+ "haber leído, revisado y aceptado la correspondencia entre el producto contratado, el "
					+ "plan seleccionado, la persona o bien asegurado, las fechas de vigencia y el desglose "
					+ "económico. Cualquier omisión, inexactitud o agravación esencial del riesgo deberá "
					+ "notificarse oportunamente y quedar asentada mediante un endoso o una nueva versión "
					+ "foliada de la póliza."
			),
			new ContractSection(
				"4. Vigencia, prima y forma de pago",
				"La protección descrita en este documento inicia y concluye a las 12:00 horas del día "
					+ "indicado en los campos de inicio y fin de vigencia de la carátula, siempre que el estado "
					+ "operativo de la póliza figure como activo. La prima neta anual, el Impuesto al Valor "
					+ "Agregado (IVA) informativo y la periodicidad de pago se detallan en el resumen económico. "
					+ "Si el contratante registra una solicitud de cancelación anticipada, la cobertura "
					+ "continuará bajo los términos vigentes hasta que el área operativa emita una resolución "
					+ "aprobatoria formal y genere el folio correspondiente."
			),
			new ContractSection(
				"5. Privacidad y tratamiento de datos",
				"En cumplimiento con los principios de licitud, consentimiento, información y "
					+ "confidencialidad aplicables a la protección de datos personales en posesión de "
					+ "particulares, Virtual Insurance recaba y procesa únicamente los datos necesarios para "
					+ "la emisión, administración y consulta de la póliza. El acceso al expediente digital "
					+ "está restringido al titular autenticado mediante controles de sesión, autorización "
					+ "por rol y protección contra falsificación de petición en sitios cruzados (CSRF). Los "
					+ "datos financieros y números de tarjeta empleados en el entorno de prueba se transmiten "
					+ "bajo protocolos seguros y se almacenan cifrados; el código CVV o CVC nunca se conserva."
			),
			new ContractSection(
				"6. Términos, condiciones y exclusiones",
				"Quedan expresamente excluidos los padecimientos, accidentes o siniestros ocurridos con "
					+ "anterioridad a la fecha de inicio de vigencia, los eventos derivados de dolo, culpa grave "
					+ "o actos intencionales del contratante o asegurado, así como cualquier supuesto derivado "
					+ "de declaraciones falsas o inexactas. Tampoco estarán amparados gastos o servicios fuera "
					+ "del alcance del plan contratado. En una operación real, las exclusiones particulares, "
					+ "los procedimientos de reclamación y reembolso, la prescripción de acciones, la "
					+ "jurisdicción y las firmas de representantes legales deberán formalizarse conforme a "
					+ "la legislación mercantil y de seguros vigente."
			)
		);
	}
}

