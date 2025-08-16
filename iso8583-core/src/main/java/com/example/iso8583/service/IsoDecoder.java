package com.example.iso8583.service;

import com.example.iso8583.domain.DecodeResult;
import com.example.iso8583.domain.FieldTemplate;
import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.domain.IsoValue;
import com.example.iso8583.enums.IsoType;
import com.example.iso8583.utils.BitmapUtils;
import com.example.iso8583.utils.FieldFormatter;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Decoder para converter bytes em mensagens IsoMessage seguindo o protocolo ISO 8583.
 * <p>
 * Este decoder básico extrai MTI e bitmap, mas para decodificar campos específicos
 * é necessário conhecer a configuração de cada campo (tipo, tamanho, etc.).
 */
public class IsoDecoder {
	/**
	 * Decodifica uma mensagem ISO 8583 a partir de string
	 */
	public IsoMessage decode(String messageStr) {
		if (messageStr == null || messageStr.length() < 20) {
			throw new IllegalArgumentException("Invalid message string");
		}

		// 1. Extrai MTI (4 caracteres)
		String mti = messageStr.substring(0, 4);
		IsoMessage message = new IsoMessage(mti);

		// 2. Primary Bitmap (próximos 16 dígitos chars hex = 64 bits)
		BitSet bitmap = BitmapUtils.hexToBitSet(messageStr.substring(4, 20));

		// 3. Verifica se há bitmap secondário
		if (bitmap.get(1)) {
			bitmap = BitmapUtils.hexToBitSet(messageStr.substring(4, 36));
		}

		// Salva bitmap no objeto
		message.setBitmap(bitmap);

		// Retorna mensagem com MTI e bitmap decodificados
		// Para decodificar campos específicos, use decodeWithTemplate()
		return message;
	}

	/**
	 * Decodifica uma mensagem usando um template de configuração de campos
	 */
	public IsoMessage decodeWithTemplate(byte[] data, Map<Integer, FieldTemplate> fieldTemplates) {
		String messageStr = Arrays.toString(data);
		return decodeWithTemplate(messageStr, fieldTemplates);
	}

	/**
	 * Decodifica uma mensagem usando um template de configuração de campos
	 */
	public IsoMessage decodeWithTemplate(String messageStr, Map<Integer, FieldTemplate> fieldTemplates) {
		// Primeiro decodifica MTI e bitmap
		IsoMessage message = decode(messageStr);

		// Calcula posição inicial dos campos de dados
		AtomicInteger currentIndex = new AtomicInteger(20); // MTI + bitmap primário

		// Decodifica cada campo presente no bitmap
		message.getBitmap()
			.stream()
			.forEach(fieldNumber -> {
				FieldTemplate template = fieldTemplates.get(fieldNumber);
				if (template == null) {
					throw new IllegalStateException(
						"No template found for field " + fieldNumber + " but field is present in bitmap"
					);
				}

				try {
					final var result = decodeField(messageStr, currentIndex.get(), template);

					IsoValue<?> isoValue = new IsoValue<>(
						template.type(),
						result.value(),
						template.length()
					);

					message.setField(fieldNumber, isoValue);
					currentIndex.set(result.nextIndex());
				} catch (Exception e) {
					throw new RuntimeException(
						"Error decoding field " + fieldNumber + " at position " + currentIndex, e
					);
				}
			});

		return message;
	}

	/**
	 * Decodifica um campo individual
	 */
	private DecodeResult<?> decodeField(String messageStr, int startIndex, FieldTemplate template) {
		IsoType type = template.type();
		int declaredLength = template.length();

		final var result = switch (type) {
			case LLVAR, LLBIN, LLBCDBIN -> decodeVariableField(messageStr, startIndex, 2);
			case LLLVAR, LLLBIN, LLLBCDBIN -> decodeVariableField(messageStr, startIndex, 3);
			case LLLLVAR, LLLLBIN, LLLLBCDBIN -> decodeVariableField(messageStr, startIndex, 4);
			case NUMERIC, ALPHA, BINARY -> decodeFixedField(messageStr, startIndex, declaredLength);
			case DATE14 -> decodeFixedField(messageStr, startIndex, 14);
			case DATE12, AMOUNT -> decodeFixedField(messageStr, startIndex, 12);
			case DATE10 -> decodeFixedField(messageStr, startIndex, 10);
			case DATE6, TIME -> decodeFixedField(messageStr, startIndex, 6);
			case DATE4, DATE_EXP -> decodeFixedField(messageStr, startIndex, 4);
		};

		final var fieldParsed = FieldFormatter.decodingValue(result.value().toString(), template.type());

		return new DecodeResult<>(fieldParsed, result.nextIndex());

	}

	/**
	 * Decodifica campo de tamanho fixo
	 */
	private DecodeResult<?> decodeFixedField(String messageStr, int startIndex, int length) {
		final var nextIndex = startIndex + length;
		if (messageStr.length() < nextIndex) {
			throw new IllegalArgumentException(
				String.format("Message too short for fixed field of length %d at position %d",
					length, startIndex)
			);
		}

		String value = messageStr.substring(startIndex, nextIndex);
		return new DecodeResult<>(value, nextIndex);
	}

	/**
	 * Decodifica campo de tamanho variável
	 */
	private DecodeResult<?> decodeVariableField(String messageStr, int startIndex, int lengthDigits) {
		int contentStart = startIndex + lengthDigits;
		if (messageStr.length() < contentStart) {
			throw new IllegalArgumentException(
				String.format("Message too short for variable field length indicator (%d digits) at position %d",
					lengthDigits, startIndex)
			);
		}

		// Extrai o comprimento
		String lengthStr = messageStr.substring(startIndex, contentStart);
		try {
			final int contentLength = Integer.parseInt(lengthStr);
			final int nextIndex = contentStart + contentLength;
			if (messageStr.length() < nextIndex) {
				throw new IllegalArgumentException(
					String.format("Message too short for variable field content (length %d) at position %d",
						contentLength, contentStart)
				);
			}

			String value = messageStr.substring(contentStart, nextIndex);
			return new DecodeResult<>(value, nextIndex);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid length indicator: " + lengthStr, e);
		}
	}
}
