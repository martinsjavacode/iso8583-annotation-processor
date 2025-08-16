package com.example.iso8583.utils;

import com.example.iso8583.enums.IsoType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.BitSet;

/**
 * Utilitários para formatação e parsing de campos ISO 8583.
 * <p>
 * Esta classe fornece métodos para:
 * - ENCODING: Converter objetos Java para strings formatadas ISO 8583
 * - DECODING: Converter strings ISO 8583 de volta para objetos Java
 */
public final class FieldFormatter {

	private static final String FORMAT_DATE14 = "yyyyMMddHHmmss";
	private static final String FORMAT_DATE12 = "yyMMddHHmmss";
	private static final String FORMAT_DATE10 = "MMddHHmmss";
	private static final String FORMAT_DATE6 = "yyMMdd";
	private static final String FORMAT_DATE4 = "MMdd";
	private static final String FORMAT_DATE_EXP = "yyMM";
	private static final String FORMAT_TIME = "HHmmss";

	private FieldFormatter() {
		// Utility class
	}

	// ========================================
	// ENCODING METHODS (Java Object -> ISO String)
	// ========================================

	/**
	 * Metodo principal para encoding: converte um objeto Java para string formatada ISO 8583.
	 *
	 * @param value  O valor a ser formatado
	 * @param type   O tipo ISO 8583 do campo
	 * @param length O comprimento esperado do campo (quando aplicável)
	 * @return String formatada de acordo com o padrão ISO 8583
	 */
	public static <T> String encodingValue(T value, IsoType type, int length) {
		if (value == null) {
			return "";
		}

		return switch (type) {
			case NUMERIC -> formatNumeric(value, length);
			case ALPHA -> formatAlpha(value, length);
			case LLVAR, LLLVAR, LLLLVAR -> formatVariable(value, type);
			case DATE14 -> formatDate(value, FORMAT_DATE14);
			case DATE12 -> formatDate(value, FORMAT_DATE12);
			case DATE10 -> formatDate(value, FORMAT_DATE10);
			case DATE6 -> formatDate(value, FORMAT_DATE6);
			case DATE4 -> formatDate(value, FORMAT_DATE4);
			case DATE_EXP -> formatDate(value, FORMAT_DATE_EXP);
			case TIME -> formatTime(value);
			case AMOUNT -> formatAmount(value);
			case BINARY -> formatBinary(value, length);
			case LLBIN, LLLBIN, LLLLBIN -> formatVariableBinary(value, type);
			default -> value.toString();
		};
	}

	// --- Encoding Helper Methods ---

	/**
	 * Formata campo numérico com padding de zeros à esquerda.
	 */
	private static <T> String formatNumeric(T value, int length) {
		String str = value.toString().replaceAll("\\D", ""); // só números
		return String.format("%0" + length + "d", Long.parseLong(str)); // padding com zeros
	}

	/**
	 * Formata campo alfanumérico com padding de espaços à direita.
	 */
	private static <T> String formatAlpha(T value, int length) {
		String str = value.toString();
		return String.format("%-" + length + "s", str); // padding com espaços
	}

	/**
	 * Formata campo de tamanho variável com indicador de comprimento.
	 */
	private static <T> String formatVariable(T value, IsoType type) {
		String str = value.toString();
		int len = str.length();
		return switch (type) {
			case LLVAR -> String.format("%02d", len) + str;
			case LLLVAR -> String.format("%03d", len) + str;
			case LLLLVAR -> String.format("%04d", len) + str;
			default -> str;
		};
	}

	/**
	 * Formata data no formato especificado.
	 */
	private static <T> String formatDate(T value, String format) {
		LocalDateTime dateTime = convertToDateTime(value);
		return dateTime.format(DateTimeFormatter.ofPattern(format));
	}

	/**
	 * Formata hora no formato HHMMSS (6 dígitos).
	 */
	private static <T> String formatTime(T value) {
		final var pattern = DateTimeFormatter.ofPattern(FORMAT_TIME);
		if (value instanceof LocalTime ldt) {
			return ldt.format(pattern);
		} else if (value instanceof String str) {
			// Tenta parsear string como ISO time
			return LocalTime.parse(str)
				.format(pattern);
		} else {
			throw new IllegalArgumentException("Time field must be LocalTime or ISO time string");
		}
	}

	/**
	 * Formata valor monetário como 12 dígitos sem separadores decimais.
	 */
	private static <T> String formatAmount(T value) {
		// 12 dígitos, sem vírgula
		String str = value.toString().replace(".", "").replace(",", "");
		return String.format("%012d", Long.parseLong(str));
	}

	/**
	 * Formata campo binário como string hexadecimal.
	 */
	private static <T> String formatBinary(T value, int length) {
		// valor em hex, padding com 0 até o tamanho
		String hex = value.toString().replaceAll("\\s+", "");
		int expectedLength = length * 2; // cada byte = 2 hex
		return String.format("%-" + expectedLength + "s", hex).replace(' ', '0');
	}

	/**
	 * Formata campo binário de tamanho variável com indicador de comprimento.
	 */
	private static <T> String formatVariableBinary(T value, IsoType type) {
		String hex = value.toString().replaceAll("\\s+", "");
		int lenBytes = hex.length() / 2;
		return switch (type) {
			case LLBIN -> String.format("%02d", lenBytes) + hex;
			case LLLBIN -> String.format("%03d", lenBytes) + hex;
			case LLLLBIN -> String.format("%04d", lenBytes) + hex;
			default -> hex;
		};
	}

	/**
	 * Converte objeto para LocalDateTime para formatação de datas.
	 */
	private static <T> LocalDateTime convertToDateTime(T value) {
		return switch (value) {
			case LocalDateTime ldt -> ldt;
			case LocalDate ld -> ld.atStartOfDay();
			case String str ->
				// Tenta parsear string como ISO date
				LocalDateTime.parse(str);
			case null, default ->
				throw new IllegalArgumentException("Date field must be LocalDateTime or ISO date string");
		};
	}

	// ========================================
	// DECODING METHODS (ISO String -> Java Object)
	// ========================================

	/**
	 * Metodo principal para decoding: converte string ISO 8583 para objeto Java tipado.
	 *
	 * @param raw  A string ISO 8583 a ser parseada
	 * @param type O tipo ISO 8583 do campo
	 * @return Objeto Java do tipo especificado
	 */
	@SuppressWarnings("unchecked")
	public static <T> T decodingValue(String raw, IsoType type) {
		return switch (type) {
			case NUMERIC, ALPHA, LLVAR, LLLVAR, LLLLVAR -> (T) raw.trim();
			case DATE14 -> (T) parseDateTime(raw, FORMAT_DATE14);
			case DATE12 -> (T) parseDateTime(raw, FORMAT_DATE12);
			case DATE10 -> (T) parseDateTime(raw, FORMAT_DATE10);
			case DATE6 -> (T) parseDate(raw, FORMAT_DATE6);
			case DATE4 -> (T) parseDate(raw, FORMAT_DATE4);
			case DATE_EXP -> (T) parseDate(raw, FORMAT_DATE_EXP);
			case TIME -> (T) parseTime(raw);
			case AMOUNT -> (T) parseAmount(raw);
			case BINARY -> (T) parseBinary(raw);
			case LLBIN, LLLBIN, LLLLBIN -> (T) parseVariableBinary(raw, type);
			default -> (T) raw;
		};
	}

	// --- Decoding Helper Methods ---

	/**
	 * Parseia campo de data de acordo com o padrão especificado.
	 */
	private static LocalDateTime parseDateTime(String raw, String pattern) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		TemporalAccessor parsed = formatter.parse(raw);

		int year = parsed.isSupported(ChronoField.YEAR) ? parsed.get(ChronoField.YEAR) : LocalDateTime.now().getYear();
		int month = parsed.isSupported(ChronoField.MONTH_OF_YEAR) ? parsed.get(ChronoField.MONTH_OF_YEAR) : 1;
		int day = parsed.isSupported(ChronoField.DAY_OF_MONTH) ? parsed.get(ChronoField.DAY_OF_MONTH) : 1;
		int hour = parsed.isSupported(ChronoField.HOUR_OF_DAY) ? parsed.get(ChronoField.HOUR_OF_DAY) : 0;
		int minute = parsed.isSupported(ChronoField.MINUTE_OF_HOUR) ? parsed.get(ChronoField.MINUTE_OF_HOUR) : 0;
		int second = parsed.isSupported(ChronoField.SECOND_OF_MINUTE) ? parsed.get(ChronoField.SECOND_OF_MINUTE) : 0;

		return LocalDateTime.of(year, month, day, hour, minute, second);
	}

	/**
	 * Parseia campo de data de acordo com o padrão especificado.
	 */
	private static LocalDate parseDate(String raw, String pattern) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		TemporalAccessor parsed = formatter.parse(raw);

		int year = parsed.isSupported(ChronoField.YEAR) ? parsed.get(ChronoField.YEAR) : LocalDateTime.now().getYear();
		int month = parsed.isSupported(ChronoField.MONTH_OF_YEAR) ? parsed.get(ChronoField.MONTH_OF_YEAR) : 1;
		int day = parsed.isSupported(ChronoField.DAY_OF_MONTH) ? parsed.get(ChronoField.DAY_OF_MONTH) : 1;

		return LocalDate.of(year, month, day);
	}

	/**
	 * Parseia campo de time de acordo com o padrão especificado.
	 */
	private static LocalTime parseTime(String raw) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_TIME);
		TemporalAccessor parsed = formatter.parse(raw);
		return LocalTime.from(parsed);
	}

	/**
	 * Parseia valor monetário convertendo de centavos para valor decimal.
	 */
	private static BigDecimal parseAmount(String raw) {
		return new BigDecimal(raw).movePointLeft(2);
	}

	/**
	 * Parseia campo binário convertendo string hexadecimal para BitSet.
	 */
	private static BitSet parseBinary(String rawHex) {
		return BitSet.valueOf(hexStringToByteArray(rawHex));
	}

	/**
	 * Parseia campo binário de tamanho variável extraindo dados após indicador de comprimento.
	 */
	private static BitSet parseVariableBinary(String rawData, IsoType type) {
		int lengthIndicatorSize = switch (type) {
			case LLBIN -> 2;   // 2 dígitos (bytes)
			case LLLBIN -> 3;  // 3 dígitos
			case LLLLBIN -> 4; // 4 dígitos
			default -> throw new IllegalArgumentException("Tipo inválido para parseVariableBinary: " + type);
		};

		// Extrai o tamanho em bytes a partir do indicador
		int lengthInBytes = Integer.parseInt(rawData.substring(0, lengthIndicatorSize));

		// Extrai a parte binária em hex
		String hexData = rawData.substring(lengthIndicatorSize, lengthIndicatorSize + (lengthInBytes * 2));

		return BitSet.valueOf(hexStringToByteArray(hexData));
	}

	// ========================================
	// UTILITY METHODS
	// ========================================

	/**
	 * Converte string hexadecimal para array de bytes.
	 */
	private static byte[] hexStringToByteArray(String value) {
		int len = value.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4)
				+ Character.digit(value.charAt(i + 1), 16));
		}
		return data;
	}

}
