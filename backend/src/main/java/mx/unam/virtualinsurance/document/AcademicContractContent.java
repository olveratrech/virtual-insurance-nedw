package mx.unam.virtualinsurance.document;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AcademicContractContent {

	public String version() {
		return "NEDW-2026.1";
	}

	public String disclaimer() {
		return "DOCUMENTO EXCLUSIVAMENTE ACADEMICO. No constituye una oferta, contrato de seguro, "
			+ "comprobante fiscal ni operacion financiera real.";
	}

	public List<ContractSection> policySections(String policyType) {
		String coverage = "AUTO".equals(policyType)
			? "La demostracion contempla danos materiales, responsabilidad civil y asistencia vial "
				+ "unicamente como elementos ilustrativos."
			: "La demostracion contempla atencion medica, hospitalizacion y asistencia individual "
				+ "unicamente como elementos ilustrativos.";

		return List.of(
			new ContractSection("1. Objeto academico", disclaimer()),
			new ContractSection("2. Cobertura demostrativa", coverage),
			new ContractSection(
				"3. Declaraciones",
				"La informacion mostrada proviene de datos ficticios capturados en Virtual Insurance. "
					+ "No debe utilizarse informacion bancaria, medica o personal real."
			),
			new ContractSection(
				"4. Vigencia y pago",
				"La vigencia, prima y pagos se administran mediante reglas academicas. La aprobacion "
					+ "del simulador no representa autorizacion de una institucion financiera."
			),
			new ContractSection(
				"5. Privacidad y seguridad",
				"Los numeros de instrumentos ficticios se cifran y el codigo de seguridad no se "
					+ "persiste. La aplicacion aplica sesion, CSRF y autorizacion por propietario."
			),
			new ContractSection(
				"6. Terminos y modificaciones",
				"Este contenido es una plantilla versionable. Futuras implementaciones podran incorporar "
					+ "condiciones generales, anexos, exclusiones y firmas con revision juridica academica."
			)
		);
	}
}

