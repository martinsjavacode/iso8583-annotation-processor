package com.example.iso8583.dto.generated;

import com.example.iso8583.contract.IsoMessageEncoder;
import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.enums.IsoType;
import com.example.iso8583.service.IsoEncoder;
import com.example.iso8583.service.IsoMessageFactory;

/**
 * Encoder gerado automaticamente para PurchaseRequestDto.
 * Não edite este arquivo.
 */
public final class PurchaseRequestDtoEncoder implements IsoMessageEncoder<PurchaseRequestDto> {
  /**
   * Valida campos obrigatórios
   */
  private PurchaseRequestDto validateRequirements(PurchaseRequestDto dto) {
    if (dto.getPrimaryAccountNumber() == null) {
      throw new IllegalArgumentException("Field primaryAccountNumber (DE 2) is required");
    }
    if (dto.getProcessingCode() == null) {
      throw new IllegalArgumentException("Field processingCode (DE 3) is required");
    }
    if (dto.getTransactionAmount() == null) {
      throw new IllegalArgumentException("Field transactionAmount (DE 4) is required");
    }
    if (dto.getTransmissionDateTime() == null) {
      throw new IllegalArgumentException("Field transmissionDateTime (DE 7) is required");
    }
    if (dto.getSystemTraceAuditNumber() == null) {
      throw new IllegalArgumentException("Field systemTraceAuditNumber (DE 11) is required");
    }
    if (dto.getLocalTransactionTime() == null) {
      throw new IllegalArgumentException("Field localTransactionTime (DE 12) is required");
    }
    if (dto.getLocalTransactionDate() == null) {
      throw new IllegalArgumentException("Field localTransactionDate (DE 13) is required");
    }
    if (dto.getPosEntryMode() == null) {
      throw new IllegalArgumentException("Field posEntryMode (DE 22) is required");
    }
    if (dto.getPosConditionCode() == null) {
      throw new IllegalArgumentException("Field posConditionCode (DE 25) is required");
    }
    if (dto.getRetrievalReferenceNumber() == null) {
      throw new IllegalArgumentException("Field retrievalReferenceNumber (DE 37) is required");
    }
    if (dto.getTerminalId() == null) {
      throw new IllegalArgumentException("Field terminalId (DE 41) is required");
    }
    if (dto.getMerchantId() == null) {
      throw new IllegalArgumentException("Field merchantId (DE 42) is required");
    }
    if (dto.getCurrencyCode() == null) {
      throw new IllegalArgumentException("Field currencyCode (DE 49) is required");
    }
    return dto;
  }

  /**
   * Converte DTO em IsoMessage
   */
  @Override
  public IsoMessage toIsoMessage(PurchaseRequestDto dto) {
    validateRequirements(dto);
    IsoMessage message = new IsoMessage(512);
    message.setField(2, dto.getPrimaryAccountNumber(), IsoType.LLVAR, 0);
    message.setField(3, dto.getProcessingCode(), IsoType.NUMERIC, 6);
    message.setField(4, dto.getTransactionAmount(), IsoType.AMOUNT, 12);
    message.setField(7, dto.getTransmissionDateTime(), IsoType.DATE10, 10);
    message.setField(11, dto.getSystemTraceAuditNumber(), IsoType.NUMERIC, 6);
    message.setField(12, dto.getLocalTransactionTime(), IsoType.TIME, 6);
    message.setField(13, dto.getLocalTransactionDate(), IsoType.DATE4, 4);
    message.setField(18, dto.getMerchantCategoryCode(), IsoType.NUMERIC, 4);
    message.setField(22, dto.getPosEntryMode(), IsoType.NUMERIC, 3);
    message.setField(25, dto.getPosConditionCode(), IsoType.NUMERIC, 2);
    message.setField(32, dto.getAcquiringInstitutionId(), IsoType.LLVAR, 0);
    message.setField(37, dto.getRetrievalReferenceNumber(), IsoType.ALPHA, 12);
    message.setField(41, dto.getTerminalId(), IsoType.ALPHA, 8);
    message.setField(42, dto.getMerchantId(), IsoType.ALPHA, 15);
    message.setField(49, dto.getCurrencyCode(), IsoType.NUMERIC, 3);
    return message;
  }

  /**
   * Codifica DTO em bytes ISO 8583
   */
  @Override
  public byte[] encode(PurchaseRequestDto dto) {
    IsoMessage message = toIsoMessage(dto);
    IsoEncoder encoder = new IsoEncoder();
    return encoder.encode(message);
  }

  /**
   * Codifica DTO em bytes ISO 8583 usando factory específica
   */
  @Override
  public byte[] encode(PurchaseRequestDto dto, IsoMessageFactory factory) {
    IsoMessage message = toIsoMessage(dto);
    return factory.encode(message);
  }
}
