package mx.unam.virtualinsurance.document;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class PolicyDocumentGenerator {

	private static final PDFont REGULAR =
		new PDType1Font(Standard14Fonts.FontName.HELVETICA);
	private static final PDFont BOLD =
		new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
	private static final DateTimeFormatter DATE =
		DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale.of("es", "MX"));

	private final AcademicContractContent contractContent;

	public PolicyDocumentGenerator(AcademicContractContent contractContent) {
		this.contractContent = contractContent;
	}

	public GeneratedDocument generatePolicy(PolicyPdfData data) {
		return new GeneratedDocument(
			"poliza-" + safeFilename(data.policyNumber()) + ".pdf",
			"application/pdf",
			createPdf("Poliza academica " + data.policyNumber(), writer -> {
				writer.title("VIRTUAL INSURANCE");
				writer.subtitle("Poliza academica " + data.policyNumber());
				writer.notice(contractContent.disclaimer());
				writer.sectionTitle("Datos de la poliza");
				writer.keyValue("Folio", data.policyNumber());
				writer.keyValue("Estatus", policyStatusLabel(data.status()));
				if (data.cancellationNumber() != null) {
					writer.sectionTitle("Cancelacion");
					writer.keyValue("Folio de cancelacion", data.cancellationNumber());
					writer.keyValue(
						"Estado de solicitud",
						cancellationStatusLabel(data.cancellationStatus())
					);
					writer.keyValue(
						"Fecha solicitada",
						formatDate(data.cancellationRequestedDate())
					);
					if (data.cancellationResolvedDate() != null) {
						writer.keyValue(
							"Fecha de resolucion",
							formatDate(data.cancellationResolvedDate())
						);
						writer.keyValue(
							"Observacion de resolucion",
							data.cancellationResolutionObservation()
						);
					}
				}
				writer.sectionTitle("Datos de cobertura");
				writer.keyValue("Producto", data.productName());
				writer.keyValue("Cobertura", data.coverageName());
				writer.keyValue("Contratante", data.clientName());
				writer.keyValue(data.insuredLabel(), data.insuredValue());
				writer.keyValue("Inicio de vigencia", formatDate(data.validFrom()));
				writer.keyValue("Fin de vigencia", formatDate(data.validUntil()));
				writer.keyValue("Costo anual", money(data.annualCost()));
				writer.keyValue("IVA informativo", money(data.tax()));
				writer.keyValue("Modalidad", paymentModeLabel(data.paymentMode()));
				writer.keyValue("Folio de factura", data.invoiceNumber());
				for (ContractSection section : contractContent.policySections(data.type())) {
					writer.sectionTitle(section.title());
					writer.paragraph(section.body());
				}
				writer.paragraph("Version de plantilla: " + contractContent.version());
			})
		);
	}

	public GeneratedDocument generateReceipt(PolicyPdfData data) {
		return new GeneratedDocument(
			"comprobante-" + safeFilename(data.policyNumber()) + ".pdf",
			"application/pdf",
			createPdf("Comprobante academico " + data.policyNumber(), writer -> {
				writer.title("VIRTUAL INSURANCE");
				writer.subtitle("Comprobante academico de pago");
				writer.notice(contractContent.disclaimer());
				writer.sectionTitle("Operacion");
				writer.keyValue("Poliza", data.policyNumber());
				writer.keyValue("Contratante", data.clientName());
				writer.keyValue("Producto", data.productName());
				writer.keyValue("Modalidad", paymentModeLabel(data.paymentMode()));
				writer.keyValue("Importe aplicado", money(data.paymentAmount()));
				writer.keyValue("Moneda", "MXN");
				writer.keyValue("Autorizacion simulada", data.authorizationCode());
				writer.keyValue("Fecha de vigencia", formatDate(data.validFrom()));
				writer.sectionTitle("Alcance");
				writer.paragraph(
					"El importe fue procesado por una pasarela academica con instrumentos ficticios. "
						+ "Este comprobante no acredita un cargo bancario ni tiene efectos fiscales."
				);
				writer.paragraph("Version de plantilla: " + contractContent.version());
			})
		);
	}

	private byte[] createPdf(String title, PdfContent content) {
		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			PDDocumentInformation information = new PDDocumentInformation();
			information.setTitle(ascii(title));
			information.setAuthor("Virtual Insurance - UNAM");
			information.setSubject("Documento academico de demostracion");
			document.setDocumentInformation(information);
			try (PdfWriter writer = new PdfWriter(document)) {
				content.write(writer);
			}
			document.save(output);
			return output.toByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException("No fue posible generar el documento PDF.", exception);
		}
	}

	private String money(BigDecimal value) {
		return String.format(Locale.US, "$%,.2f MXN", value);
	}

	private String formatDate(LocalDate value) {
		return value.format(DATE);
	}

	private String paymentModeLabel(String paymentMode) {
		return switch (paymentMode) {
			case "MONTHLY" -> "Mensual";
			case "ANNUAL" -> "Anual";
			default -> paymentMode;
		};
	}

	private String policyStatusLabel(String status) {
		return switch (status) {
			case "ACTIVA" -> "Activa";
			case "CANCELACION_SOLICITADA" -> "Cancelacion solicitada";
			case "CANCELADA" -> "Cancelada";
			case "VENCIDA" -> "Vencida";
			default -> status;
		};
	}

	private String cancellationStatusLabel(String status) {
		return switch (status) {
			case "SOLICITADA" -> "Cancelacion solicitada";
			case "APROBADA" -> "Cancelacion aprobada";
			case "RECHAZADA" -> "Cancelacion rechazada";
			default -> status;
		};
	}

	private String safeFilename(String value) {
		return value.replaceAll("[^A-Za-z0-9_-]", "-");
	}

	private static String ascii(String value) {
		String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
			.replaceAll("\\p{M}", "");
		return normalized.replaceAll("[^\\x20-\\x7E]", "?");
	}

	@FunctionalInterface
	private interface PdfContent {
		void write(PdfWriter writer) throws IOException;
	}

	private static final class PdfWriter implements AutoCloseable {

		private static final float MARGIN = 54;
		private static final float WIDTH = PDRectangle.A4.getWidth() - (MARGIN * 2);
		private static final float BOTTOM = 58;
		private final PDDocument document;
		private PDPageContentStream stream;
		private float y;
		private int pageNumber;

		private PdfWriter(PDDocument document) throws IOException {
			this.document = document;
			newPage();
		}

		private void title(String value) throws IOException {
			writeLine(value, BOLD, 20, new Color(22, 45, 84), 26);
		}

		private void subtitle(String value) throws IOException {
			writeLine(value, BOLD, 14, new Color(32, 93, 218), 28);
		}

		private void notice(String value) throws IOException {
			ensureSpace(56);
			setColor(new Color(255, 247, 230));
			stream.addRect(MARGIN, y - 40, WIDTH, 48);
			stream.fill();
			y -= 8;
			writeWrapped(value, BOLD, 9, new Color(137, 55, 16), 12);
			y -= 12;
		}

		private void sectionTitle(String value) throws IOException {
			ensureSpace(35);
			y -= 10;
			writeLine(value, BOLD, 12, new Color(15, 23, 42), 20);
		}

		private void keyValue(String label, String value) throws IOException {
			ensureSpace(20);
			String text = label + ": " + (value == null ? "No aplica" : value);
			writeWrapped(text, REGULAR, 10, new Color(51, 65, 85), 14);
		}

		private void paragraph(String value) throws IOException {
			writeWrapped(value, REGULAR, 10, new Color(51, 65, 85), 14);
			y -= 5;
		}

		private void writeLine(
			String value,
			PDFont font,
			float size,
			Color color,
			float leading
		) throws IOException {
			ensureSpace(leading);
			stream.beginText();
			stream.setFont(font, size);
			setColor(color);
			stream.newLineAtOffset(MARGIN, y);
			stream.showText(ascii(value));
			stream.endText();
			y -= leading;
		}

		private void writeWrapped(
			String value,
			PDFont font,
			float size,
			Color color,
			float leading
		) throws IOException {
			for (String line : wrap(ascii(value), font, size, WIDTH)) {
				writeLine(line, font, size, color, leading);
			}
		}

		private List<String> wrap(String value, PDFont font, float size, float maxWidth)
			throws IOException {
			List<String> lines = new ArrayList<>();
			StringBuilder current = new StringBuilder();
			for (String word : value.split("\\s+")) {
				String candidate = current.isEmpty() ? word : current + " " + word;
				float width = font.getStringWidth(candidate) / 1000 * size;
				if (width > maxWidth && !current.isEmpty()) {
					lines.add(current.toString());
					current = new StringBuilder(word);
				} else {
					current = new StringBuilder(candidate);
				}
			}
			if (!current.isEmpty()) lines.add(current.toString());
			return lines;
		}

		private void ensureSpace(float required) throws IOException {
			if (y - required < BOTTOM) newPage();
		}

		private void newPage() throws IOException {
			if (stream != null) {
				footer();
				stream.close();
			}
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);
			stream = new PDPageContentStream(document, page);
			pageNumber++;
			y = PDRectangle.A4.getHeight() - MARGIN;
		}

		private void footer() throws IOException {
			stream.beginText();
			stream.setFont(REGULAR, 8);
			setColor(new Color(100, 116, 139));
			stream.newLineAtOffset(MARGIN, 30);
			stream.showText("Virtual Insurance - Documento academico - Pagina " + pageNumber);
			stream.endText();
		}

		private void setColor(Color color) throws IOException {
			stream.setNonStrokingColor(
				color.getRed() / 255f,
				color.getGreen() / 255f,
				color.getBlue() / 255f
			);
		}

		@Override
		public void close() throws IOException {
			footer();
			stream.close();
		}
	}
}

