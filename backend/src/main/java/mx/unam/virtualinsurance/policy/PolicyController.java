package mx.unam.virtualinsurance.policy;

import mx.unam.virtualinsurance.auth.ClientPrincipal;
import mx.unam.virtualinsurance.document.PolicyDocumentDownload;
import mx.unam.virtualinsurance.document.PolicyDocumentService;

import java.nio.charset.StandardCharsets;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/polizas")
public class PolicyController {

	private final PolicyService policyService;
	private final PolicyDocumentService documentService;

	public PolicyController(PolicyService policyService, PolicyDocumentService documentService) {
		this.policyService = policyService;
		this.documentService = documentService;
	}

	@GetMapping("/{policyId}")
	public PolicyResponse findPolicy(
		@AuthenticationPrincipal ClientPrincipal principal,
		@PathVariable Long policyId
	) {
		return policyService.findPolicy(principal.clientId(), policyId);
	}

	@GetMapping("/{policyId}/documentos/{documentId}/contenido")
	public ResponseEntity<byte[]> downloadDocument(
		@AuthenticationPrincipal ClientPrincipal principal,
		@PathVariable Long policyId,
		@PathVariable Long documentId
	) {
		PolicyDocumentDownload document = documentService.download(
			principal.clientId(), policyId, documentId
		);
		ContentDisposition disposition = ContentDisposition.attachment()
			.filename(document.filename(), StandardCharsets.UTF_8)
			.build();
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(document.mediaType()))
			.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
			.cacheControl(CacheControl.noStore())
			.body(document.content());
	}
}

