package mx.unam.virtualinsurance.auth;

import java.io.IOException;
import java.util.Set;

import mx.unam.virtualinsurance.shared.error.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PhotoValidator {

	private static final long MAX_SIZE = 2L * 1024 * 1024;
	private static final Set<String> SUPPORTED_TYPES = Set.of("image/jpeg", "image/png");

	public byte[] validateAndRead(MultipartFile photo) {
		if (photo == null || photo.isEmpty()) {
			throw unsupported("La fotografia es obligatoria.");
		}
		if (photo.getSize() > MAX_SIZE) {
			throw unsupported("La fotografia supera el tamano permitido de 2 MiB.");
		}
		if (!SUPPORTED_TYPES.contains(photo.getContentType())) {
			throw unsupported("La fotografia debe ser JPEG o PNG.");
		}

		try {
			byte[] content = photo.getBytes();
			if (!matchesDeclaredType(content, photo.getContentType())) {
				throw unsupported("El contenido no coincide con el tipo declarado de la fotografia.");
			}
			return content;
		} catch (IOException exception) {
			throw unsupported("No fue posible leer la fotografia.");
		}
	}

	private boolean matchesDeclaredType(byte[] content, String contentType) {
		return switch (contentType) {
			case "image/png" -> isPng(content);
			case "image/jpeg" -> isJpeg(content);
			default -> false;
		};
	}

	private boolean isPng(byte[] content) {
		byte[] signature = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
		if (content.length < signature.length) {
			return false;
		}
		for (int index = 0; index < signature.length; index++) {
			if (content[index] != signature[index]) {
				return false;
			}
		}
		return true;
	}

	private boolean isJpeg(byte[] content) {
		return content.length >= 3
			&& content[0] == (byte) 0xff
			&& content[1] == (byte) 0xd8
			&& content[2] == (byte) 0xff;
	}

	private ApiException unsupported(String detail) {
		return new ApiException(
			HttpStatus.UNPROCESSABLE_CONTENT,
			"UNSUPPORTED_FILE",
			"La fotografia no es valida",
			detail,
			"unsupported-file"
		);
	}
}
