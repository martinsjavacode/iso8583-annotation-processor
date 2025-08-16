package com.example.iso8583.service;

import com.example.iso8583.domain.FieldTemplate;
import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.enums.IsoType;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory para criar e configurar mensagens ISO 8583.
 * Permite definir templates de campos e criar mensagens pré-configuradas.
 */
public class IsoMessageFactory {

	private final Map<Integer, FieldTemplate> fieldTemplates = new HashMap<>();
	private final IsoEncoder encoder = new IsoEncoder();
	private final IsoDecoder decoder = new IsoDecoder();

	/**
	 * Adiciona um template de campo à factory
	 */
	public IsoMessageFactory addFieldTemplate(int fieldNumber, IsoType type, int length) {
		fieldTemplates.put(fieldNumber, new FieldTemplate(type, length));
		return this;
	}

	/**
	 * Codifica uma mensagem usando esta factory
	 */
	public byte[] encode(IsoMessage message) {
		return encoder.encode(message);
	}

	/**
	 * Decodifica uma mensagem a partir de string
	 */
	public IsoMessage decode(String messageStr) {
		if (fieldTemplates.isEmpty()) {
			return decoder.decode(messageStr);
		} else {
			return decoder.decodeWithTemplate(messageStr, fieldTemplates);
		}
	}
}
