package mx.unam.virtualinsurance.quote;

import jakarta.validation.Valid;

import mx.unam.virtualinsurance.auth.ClientPrincipal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cotizaciones")
public class QuoteController {

	private final QuoteService quoteService;

	public QuoteController(QuoteService quoteService) {
		this.quoteService = quoteService;
	}

	@PostMapping("/auto")
	public ResponseEntity<QuoteResponse> createAutoQuote(
		@AuthenticationPrincipal ClientPrincipal principal,
		@RequestBody @Valid AutoQuoteRequest request
	) {
		QuoteResponse response = quoteService.createAutoQuote(principal.clientId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/medicas")
	public ResponseEntity<QuoteResponse> createMedicalQuote(
		@AuthenticationPrincipal ClientPrincipal principal,
		@RequestBody @Valid MedicalQuoteRequest request
	) {
		QuoteResponse response = quoteService.createMedicalQuote(principal.clientId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{quoteId}")
	public QuoteResponse findQuote(
		@AuthenticationPrincipal ClientPrincipal principal,
		@PathVariable Long quoteId
	) {
		return quoteService.findQuote(principal.clientId(), quoteId);
	}
}
