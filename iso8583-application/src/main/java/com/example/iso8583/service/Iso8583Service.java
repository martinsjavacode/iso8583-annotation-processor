package com.example.iso8583.service;

import com.example.iso8583.contract.EncoderRegistry;
import com.example.iso8583.contract.IsoMessageEncoder;
import com.example.iso8583.contract.IsoMessageDecoder;
import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.generated.GeneratedIso8583Registry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Serviço que demonstra o uso da nossa implementação ISO 8583.
 * 
 * Nota: Este serviço será atualizado automaticamente quando o código for gerado
 * pelo annotation processor. Por enquanto, fornece métodos básicos.
 */
@Service
public class Iso8583Service {

     private final GeneratedIso8583Registry registry;

    public Iso8583Service() {
        registry = new GeneratedIso8583Registry();
    }

    /**
     * Exemplo de codificação de uma mensagem de compra
     * TODO: Implementar após geração do código
     */
    public byte[] encodePurchaseRequest(PurchaseRequestDto dto) {
        // Implementação temporária
        throw new UnsupportedOperationException("Encoder will be available after code generation");
    }

    /**
     * Exemplo de decodificação de uma mensagem de compra
     * TODO: Implementar após geração do código
     */
    public PurchaseRequestDto decodePurchaseRequest(byte[] data) {
        // Implementação temporária
        throw new UnsupportedOperationException("Decoder will be available after code generation");
    }

    /**
     * Cria um exemplo de DTO de compra para testes
     */
    public PurchaseRequestDto createSamplePurchaseRequest() {
        PurchaseRequestDto dto = new PurchaseRequestDto();
        
        // Campos obrigatórios
        dto.setPrimaryAccountNumber("4111111111111111");
        dto.setProcessingCode("000000");
        dto.setTransactionAmount(new BigDecimal("100.50"));
        dto.setTransmissionDateTime(LocalDateTime.now());
        dto.setSystemTraceAuditNumber("123456");
        dto.setLocalTransactionTime(LocalDateTime.now());
        dto.setLocalTransactionDate(LocalDateTime.now());
        dto.setPosEntryMode("012");
        dto.setPosConditionCode("00");
        dto.setRetrievalReferenceNumber("123456789012");
        dto.setTerminalId("TERM0001");
        dto.setMerchantId("MERCHANT000001");
        dto.setCurrencyCode("986"); // BRL
        
        // Campos opcionais
        dto.setMerchantCategoryCode("5411");
        dto.setAcquiringInstitutionId("12345");
        
        return dto;
    }

    /**
     * Demonstra o ciclo completo: DTO -> bytes -> DTO
     * TODO: Implementar após geração do código
     */
    public void demonstrateFullCycle() {
        System.out.println("=== Demonstração do Ciclo Completo ISO 8583 ===");
        System.out.println("Aguardando geração do código...");
    }

    /**
     * Compara campos principais entre dois DTOs
     */
    private boolean compareMainFields(PurchaseRequestDto dto1, PurchaseRequestDto dto2) {
        return dto1.getPrimaryAccountNumber().equals(dto2.getPrimaryAccountNumber()) &&
               dto1.getProcessingCode().equals(dto2.getProcessingCode()) &&
               dto1.getTransactionAmount().compareTo(dto2.getTransactionAmount()) == 0 &&
               dto1.getSystemTraceAuditNumber().equals(dto2.getSystemTraceAuditNumber()) &&
               dto1.getTerminalId().equals(dto2.getTerminalId()) &&
               dto1.getMerchantId().equals(dto2.getMerchantId()) &&
               dto1.getCurrencyCode().equals(dto2.getCurrencyCode());
    }

    /**
     * Converte bytes para representação hexadecimal
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b & 0xFF));
        }
        return hex.toString();
    }
}
