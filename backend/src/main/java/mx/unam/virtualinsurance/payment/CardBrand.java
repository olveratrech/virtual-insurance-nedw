package mx.unam.virtualinsurance.payment;

import java.util.regex.Pattern;

public enum CardBrand {

	VISA(Pattern.compile("4\\d{15}"), 3),
	MASTERCARD(
		Pattern.compile("(?:5[1-5]\\d{14}|2(?:2(?:2[1-9]|[3-9]\\d)|[3-6]\\d{2}|7(?:[01]\\d|20))\\d{12})"),
		3
	),
	AMEX(Pattern.compile("3[47]\\d{13}"), 4);

	private final Pattern cardNumberPattern;
	private final int securityCodeLength;

	CardBrand(Pattern cardNumberPattern, int securityCodeLength) {
		this.cardNumberPattern = cardNumberPattern;
		this.securityCodeLength = securityCodeLength;
	}

	public boolean matches(String cardNumber) {
		return cardNumber != null && cardNumberPattern.matcher(cardNumber).matches();
	}

	public boolean matchesSecurityCode(String securityCode) {
		return securityCode != null
			&& securityCode.matches("\\d{" + securityCodeLength + "}");
	}

	public int securityCodeLength() {
		return securityCodeLength;
	}
}
