package com.example.iso8583.dto.generated;

import com.example.iso8583.contract.IsoMessageDecoder;
import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.domain.IsoValue;
import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.enums.IsoType;
import com.example.iso8583.service.IsoDecoder;
import com.example.iso8583.service.IsoMessageFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Decoder gerado automaticamente para PurchaseRequestDto.
 * Não edite este arquivo.
 */
public final class PurchaseRequestDtoDecoder implements IsoMessageDecoder<PurchaseRequestDto> {
  /**
   * Cria template de campos para decodificação
   */
  private static Map<Integer, IsoDecoder.FieldTemplate> createFieldTemplate() {
    Map<Integer, IsoDecoder.FieldTemplate> template = new HashMap<>();
    template.put(2, new IsoDecoder.FieldTemplate(IsoType.LLVAR, 0));
    template.put(3, new IsoDecoder.FieldTemplate(IsoType.NUMERIC, 6));
    template.put(4, new IsoDecoder.FieldTemplate(IsoType.AMOUNT, 12));
    template.put(7, new IsoDecoder.FieldTemplate(IsoType.DATE10, 10));
    template.put(11, new IsoDecoder.FieldTemplate(IsoType.NUMERIC, 6));
    template.put(12, new IsoDecoder.FieldTemplate(IsoType.TIME, 6));
    template.put(13, new IsoDecoder.FieldTemplate(IsoType.DATE4, 4));
    template.put(18, new IsoDecoder.FieldTemplate(IsoType.NUMERIC, 4));
    template.put(22, new IsoDecoder.FieldTemplate(IsoType.NUMERIC, 3));
    template.put(25, new IsoDecoder.FieldTemplate(IsoType.NUMERIC, 2));
    template.put(32, new IsoDecoder.FieldTemplate(IsoType.LLVAR, 0));
    template.put(37, new IsoDecoder.FieldTemplate(IsoType.ALPHA, 12));
    template.put(41, new IsoDecoder.FieldTemplate(IsoType.ALPHA, 8));
    template.put(42, new IsoDecoder.FieldTemplate(IsoType.ALPHA, 15));
    template.put(49, new IsoDecoder.FieldTemplate(IsoType.NUMERIC, 3));
    return template;
  }

  /**
   * Converte IsoMessage em DTO
   */
  @Override
  public PurchaseRequestDto fromIsoMessage(IsoMessage isoMessage) {
    PurchaseRequestDto result = new PurchaseRequestDto();
    IsoValue<?> field2 = isoMessage.getField(2);
    if (field2 != null) {
      result.setPrimaryAccountNumber(field2.value().toString());
    }
    IsoValue<?> field3 = isoMessage.getField(3);
    if (field3 != null) {
      result.setProcessingCode(field3.value().toString());
    }
    IsoValue<?> field4 = isoMessage.getField(4);
    if (field4 != null) {
      result.setTransactionAmount(new java.math.BigDecimal(field4.value().toString()));
    }
    IsoValue<?> field7 = isoMessage.getField(7);
    if (field7 != null) {
      result.setTransmissionDateTime(java.time.LocalDateTime.parse(field7.value().toString()));
    }
    IsoValue<?> field11 = isoMessage.getField(11);
    if (field11 != null) {
      result.setSystemTraceAuditNumber(field11.value().toString());
    }
    IsoValue<?> field12 = isoMessage.getField(12);
    if (field12 != null) {
      result.setLocalTransactionTime(java.time.LocalDateTime.parse(field12.value().toString()));
    }
    IsoValue<?> field13 = isoMessage.getField(13);
    if (field13 != null) {
      result.setLocalTransactionDate(java.time.LocalDateTime.parse(field13.value().toString()));
    }
    IsoValue<?> field18 = isoMessage.getField(18);
    if (field18 != null) {
      result.setMerchantCategoryCode(field18.value().toString());
    }
    IsoValue<?> field22 = isoMessage.getField(22);
    if (field22 != null) {
      result.setPosEntryMode(field22.value().toString());
    }
    IsoValue<?> field25 = isoMessage.getField(25);
    if (field25 != null) {
      result.setPosConditionCode(field25.value().toString());
    }
    IsoValue<?> field32 = isoMessage.getField(32);
    if (field32 != null) {
      result.setAcquiringInstitutionId(field32.value().toString());
    }
    IsoValue<?> field37 = isoMessage.getField(37);
    if (field37 != null) {
      result.setRetrievalReferenceNumber(field37.value().toString());
    }
    IsoValue<?> field41 = isoMessage.getField(41);
    if (field41 != null) {
      result.setTerminalId(field41.value().toString());
    }
    IsoValue<?> field42 = isoMessage.getField(42);
    if (field42 != null) {
      result.setMerchantId(field42.value().toString());
    }
    IsoValue<?> field49 = isoMessage.getField(49);
    if (field49 != null) {
      result.setCurrencyCode(field49.value().toString());
    }
    return result;
  }

  /**
   * Decodifica bytes ISO 8583 em DTO
   */
  @Override
  public PurchaseRequestDto decode(byte[] data) {
    IsoDecoder decoder = new IsoDecoder();
    Map<Integer, IsoDecoder.FieldTemplate> template = createFieldTemplate();
    IsoMessage message = decoder.decodeWithTemplate(data, template);
    return fromIsoMessage(message);
  }

  /**
   * Decodifica bytes ISO 8583 em DTO usando factory específica
   */
  @Override
  public PurchaseRequestDto decode(byte[] data, IsoMessageFactory factory) {
    IsoMessage message = factory.decode(data);
    return fromIsoMessage(message);
  }
}
