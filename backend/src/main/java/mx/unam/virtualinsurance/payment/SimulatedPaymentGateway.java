package mx.unam.virtualinsurance.payment;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

	private final JdbcTemplate jdbcTemplate;

	public SimulatedPaymentGateway(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public GatewayPaymentResult authorize(GatewayPaymentCommand command) {
		List<TestInstrument> instruments = jdbcTemplate.query(
			"""
			select instrumento_pago_prueba_id, ultimos_cuatro, saldo_disponible,
			       estatus, anio_expiracion, mes_expiracion, respuesta_forzada
			  from instrumento_pago_prueba
			 where numero_hash = lower(rawtohex(standard_hash(?, 'SHA256')))
			 for update
			""",
			(rs, rowNumber) -> new TestInstrument(
				rs.getLong("instrumento_pago_prueba_id"),
				rs.getString("ultimos_cuatro"),
				rs.getBigDecimal("saldo_disponible"),
				rs.getString("estatus"),
				rs.getInt("anio_expiracion"),
				rs.getInt("mes_expiracion"),
				rs.getString("respuesta_forzada")
			),
			command.cardNumber()
		);

		if (instruments.isEmpty()) {
			return GatewayPaymentResult.rejected(
				"TEST_CARD_NOT_FOUND",
				"Utiliza una de las tarjetas habilitadas para el entorno de evaluación.",
				command.cardNumber().substring(command.cardNumber().length() - 4)
			);
		}

		TestInstrument instrument = instruments.getFirst();
		if (YearMonth.of(instrument.expirationYear(), instrument.expirationMonth())
			.isBefore(YearMonth.now())) {
			return rejected("EXPIRED_CARD", "La tarjeta está vencida.", instrument);
		}
		if (instrument.expirationMonth() != command.expirationMonth()
			|| instrument.expirationYear() != command.expirationYear()) {
			return rejected("CARD_DATA_MISMATCH", "El vencimiento no coincide con la tarjeta proporcionada.", instrument);
		}
		if (!"123".equals(command.securityCode())) {
			return rejected("INVALID_SECURITY_CODE", "El CVV no es válido.", instrument);
		}
		if ("BLOCKED".equals(instrument.status())) {
			return rejected("BLOCKED_CARD", "La tarjeta está bloqueada.", instrument);
		}
		if (instrument.forcedResponse() != null) {
			return rejected("PROCESSOR_DECLINED", "El procesador de pagos rechazó la operación.", instrument);
		}
		if (instrument.balance().compareTo(command.amount()) < 0) {
			return rejected("INSUFFICIENT_FUNDS", "La tarjeta no tiene fondos suficientes.", instrument);
		}

		int updated = jdbcTemplate.update(
			"""
			update instrumento_pago_prueba
			   set saldo_disponible = saldo_disponible - ?,
			       version = version + 1
			 where instrumento_pago_prueba_id = ?
			   and saldo_disponible >= ?
			""",
			command.amount(), instrument.id(), command.amount()
		);
		if (updated != 1) {
			return rejected("INSUFFICIENT_FUNDS", "La tarjeta no tiene fondos suficientes.", instrument);
		}

		String authorizationCode = "AUTH-%010d".formatted(command.paymentNumber());
		return GatewayPaymentResult.approved(authorizationCode, instrument.lastFour());
	}

	private GatewayPaymentResult rejected(String code, String message, TestInstrument instrument) {
		return GatewayPaymentResult.rejected(code, message, instrument.lastFour());
	}

	private record TestInstrument(
		long id,
		String lastFour,
		BigDecimal balance,
		String status,
		int expirationYear,
		int expirationMonth,
		String forcedResponse
	) {
	}
}

