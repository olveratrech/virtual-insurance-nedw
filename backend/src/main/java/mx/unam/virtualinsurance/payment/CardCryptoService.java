package mx.unam.virtualinsurance.payment;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CardCryptoService {

	private static final int IV_LENGTH = 12;
	private static final int TAG_LENGTH_BITS = 128;
	private final SecureRandom secureRandom = new SecureRandom();
	private final String encodedKey;

	public CardCryptoService(@Value("${app.payment.card-encryption-key:}") String encodedKey) {
		this.encodedKey = encodedKey;
	}

	public byte[] encrypt(String cardNumber) {
		byte[] key = decodeKey();
		byte[] iv = new byte[IV_LENGTH];
		secureRandom.nextBytes(iv);
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(
				Cipher.ENCRYPT_MODE,
				new SecretKeySpec(key, "AES"),
				new GCMParameterSpec(TAG_LENGTH_BITS, iv)
			);
			byte[] encrypted = cipher.doFinal(cardNumber.getBytes(StandardCharsets.US_ASCII));
			return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("No fue posible cifrar el instrumento de pago.", exception);
		}
	}

	private byte[] decodeKey() {
		if (encodedKey == null || encodedKey.isBlank()) {
			throw new IllegalStateException("Falta configurar VI_CARD_ENCRYPTION_KEY.");
		}
		try {
			byte[] key = Base64.getDecoder().decode(encodedKey);
			if (key.length != 32) {
				throw new IllegalStateException("VI_CARD_ENCRYPTION_KEY debe contener 32 bytes en Base64.");
			}
			return key;
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("VI_CARD_ENCRYPTION_KEY no contiene Base64 valido.", exception);
		}
	}
}

