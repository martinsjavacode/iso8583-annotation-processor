package com.example.iso8583.processor.meta;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * @param type        DTO
 * @param mti
 * @param fields
 * @param packageName do DTO
 * @param simpleName  ex: PurchaseRequestDto
 */
public record MessageMeta(TypeElement type, int mti, List<FieldMeta> fields, String packageName, String simpleName) {
}
