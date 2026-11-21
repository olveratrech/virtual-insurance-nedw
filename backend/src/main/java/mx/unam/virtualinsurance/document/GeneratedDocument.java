package mx.unam.virtualinsurance.document;

public record GeneratedDocument(String filename, String mediaType, byte[] content) {
	public GeneratedDocument {
		content = content.clone();
	}

	@Override
	public byte[] content() {
		return content.clone();
	}
}

