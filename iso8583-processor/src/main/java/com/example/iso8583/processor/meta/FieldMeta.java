package com.example.iso8583.processor.meta;

import com.example.iso8583.enums.IsoType;

import javax.lang.model.element.VariableElement;

/**
 * @param element      o campo ou getter
 * @param number       Número do campo ISO 8583 (2-128)
 * @param type         Tipo do campo ISO 8583
 * @param length       use 0 quando não aplicável (p.ex. LLVAR)
 * @param required     se o campo é obrigatório
 * @param propertyName ex: "pan" -> usado para gerar getPan()
 */
public record FieldMeta(
		VariableElement element,
		int number,
		IsoType type,
		int length,
		boolean required,
		String propertyName
) {
}
