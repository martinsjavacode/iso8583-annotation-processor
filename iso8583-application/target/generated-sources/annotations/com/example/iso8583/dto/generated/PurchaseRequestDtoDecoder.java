package com.example.iso8583.dto.generated;

import com.example.iso8583.contract.IsoMessageDecoder;
import com.example.iso8583.domain.FieldTemplate;
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
  private static Map<Integer, FieldTemplate> createFieldTemplate() {
    Map<Integer, FieldTemplate> template = new HashMap<>();
    template.put(2, new FieldTemplate(IsoType.LLVAR, 0));
    template.put(3, new FieldTemplate(IsoType.NUMERIC, 6));
    template.put(4, new FieldTemplate(IsoType.AMOUNT, 12));
    template.put(7, new FieldTemplate(IsoType.DATE10, 10));
    template.put(11, new FieldTemplate(IsoType.NUMERIC, 6));
    template.put(12, new FieldTemplate(IsoType.TIME, 6));
    template.put(13, new FieldTemplate(IsoType.DATE4, 4));
    template.put(18, new FieldTemplate(IsoType.NUMERIC, 4));
    template.put(22, new FieldTemplate(IsoType.NUMERIC, 3));
    template.put(25, new FieldTemplate(IsoType.NUMERIC, 2));
    template.put(32, new FieldTemplate(IsoType.LLVAR, 0));
    template.put(37, new FieldTemplate(IsoType.ALPHA, 12));
    template.put(41, new FieldTemplate(IsoType.ALPHA, 8));
    template.put(42, new FieldTemplate(IsoType.ALPHA, 15));
    template.put(49, new FieldTemplate(IsoType.NUMERIC, 3));
    return template;
  }

  /**
   * Converte IsoMessage em DTO
   */
  @Override
  public PurchaseRequestDto fromIsoMessage(IsoMessage isoMessage) {
    PurchaseRequestDto result = new PurchaseRequestDto();
    IsoValue<String> fieldPrimaryAccountNumber = (IsoValue<String>) isoMessage.getField(2);
    if (fieldPrimaryAccountNumber != null) {
      result.setPrimaryAccountNumber(fieldPrimaryAccountNumber.value());
    }
    IsoValue<String> fieldProcessingCode = (IsoValue<String>) isoMessage.getField(3);
    if (fieldProcessingCode != null) {
      result.setProcessingCode(fieldProcessingCode.value());
    }
    IsoValue<java.math.BigDecimal> fieldTransactionAmount = (IsoValue<java.math.BigDecimal>) isoMessage.getField(4);
    if (fieldTransactionAmount != null) {
      result.setTransactionAmount(fieldTransactionAmount.value());
    }
    IsoValue<java.time.LocalDateTime> fieldTransmissionDateTime = (IsoValue<java.time.LocalDateTime>) isoMessage.getField(7);
    if (fieldTransmissionDateTime != null) {
      result.setTransmissionDateTime(fieldTransmissionDateTime.value());
    }
    IsoValue<String> fieldSystemTraceAuditNumber = (IsoValue<String>) isoMessage.getField(11);
    if (fieldSystemTraceAuditNumber != null) {
      result.setSystemTraceAuditNumber(fieldSystemTraceAuditNumber.value());
    }
    IsoValue<java.time.LocalTime> fieldLocalTransactionTime = (IsoValue<java.time.LocalTime>) isoMessage.getField(12);
    if (fieldLocalTransactionTime != null) {
      result.setLocalTransactionTime(fieldLocalTransactionTime.value());
    }
    IsoValue<java.time.LocalDate> fieldLocalTransactionDate = (IsoValue<java.time.LocalDate>) isoMessage.getField(13);
    if (fieldLocalTransactionDate != null) {
      result.setLocalTransactionDate(fieldLocalTransactionDate.value());
    }
    IsoValue<String> fieldMerchantCategoryCode = (IsoValue<String>) isoMessage.getField(18);
    if (fieldMerchantCategoryCode != null) {
      result.setMerchantCategoryCode(fieldMerchantCategoryCode.value());
    }
    IsoValue<String> fieldPosEntryMode = (IsoValue<String>) isoMessage.getField(22);
    if (fieldPosEntryMode != null) {
      result.setPosEntryMode(fieldPosEntryMode.value());
    }
    IsoValue<String> fieldPosConditionCode = (IsoValue<String>) isoMessage.getField(25);
    if (fieldPosConditionCode != null) {
      result.setPosConditionCode(fieldPosConditionCode.value());
    }
    IsoValue<String> fieldAcquiringInstitutionId = (IsoValue<String>) isoMessage.getField(32);
    if (fieldAcquiringInstitutionId != null) {
      result.setAcquiringInstitutionId(fieldAcquiringInstitutionId.value());
    }
    IsoValue<String> fieldRetrievalReferenceNumber = (IsoValue<String>) isoMessage.getField(37);
    if (fieldRetrievalReferenceNumber != null) {
      result.setRetrievalReferenceNumber(fieldRetrievalReferenceNumber.value());
    }
    IsoValue<String> fieldTerminalId = (IsoValue<String>) isoMessage.getField(41);
    if (fieldTerminalId != null) {
      result.setTerminalId(fieldTerminalId.value());
    }
    IsoValue<String> fieldMerchantId = (IsoValue<String>) isoMessage.getField(42);
    if (fieldMerchantId != null) {
      result.setMerchantId(fieldMerchantId.value());
    }
    IsoValue<String> fieldCurrencyCode = (IsoValue<String>) isoMessage.getField(49);
    if (fieldCurrencyCode != null) {
      result.setCurrencyCode(fieldCurrencyCode.value());
    }
    return result;
  }

  /**
   * Decodifica bytes ISO 8583 em DTO
   */
  @Override
  public PurchaseRequestDto decode(String data) {
    IsoDecoder decoder = new IsoDecoder();
    Map<Integer, FieldTemplate> template = createFieldTemplate();
    IsoMessage message = decoder.decodeWithTemplate(data, template);
    return fromIsoMessage(message);
  }

  /**
   * Decodifica bytes ISO 8583 em DTO usando factory específica
   */
  @Override
  public PurchaseRequestDto decode(String data, IsoMessageFactory factory) {
    IsoMessage message = factory.decode(data);
    return fromIsoMessage(message);
  }
}
