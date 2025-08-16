package com.example.iso8583.domain;


import com.example.iso8583.enums.IsoType;

import java.util.BitSet;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Representa uma mensagem ISO 8583 completa com MTI, bitmap e campos de dados.
 */
public class IsoMessage {
	private final Map<Integer, IsoValue<?>> fields = new TreeMap<>();
	private String mti;
	private BitSet bitmap; // bin

	public IsoMessage(String mti) {
		this.mti = mti;
	}

	public String getMti() {
		return mti;
	}

	public void setMti(String mti) {
		this.mti = mti;
	}

	public BitSet getBitmap() {
		return bitmap;
	}

	public void setBitmap(BitSet bitmap) {
		this.bitmap = bitmap;
	}

	/**
	 * Remove um campo da mensagem
	 */
	public void removeField(int fieldNumber) {
		fields.remove(fieldNumber);
	}

	/**
	 * Retorna o valor de um campo
	 */
	public IsoValue<?> getField(int fieldNumber) {
		return fields.get(fieldNumber);
	}

	/**
	 * Retorna todos os campos da mensagem
	 */
	public Map<Integer, IsoValue<?>> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	/**
	 * Define um campo da mensagem
	 */
	public void setField(int fieldNumber, IsoValue<?> value) {
		if (fieldNumber < 2 || fieldNumber > 128) {
			throw new IllegalArgumentException("Field number must be between 2 and 128");
		}

		if (value != null) {
			fields.put(fieldNumber, value);
		} else {
			fields.remove(fieldNumber);
		}
	}

	/**
	 * Define um campo da mensagem com tipo e comprimento
	 */
	public <T> void setField(int fieldNumber, T value, IsoType type, int length) {
		if (value == null) {
			removeField(fieldNumber);
			return;
		}

		IsoValue<?> isoValue = new IsoValue<>(type, value, length);
		setField(fieldNumber, isoValue);
	}

	/**
	 * Valida a estrutura básica da mensagem
	 */
	public void validate() {
		final var i = Integer.parseInt(mti);
		if (i < 0 || i > 9999) {
			throw new IllegalStateException("Invalid MTI: " + mti);
		}

		// Verifica consistência do bitmap
		for (Integer fieldNumber : fields.keySet()) {
			if (!bitmap.get(fieldNumber)) {
				throw new IllegalStateException(
					"Field " + fieldNumber + " is present but not marked in bitmap"
				);
			}
		}

		// Verifica se bitmap secundário está correto
		boolean hasSecondaryFields = fields.keySet()
			.stream()
			.anyMatch(field -> field > 64);
		if (hasSecondaryFields && !bitmap.get(1)) {
			throw new IllegalStateException("Secondary fields present but bit 1 not set");
		}
	}
}
