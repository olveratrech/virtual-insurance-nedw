package mx.unam.virtualinsurance.panel;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import mx.unam.virtualinsurance.customer.Client;
import mx.unam.virtualinsurance.customer.ClientRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PanelService {

	private static final DateTimeFormatter DATE =
		DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "MX"));

	private final ClientRepository clientRepository;
	private final PanelRepository panelRepository;

	public PanelService(ClientRepository clientRepository, PanelRepository panelRepository) {
		this.clientRepository = clientRepository;
		this.panelRepository = panelRepository;
	}

	@Transactional(readOnly = true)
	public PanelResponse getPanel(Long clientId) {
		Client client = clientRepository.findById(clientId)
			.orElseThrow(() -> new IllegalStateException("La sesion no corresponde a un cliente."));
		List<PanelActivityResponse> activity = panelRepository.findRecentActivity(clientId)
			.stream()
			.map(this::toResponse)
			.toList();
		return new PanelResponse(client.getName(), activity);
	}

	private PanelActivityResponse toResponse(PanelRepository.ActivityRow row) {
		boolean quote = "QUOTE".equals(row.resourceType());
		boolean auto = "A".equals(row.insuranceType());
		String prefix = quote ? "quote" : "policy";
		return new PanelActivityResponse(
			prefix + "-" + row.resourceId(),
			row.resourceType(),
			row.resourceId(),
			(quote ? "Cotización" : "Póliza") + (auto ? " de auto" : " médica"),
			quote ? "#" + row.reference() : row.reference(),
			row.status(),
			quote ? quoteDetail(row.annualCost(), row.status(), auto) : policyDetail(row),
			(quote ? "/cotizaciones/" : "/polizas/") + row.resourceId()
		);
	}

	private String quoteDetail(BigDecimal annualCost, String status, boolean auto) {
		if (annualCost == null || !"APROBADA".equals(status)) {
			return "Cotización no aprobada";
		}
		String coverage = auto ? "Cobertura amplia" : "Cobertura médica individual";
		return coverage + " · " + String.format(Locale.US, "$%,.2f MXN anuales", annualCost);
	}

	private String policyDetail(PanelRepository.ActivityRow row) {
		return "Vigencia hasta el " + row.validUntil().format(DATE);
	}
}
