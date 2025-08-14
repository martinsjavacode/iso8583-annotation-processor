package com.example.iso8583.processor.meta;

import com.solab.iso8583.IsoType;

import javax.lang.model.element.VariableElement;

/**
 * @param element      o campo ou getter
 * @param number       Número do campo ISO 8583 (1-128)
 * @param type      "NUMERIC", "LLVAR", "ALPHA" etc.
 * @param length       use 0 quando não aplicável (p.ex. LLVAR)
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
