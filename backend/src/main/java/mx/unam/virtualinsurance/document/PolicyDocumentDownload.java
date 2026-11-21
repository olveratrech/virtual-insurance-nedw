package mx.unam.virtualinsurance.document;

public record PolicyDocumentDownload(String filename, String mediaType, byte[] content) {
	public PolicyDocumentDownload {
		content = content.clone();
	}

	@Override
	public byte[] content() {
		return content.clone();
	}
}

