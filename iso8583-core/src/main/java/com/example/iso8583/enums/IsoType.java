package com.example.iso8583.enums;

/**
 * Enum que define os tipos de dados suportados pelo protocolo ISO 8583.
 * Cada tipo tem características específicas de codificação e validação.
 */
public enum IsoType {
	/**
	 * Campo numérico com tamanho fixo (ex: "000123" para length=6)
	 */
	NUMERIC(true),

	/**
	 * Campo alfanumérico com tamanho fixo (ex: "ABC123" para length=6)
	 */
	ALPHA(true),

	/**
	 * Campo de tamanho variável com 2 dígitos de comprimento (max 99 chars)
	 */
	LLVAR(false),

	/**
	 * Campo de tamanho variável com 3 dígitos de comprimento (max 999 chars)
	 */
	LLLVAR(false),

	/**
	 * Campo de tamanho variável com 4 dígitos de comprimento (max 9999 chars)
	 */
	LLLLVAR(false),

	/**
	 * Data no formato YYYYMMDDHHMMSS (14 dígitos)
	 */
	DATE14(false),

	/**
	 * Data no formato MMDDHHMMSS (10 dígitos)
	 */
	DATE10(false),

	/**
	 * Data no formato MMDD (4 dígitos)
	 */
	DATE4(false),

	/**
	 * Data no formato YYMMDDHHMMSS (12 dígitos)
	 */
	DATE12(false),

	/**
	 * Data no formato YYMMDD (6 dígitos)
	 */
	DATE6(false),

	/**
	 * Data de expiração no formato YYMM (4 dígitos)
	 */
	DATE_EXP(false),

	/**
	 * Hora no formato HHMMSS (6 dígitos)
	 */
	TIME(false),

	/**
	 * Valor monetário (12 dígitos numéricos, sem ponto decimal)
	 */
	AMOUNT(false),

	/**
	 * Campo binário com tamanho fixo
	 */
	BINARY(true),

	/**
	 * Campo binário de tamanho variável com 2 dígitos de comprimento
	 */
	LLBIN(false),

	/**
	 * Campo binário de tamanho variável com 3 dígitos de comprimento
	 */
	LLLBIN(false),

	/**
	 * Campo binário de tamanho variável com 4 dígitos de comprimento
	 */
	LLLLBIN(false),

	/**
	 * Campo BCD binário de tamanho variável com 2 dígitos de comprimento
	 */
	LLBCDBIN(false),

	/**
	 * Campo BCD binário de tamanho variável com 3 dígitos de comprimento
	 */
	LLLBCDBIN(false),

	/**
	 * Campo BCD binário de tamanho variável com 4 dígitos de comprimento
	 */
	LLLLBCDBIN(false);

	private final boolean needsLength;

	IsoType(boolean needsLength) {
		this.needsLength = needsLength;
	}

	/**
	 * Indica se o tipo precisa de um comprimento definido na anotação
	 */
	public boolean needsLength() {
		return needsLength;
	}

	/**
	 * Retorna o número de dígitos usados para indicar o comprimento em tipos variáveis
	 */
	public int getLengthDigits() {
		return switch (this) {
			case LLVAR, LLBIN, LLBCDBIN -> 2;
			case LLLVAR, LLLBIN, LLLBCDBIN -> 3;
			case LLLLVAR, LLLLBIN, LLLLBCDBIN -> 4;
			default -> 0;
		};
	}

	/**
	 * Retorna o comprimento fixo
	 */
	public int getFixedLength() {
		return switch (this) {
			case DATE14 -> 14;
			case DATE12, AMOUNT -> 12;
			case DATE10 -> 10;
			case DATE6, TIME -> 6;
			case DATE4, DATE_EXP -> 4;
			default -> 0;
		};
	}
}
