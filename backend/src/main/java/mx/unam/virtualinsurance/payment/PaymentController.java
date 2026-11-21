package mx.unam.virtualinsurance.payment;

import mx.unam.virtualinsurance.auth.ClientPrincipal;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cotizaciones/{quoteId}/intentos-pago")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ResponseEntity<PaymentResponse> pay(
		@AuthenticationPrincipal ClientPrincipal principal,
		@PathVariable Long quoteId,
		@RequestHeader("Idempotency-Key") String idempotencyKey,
		@RequestBody @Valid PaymentRequest request
	) {
		PaymentResponse response = paymentService.pay(
			principal.clientId(), quoteId, idempotencyKey, request
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}

