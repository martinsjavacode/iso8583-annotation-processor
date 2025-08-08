package com.example.iso8583.dto;

import com.example.iso8583.annotation.Iso8583Field;
import com.example.iso8583.annotation.Iso8583Message;
import com.solab.iso8583.IsoType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representando uma requisição de compra ISO 8583.
 * Esta classe será processada pelo Annotation Processor para gerar
 * automaticamente um parser/builder de mensagens ISO 8583.
 */
@Iso8583Message(
    version = 0, // ISO 8583-1987
    clazz = 2,   // Authorization
    function = 0, // Request
    source = 0   // Acquirer
)
public class PurchaseRequestDto {

    @Iso8583Field(
        field = 2,
        length = 19,
        type = IsoType.NUMERIC,
        required = true,
        description = "Primary Account Number (PAN)"
    )
    private String primaryAccountNumber;

    @Iso8583Field(
        field = 3,
        length = 6,
        type = IsoType.NUMERIC,
        required = true,
        description = "Processing Code"
    )
    private String processingCode;

    @Iso8583Field(
        field = 4,
        length = 12,
        type = IsoType.AMOUNT,
        required = true,
        description = "Transaction Amount"
    )
    private BigDecimal transactionAmount;

    @Iso8583Field(
        field = 7,
        length = 10,
        type = IsoType.DATE10,
        required = true,
        description = "Transmission Date and Time"
    )
    private LocalDateTime transmissionDateTime;

    @Iso8583Field(
        field = 11,
        length = 6,
        type = IsoType.NUMERIC,
        required = true,
        description = "System Trace Audit Number"
    )
    private String systemTraceAuditNumber;

    @Iso8583Field(
        field = 12,
        length = 6,
        type = IsoType.TIME,
        required = true,
        description = "Local Transaction Time"
    )
    private LocalDateTime localTransactionTime;

    @Iso8583Field(
        field = 13,
        length = 4,
        type = IsoType.DATE4,
        required = true,
        description = "Local Transaction Date"
    )
    private LocalDateTime localTransactionDate;

    @Iso8583Field(
        field = 18,
        length = 4,
        type = IsoType.NUMERIC,
        required = false,
        description = "Merchant Category Code"
    )
    private String merchantCategoryCode;

    @Iso8583Field(
        field = 22,
        length = 3,
        type = IsoType.NUMERIC,
        required = true,
        description = "Point of Service Entry Mode"
    )
    private String posEntryMode;

    @Iso8583Field(
        field = 25,
        length = 2,
        type = IsoType.NUMERIC,
        required = true,
        description = "Point of Service Condition Code"
    )
    private String posConditionCode;

    @Iso8583Field(
        field = 32,
        length = 11,
        type = IsoType.LLVAR,
        required = false,
        description = "Acquiring Institution Identification Code"
    )
    private String acquiringInstitutionId;

    @Iso8583Field(
        field = 37,
        length = 12,
        type = IsoType.ALPHA,
        required = true,
        description = "Retrieval Reference Number"
    )
    private String retrievalReferenceNumber;

    @Iso8583Field(
        field = 41,
        length = 8,
        type = IsoType.ALPHA,
        required = true,
        description = "Card Acceptor Terminal Identification"
    )
    private String terminalId;

    @Iso8583Field(
        field = 42,
        length = 15,
        type = IsoType.ALPHA,
        required = true,
        description = "Card Acceptor Identification Code"
    )
    private String merchantId;

    @Iso8583Field(
        field = 49,
        length = 3,
        type = IsoType.NUMERIC,
        required = true,
        description = "Transaction Currency Code"
    )
    private String currencyCode;

    // Construtores
    public PurchaseRequestDto() {}

    public PurchaseRequestDto(String primaryAccountNumber, String processingCode, 
                             BigDecimal transactionAmount, String systemTraceAuditNumber) {
        this.primaryAccountNumber = primaryAccountNumber;
        this.processingCode = processingCode;
        this.transactionAmount = transactionAmount;
        this.systemTraceAuditNumber = systemTraceAuditNumber;
        this.transmissionDateTime = LocalDateTime.now();
        this.localTransactionTime = LocalDateTime.now();
        this.localTransactionDate = LocalDateTime.now();
    }

    // Getters e Setters
    public String getPrimaryAccountNumber() {
        return primaryAccountNumber;
    }

    public void setPrimaryAccountNumber(String primaryAccountNumber) {
        this.primaryAccountNumber = primaryAccountNumber;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public LocalDateTime getTransmissionDateTime() {
        return transmissionDateTime;
    }

    public void setTransmissionDateTime(LocalDateTime transmissionDateTime) {
        this.transmissionDateTime = transmissionDateTime;
    }

    public String getSystemTraceAuditNumber() {
        return systemTraceAuditNumber;
    }

    public void setSystemTraceAuditNumber(String systemTraceAuditNumber) {
        this.systemTraceAuditNumber = systemTraceAuditNumber;
    }

    public LocalDateTime getLocalTransactionTime() {
        return localTransactionTime;
    }

    public void setLocalTransactionTime(LocalDateTime localTransactionTime) {
        this.localTransactionTime = localTransactionTime;
    }

    public LocalDateTime getLocalTransactionDate() {
        return localTransactionDate;
    }

    public void setLocalTransactionDate(LocalDateTime localTransactionDate) {
        this.localTransactionDate = localTransactionDate;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public String getPosEntryMode() {
        return posEntryMode;
    }

    public void setPosEntryMode(String posEntryMode) {
        this.posEntryMode = posEntryMode;
    }

    public String getPosConditionCode() {
        return posConditionCode;
    }

    public void setPosConditionCode(String posConditionCode) {
        this.posConditionCode = posConditionCode;
    }

    public String getAcquiringInstitutionId() {
        return acquiringInstitutionId;
    }

    public void setAcquiringInstitutionId(String acquiringInstitutionId) {
        this.acquiringInstitutionId = acquiringInstitutionId;
    }

    public String getRetrievalReferenceNumber() {
        return retrievalReferenceNumber;
    }

    public void setRetrievalReferenceNumber(String retrievalReferenceNumber) {
        this.retrievalReferenceNumber = retrievalReferenceNumber;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public String toString() {
        return "PurchaseRequestDto{" +
                "primaryAccountNumber='" + maskPan(primaryAccountNumber) + '\'' +
                ", processingCode='" + processingCode + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", systemTraceAuditNumber='" + systemTraceAuditNumber + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                '}';
    }

    /**
     * Mascara o PAN para logs de segurança
     */
    private String maskPan(String pan) {
        if (pan == null || pan.length() < 8) {
            return "****";
        }
        return pan.substring(0, 4) + "****" + pan.substring(pan.length() - 4);
    }
}
