package com.example.iso8583.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Iso8583Utils {
	public static String hexToBinary(String hex) {
		StringBuilder binary = new StringBuilder();

		for(char ch : hex.toCharArray()) {
			int decimal = Character.digit(ch, 16);
			String bin = Integer.toBinaryString(decimal);
			binary.append(String.format("%4s", bin).replace(' ', '0'));
		}

		return binary.toString();
	}

	public static String binaryToHex(String binary) {
		StringBuilder hex = new StringBuilder();

		for(int i = 0; i < binary.length(); i += 4) {
			String chunk = binary.substring(i, Math.min(i + 4, binary.length()));
			int decimal = Integer.parseInt(chunk, 16);
			hex.append(Integer.toHexString(decimal));
		}

		return hex.toString();
	}

	public static String bigDecimalToIsoAmount(BigDecimal value) {
		if (value == null) return null;

		final var scaled = value.setScale(2, RoundingMode.FLOOR);
		final var numeric = value.movePointRight(2).toPlainString();
		return String.format("%012d",  Long.valueOf(numeric));

	}
}
