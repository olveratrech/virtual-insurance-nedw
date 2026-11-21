package mx.unam.virtualinsurance.quote;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MedicalQuoteStrategy implements QuoteStrategy<MedicalQuoteRequest> {

	private static final String PRODUCT = "Seguro medico";
	private static final String COVERAGE = "Cobertura medica individual";
	private static final BigDecimal ANNUAL_COST = new BigDecimal("18600.00");
	private static final List<String> BENEFITS = List.of(
		"Consultas",
		"Hospitalizacion",
		"Estudios de diagnostico",
		"Atencion de urgencias"
	);
	private static final String REJECTION_REASON =
		"La combinacion de factores declarados requiere una evaluacion distinta a la disponible en este canal.";

	@Override
	public QuoteDecision evaluate(MedicalQuoteRequest request) {
		int activeFactors = 0;
		activeFactors += request.hypertension() ? 1 : 0;
		activeFactors += request.glucoseOutOfRange() ? 1 : 0;
		activeFactors += request.cholesterolOutOfRange() ? 1 : 0;
		activeFactors += request.smoker() ? 1 : 0;
		activeFactors += request.alcoholAddiction() ? 1 : 0;

		if (request.age() > 70 || activeFactors >= 3) {
			return QuoteDecision.rejected(REJECTION_REASON, PRODUCT, COVERAGE);
		}

		return QuoteDecision.approved(ANNUAL_COST, PRODUCT, COVERAGE, BENEFITS);
	}
}
