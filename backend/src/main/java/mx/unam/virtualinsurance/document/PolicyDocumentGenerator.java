package mx.unam.virtualinsurance.document;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PolicyDocumentGenerator {

	private static final DateTimeFormatter DATE =
		DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale.of("es", "MX"));
	private static final String BRAND_LOGO = "branding/virtual-insurance-logo.png";

	private final AcademicContractContent contractContent;
	private final byte[] brandLogo;

	public PolicyDocumentGenerator(AcademicContractContent contractContent) {
		this.contractContent = contractContent;
		this.brandLogo = loadBrandLogo();
	}

	public GeneratedDocument generatePolicy(PolicyPdfData data) {
		return new GeneratedDocument(
			"poliza-" + safeFilename(data.policyNumber()) + ".pdf",
			"application/pdf",
			createPdf("Póliza de seguro " + data.policyNumber(), writer -> {
				writer.policyHeader(
					brandLogo,
					data.policyNumber(),
					policyStatusLabel(data.status())
				);
				writer.notice(contractContent.disclaimer());
				writer.policySectionTitle("Identificación de la póliza");
				writer.fieldGrid(List.of(
					new DocumentField("Folio", data.policyNumber()),
					new DocumentField("Estado", policyStatusLabel(data.status())),
					new DocumentField("Producto", data.productName()),
					new DocumentField("Plan o cobertura", data.coverageName()),
					new DocumentField("Contratante", data.clientName()),
					new DocumentField(data.insuredLabel(), data.insuredValue())
				));
				writer.policySectionTitle("Vigencia y resumen económico");
				writer.fieldGrid(List.of(
					new DocumentField("Inicio de vigencia", formatDate(data.validFrom())),
					new DocumentField("Fin de vigencia", formatDate(data.validUntil())),
					new DocumentField("Prima anual", money(data.annualCost())),
					new DocumentField("IVA informativo", money(data.tax())),
					new DocumentField("Forma de pago", paymentModeLabel(data.paymentMode())),
					new DocumentField("Folio de factura", data.invoiceNumber())
				));
				if (data.cancellationNumber() != null) {
					List<DocumentField> cancellationFields = new ArrayList<>();
					cancellationFields.add(new DocumentField(
						"Folio de cancelación",
						data.cancellationNumber()
					));
					cancellationFields.add(new DocumentField(
						"Estado de solicitud",
						cancellationStatusLabel(data.cancellationStatus())
					));
					cancellationFields.add(new DocumentField(
						"Fecha solicitada",
						formatDate(data.cancellationRequestedDate())
					));
					if (data.cancellationResolvedDate() != null) {
						cancellationFields.add(new DocumentField(
							"Fecha de resolución",
							formatDate(data.cancellationResolvedDate())
						));
						cancellationFields.add(new DocumentField(
							"Observación de resolución",
							data.cancellationResolutionObservation()
						));
					}
					writer.policySectionTitle("Seguimiento de cancelación");
					writer.fieldGrid(cancellationFields);
				}
				for (ContractSection section : contractContent.policySections(data.type())) {
					writer.policySectionTitle(section.title());
					writer.paragraph(section.body());
				}
				writer.documentVersion(contractContent.version());
			})
		);
	}

	public GeneratedDocument generateReceipt(PolicyPdfData data) {
		return new GeneratedDocument(
			"comprobante-" + safeFilename(data.policyNumber()) + ".pdf",
			"application/pdf",
			createPdf("Comprobante academico " + data.policyNumber(), writer -> {
				writer.brand(brandLogo);
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

	private byte[] loadBrandLogo() {
		try {
			return new ClassPathResource(BRAND_LOGO).getContentAsByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException("No fue posible cargar el logotipo institucional.", exception);
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
			case "CANCELACION_SOLICITADA" -> "Cancelación solicitada";
			case "CANCELADA" -> "Cancelada";
			case "VENCIDA" -> "Vencida";
			default -> status;
		};
	}

	private String cancellationStatusLabel(String status) {
		return switch (status) {
			case "SOLICITADA" -> "Cancelación solicitada";
			case "APROBADA" -> "Cancelación aprobada";
			case "RECHAZADA" -> "Cancelación rechazada";
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

	private record DocumentField(String label, String value) {
	}

	private static final class PdfWriter implements AutoCloseable {

		private static final float MARGIN = 54;
		private static final float WIDTH = PDRectangle.A4.getWidth() - (MARGIN * 2);
		private static final float BOTTOM = 58;
		private static final Color NAVY = new Color(15, 44, 89);
		private static final Color CYAN = new Color(2, 132, 199);
		private static final Color EMERALD = new Color(16, 185, 129);
		private static final Color WARNING = new Color(245, 158, 11);
		private static final Color DANGER = new Color(220, 38, 38);
		private static final Color PAGE_BACKGROUND = new Color(248, 250, 252);
		private static final Color WHITE = Color.WHITE;
		private static final Color BORDER = new Color(226, 232, 240);
		private static final Color BODY_TEXT = new Color(51, 65, 85);
		private final PDDocument document;
		private final PDFont cinzelRegular;
		private final PDFont firaRegular;
		private final PDFont firaMedium;
		private final PDFont firaSemiBold;
		private final PDFont monoRegular;
		private final PDFont monoMedium;
		private PDPageContentStream stream;
		private float y;
		private int pageNumber;
		private String policyReference;

		private PdfWriter(PDDocument document) throws IOException {
			this.document = document;
			cinzelRegular = loadFont("fonts/Cinzel-Regular.ttf");
			firaRegular = loadFont("fonts/FiraSans-Regular.ttf");
			firaMedium = loadFont("fonts/FiraSans-Medium.ttf");
			firaSemiBold = loadFont("fonts/FiraSans-SemiBold.ttf");
			monoRegular = loadFont("fonts/JetBrainsMono-Regular.ttf");
			monoMedium = loadFont("fonts/JetBrainsMono-Medium.ttf");
			newPage();
		}

		private PDFont loadFont(String path) throws IOException {
			try (InputStream input = new ClassPathResource(path).getInputStream()) {
				return PDType0Font.load(document, input, true);
			}
		}

		private void brand(byte[] logo) throws IOException {
			float logoWidth = 185;
			PDImageXObject image = PDImageXObject.createFromByteArray(
				document,
				logo,
				"virtual-insurance-logo"
			);
			float logoHeight = logoWidth * image.getHeight() / image.getWidth();
			ensureSpace(logoHeight + 14);
			stream.drawImage(image, MARGIN, y - logoHeight, logoWidth, logoHeight);
			y -= logoHeight + 14;
		}

		private void policyHeader(byte[] logo, String policyNumber, String status)
			throws IOException {
			policyReference = policyNumber;
			float logoWidth = 185;
			PDImageXObject image = PDImageXObject.createFromByteArray(
				document,
				logo,
				"virtual-insurance-logo"
			);
			float logoHeight = logoWidth * image.getHeight() / image.getWidth();
			stream.drawImage(image, MARGIN, y - logoHeight, logoWidth, logoHeight);

			float rightEdge = MARGIN + WIDTH;
			drawRightAlignedText("PÓLIZA DE SEGURO", cinzelRegular, 9, CYAN, rightEdge, y - 4);
			drawRightAlignedText(policyNumber, monoMedium, 12, NAVY, rightEdge, y - 21);
			drawStatusBadge(status, rightEdge, y - 48);

			y -= Math.max(logoHeight, 58) + 15;
			setColor(CYAN);
			stream.addRect(MARGIN, y, WIDTH, 2);
			stream.fill();
			y -= 16;
		}

		private void drawStatusBadge(String status, float rightEdge, float baseline) throws IOException {
			Color background = statusColor(status);
			float badgeWidth = Math.max(75, monoMedium.getStringWidth(status) / 1000 * 8.5f + 18);
			float x = rightEdge - badgeWidth;
			setColor(background);
			stream.addRect(x, baseline - 5, badgeWidth, 19);
			stream.fill();
			drawTextAt(status, monoMedium, 8.5f, WHITE, x + 9, baseline);
		}

		private void subtitle(String value) throws IOException {
			writeLine(value, firaSemiBold, 14, new Color(32, 93, 218), 28);
		}

		private void notice(String value) throws IOException {
			List<String> lines = wrap(value, firaSemiBold, 8.5f, WIDTH - 28);
			float boxHeight = Math.max(42, 20 + (lines.size() * 11));
			ensureSpace(boxHeight + 8);
			setColor(NAVY);
			stream.addRect(MARGIN, y - boxHeight, WIDTH, boxHeight);
			stream.fill();
			float baseline = y - 17;
			for (String line : lines) {
				drawTextAt(line, firaSemiBold, 8.5f, WHITE, MARGIN + 14, baseline);
				baseline -= 11;
			}
			y -= boxHeight + 8;
		}

		private void policySectionTitle(String value) throws IOException {
			ensureSpace(42);
			y -= 10;
			setColor(PAGE_BACKGROUND);
			stream.addRect(MARGIN, y - 22, WIDTH, 27);
			stream.fill();
			setColor(CYAN);
			stream.addRect(MARGIN, y - 22, 4, 27);
			stream.fill();
			drawTextAt(value, firaSemiBold, 11, NAVY, MARGIN + 14, y - 13);
			y -= 34;
		}

		private void fieldGrid(List<DocumentField> fields) throws IOException {
			float gap = 10;
			float columnWidth = (WIDTH - gap) / 2;
			for (int index = 0; index < fields.size(); index += 2) {
				DocumentField left = fields.get(index);
				DocumentField right = index + 1 < fields.size() ? fields.get(index + 1) : null;
				PDFont leftFont = valueFont(left);
				PDFont rightFont = right == null ? firaMedium : valueFont(right);
				List<String> leftLines = wrap(fieldValue(left), leftFont, 9.5f, columnWidth - 20);
				List<String> rightLines = right == null
					? List.of()
					: wrap(fieldValue(right), rightFont, 9.5f, columnWidth - 20);
				int valueLines = Math.max(leftLines.size(), rightLines.size());
				float rowHeight = Math.max(43, 27 + valueLines * 11);
				ensureSpace(rowHeight + 8);
				drawFieldCell(left, leftLines, leftFont, MARGIN, columnWidth, rowHeight);
				if (right != null) {
					drawFieldCell(
						right,
						rightLines,
						rightFont,
						MARGIN + columnWidth + gap,
						columnWidth,
						rowHeight
					);
				}
				y -= rowHeight + 8;
			}
		}

		private void drawFieldCell(
			DocumentField field,
			List<String> valueLines,
			PDFont valueFont,
			float x,
			float width,
			float height
		) throws IOException {
			setColor(WHITE);
			stream.addRect(x, y - height, width, height);
			stream.fill();
			stream.setStrokingColor(
				BORDER.getRed() / 255f,
				BORDER.getGreen() / 255f,
				BORDER.getBlue() / 255f
			);
			stream.addRect(x, y - height, width, height);
			stream.stroke();
			drawTextAt(field.label(), firaSemiBold, 7.5f, BODY_TEXT, x + 10, y - 13);
			float baseline = y - 28;
			Color valueColor = isStatusField(field) ? statusColor(field.value()) : NAVY;
			for (String line : valueLines) {
				drawTextAt(line, valueFont, 9.5f, valueColor, x + 10, baseline);
				baseline -= 11;
			}
		}

		private boolean isStatusField(DocumentField field) {
			return "Estado".equals(field.label()) || "Estado de solicitud".equals(field.label());
		}

		private PDFont valueFont(DocumentField field) {
			String label = field.label();
			if (isStatusField(field)
				|| label.contains("Folio")
				|| label.contains("vigencia")
				|| label.contains("Fecha")
				|| label.contains("Prima")
				|| label.contains("IVA")
				|| label.contains("pago")) {
				return monoMedium;
			}
			return firaMedium;
		}

		private Color statusColor(String status) {
			return switch (status) {
				case "Activa", "Cancelación aprobada" -> EMERALD;
				case "Cancelación solicitada" -> WARNING;
				case "Cancelada", "Cancelación rechazada" -> DANGER;
				default -> BODY_TEXT;
			};
		}

		private String fieldValue(DocumentField field) {
			return field.value() == null || field.value().isBlank() ? "No aplica" : field.value();
		}

		private void documentVersion(String version) throws IOException {
			ensureSpace(34);
			y -= 8;
			writeLine(
				"Versión del documento: " + version,
				monoRegular,
				8,
				BODY_TEXT,
				14
			);
		}

		private void sectionTitle(String value) throws IOException {
			ensureSpace(35);
			y -= 10;
			writeLine(value, firaSemiBold, 12, new Color(15, 23, 42), 20);
		}

		private void keyValue(String label, String value) throws IOException {
			ensureSpace(20);
			String text = label + ": " + (value == null ? "No aplica" : value);
			writeWrapped(text, firaRegular, 10, new Color(51, 65, 85), 14);
		}

		private void paragraph(String value) throws IOException {
			writeWrapped(value, firaRegular, 10, new Color(51, 65, 85), 14);
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
			stream.setCharacterSpacing(characterSpacing(font, size));
			setColor(color);
			stream.newLineAtOffset(MARGIN, y);
			stream.showText(value);
			stream.endText();
			y -= leading;
		}

		private void drawTextAt(
			String value,
			PDFont font,
			float size,
			Color color,
			float x,
			float baseline
		) throws IOException {
			stream.beginText();
			stream.setFont(font, size);
			stream.setCharacterSpacing(characterSpacing(font, size));
			setColor(color);
			stream.newLineAtOffset(x, baseline);
			stream.showText(value);
			stream.endText();
		}

		private float characterSpacing(PDFont font, float size) {
			return font == monoRegular || font == monoMedium ? size * 0.025f : 0;
		}

		private void drawRightAlignedText(
			String value,
			PDFont font,
			float size,
			Color color,
			float rightEdge,
			float baseline
		) throws IOException {
			float width = font.getStringWidth(value) / 1000 * size;
			drawTextAt(value, font, size, color, rightEdge - width, baseline);
		}

		private void writeWrapped(
			String value,
			PDFont font,
			float size,
			Color color,
			float leading
		) throws IOException {
			for (String line : wrap(value, font, size, WIDTH)) {
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
			stream.setFont(policyReference == null ? firaRegular : monoRegular, 8);
			setColor(policyReference == null ? new Color(100, 116, 139) : BODY_TEXT);
			stream.newLineAtOffset(MARGIN, 30);
			String text = policyReference == null
				? "Virtual Insurance - Documento academico - Pagina " + pageNumber
				: "Virtual Insurance | Póliza " + policyReference + " | Página " + pageNumber;
			stream.showText(text);
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

