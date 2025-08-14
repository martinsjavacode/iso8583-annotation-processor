package com.example.iso8583.utils;

public class Iso8583Utils {
	public String hexToBinary(String hex) {
		StringBuilder binary = new StringBuilder();

		for(char ch : hex.toCharArray()) {
			int decimal = Character.digit(ch, 16);
			String bin = Integer.toBinaryString(decimal);
			binary.append(String.format("%4s", bin).replace(' ', '0'));
		}

		return binary.toString();
	}

	public String binaryToHex(String binary) {
		StringBuilder hex = new StringBuilder();

		for(int i = 0; i < binary.length(); i += 4) {
			String chunk = binary.substring(i, Math.min(i + 4, binary.length()));
			int decimal = Integer.parseInt(chunk, 16);
			hex.append(Integer.toHexString(decimal));
		}

		return hex.toString();
	}
}
