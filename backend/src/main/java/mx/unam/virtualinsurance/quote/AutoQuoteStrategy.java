package mx.unam.virtualinsurance.quote;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AutoQuoteStrategy implements QuoteStrategy<AutoQuoteRequest> {

	private static final String PRODUCT = "Seguro de auto";
	private static final String COVERAGE = "Cobertura amplia";
	private static final BigDecimal ANNUAL_COST = new BigDecimal("12480.00");
	private static final List<String> BENEFITS = List.of(
		"Danos materiales",
		"Robo total",
		"Responsabilidad civil",
		"Asistencia vial"
	);
	private static final String REJECTION_REASON =
		"Los antecedentes declarados no cumplen las reglas de aceptación para este producto.";

	@Override
	public QuoteDecision evaluate(AutoQuoteRequest request) {
		if (request.criminalRecord() || request.negativePoints() >= 12) {
			return QuoteDecision.rejected(REJECTION_REASON, PRODUCT, COVERAGE);
		}

		return QuoteDecision.approved(ANNUAL_COST, PRODUCT, COVERAGE, BENEFITS);
	}
}
