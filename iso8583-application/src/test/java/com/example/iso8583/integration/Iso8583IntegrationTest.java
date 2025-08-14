package com.example.iso8583.integration;

import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.service.Iso8583Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class Iso8583IntegrationTest {

    @Autowired
    private Iso8583Service iso8583Service;

    @Test
    void shouldCreateSampleDto() {
        PurchaseRequestDto dto = iso8583Service.createSamplePurchaseRequest();
        
        assertThat(dto).isNotNull();
        assertThat(dto.getPrimaryAccountNumber()).isNotBlank();
        assertThat(dto.getProcessingCode()).isNotBlank();
        assertThat(dto.getTransactionAmount()).isPositive();
        assertThat(dto.getSystemTraceAuditNumber()).isNotBlank();
        assertThat(dto.getTerminalId()).isNotBlank();
        assertThat(dto.getMerchantId()).isNotBlank();
        assertThat(dto.getCurrencyCode()).isNotBlank();
    }

    @Test
    void shouldThrowExceptionForEncodingBeforeCodeGeneration() {
        PurchaseRequestDto dto = createTestDto();
        
        assertThatThrownBy(() -> iso8583Service.encodePurchaseRequest(dto))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Encoder will be available after code generation");
    }

    @Test
    void shouldThrowExceptionForDecodingBeforeCodeGeneration() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        
        assertThatThrownBy(() -> iso8583Service.decodePurchaseRequest(data))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Decoder will be available after code generation");
    }

    @Test
    void shouldCreateValidDtoStructure() {
        PurchaseRequestDto dto = createTestDto();
        
        // Verifica estrutura básica
        assertThat(dto.getPrimaryAccountNumber()).isEqualTo("4111111111111111");
        assertThat(dto.getProcessingCode()).isEqualTo("000000");
        assertThat(dto.getTransactionAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(dto.getSystemTraceAuditNumber()).isEqualTo("123456");
        assertThat(dto.getTerminalId()).isEqualTo("TERM0001");
        assertThat(dto.getMerchantId()).isEqualTo("MERCHANT000001");
        assertThat(dto.getCurrencyCode()).isEqualTo("986");
        
        // Verifica campos opcionais
        assertThat(dto.getMerchantCategoryCode()).isEqualTo("5411");
        assertThat(dto.getAcquiringInstitutionId()).isEqualTo("12345");
    }

    @Test
    void shouldHandleToStringWithMaskedPan() {
        PurchaseRequestDto dto = createTestDto();
        
        String toString = dto.toString();
        
        assertThat(toString).contains("4111****1111"); // PAN mascarado
        assertThat(toString).doesNotContain("4111111111111111"); // PAN completo não deve aparecer
    }

    private PurchaseRequestDto createTestDto() {
        PurchaseRequestDto dto = new PurchaseRequestDto();
        dto.setPrimaryAccountNumber("4111111111111111");
        dto.setProcessingCode("000000");
        dto.setTransactionAmount(new BigDecimal("100.50"));
        dto.setTransmissionDateTime(LocalDateTime.of(2024, 8, 14, 15, 30, 45));
        dto.setSystemTraceAuditNumber("123456");
        dto.setLocalTransactionTime(LocalDateTime.of(2024, 8, 14, 15, 30, 45));
        dto.setLocalTransactionDate(LocalDateTime.of(2024, 8, 14, 15, 30, 45));
        dto.setPosEntryMode("012");
        dto.setPosConditionCode("00");
        dto.setRetrievalReferenceNumber("123456789012");
        dto.setTerminalId("TERM0001");
        dto.setMerchantId("MERCHANT000001");
        dto.setCurrencyCode("986");
        dto.setMerchantCategoryCode("5411");
        dto.setAcquiringInstitutionId("12345");
        return dto;
    }
}
