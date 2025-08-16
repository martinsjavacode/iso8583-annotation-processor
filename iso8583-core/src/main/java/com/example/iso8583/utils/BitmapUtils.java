package com.example.iso8583.utils;

import java.util.BitSet;

public final class BitmapUtils {
	private BitmapUtils() {
		// Utility class
	}

	/**
	 * Gera o bitmap ISO 8583 completo (primário e secundário) em hexadecimal
	 *
	 * @param bits BitSet com campos ativos (1-128)
	 * @return String hexadecimal do bitmap
	 */
	public static String toIsoBitmapHex(BitSet bits) {
		// Verifica se existe secundário (qualquer campo > 64)
		boolean hasSecondary = bits.stream().anyMatch(b -> b >= 65 && b <= 128);

		// Se houver secundário, liga o bit 1 do primário
		if (hasSecondary) {
			bits.set(1); // bit 1 indica bitmap secundário
		}

		// Quantidade de bytes: 8 para primário, +8 se houver secundário
		int totalBits = hasSecondary ? 128 : 64;
		int totalBytes = totalBits / 8;
		byte[] bytes = new byte[totalBytes];

		// Preenche os bytes
		for (int i = 0; i < totalBits; i++) {
			if (bits.get(i + 1)) { // ISO 8583 conta campos a partir de 1
				int byteIndex = i / 8;
				int bitIndex = 7 - (i % 8); // MSB primeiro
				bytes[byteIndex] |= (1 << bitIndex);
			}
		}

		// Converte para hexadecimal
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	/**
	 * Converte um bitmap ISO 8583 hexadecimal em BitSet
	 *
	 * @param hexBitmap String hexadecimal do bitmap (primário ou primário+secundário)
	 * @return BitSet com campos ativos (1-128)
	 */
	public static BitSet hexToBitSet(String hexBitmap) {
		hexBitmap = hexBitmap.replaceAll("\\s+", ""); // remove espaços
		int totalBytes = hexBitmap.length() / 2;
		BitSet bits = new BitSet(totalBytes * 8);

		for (int i = 0; i < totalBytes; i++) {
			String byteHex = hexBitmap.substring(i * 2, i * 2 + 2);
			int b = Integer.parseInt(byteHex, 16);

			for (int bit = 0; bit < 8; bit++) {
				if ((b & (1 << (7 - bit))) != 0) { // MSB primeiro
					bits.set(i * 8 + bit + 1); // ISO 8583 campos começam em 1
				}
			}
		}

		return bits;
	}


}
