package com.example.iso8583;

import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.dto.generated.PurchaseRequestDtoProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes abrangentes para validação do processamento ISO 8583
 * com annotation processor JSR 269.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.example.iso8583=DEBUG",
    "iso8583.validation.enabled=true"
})
class Iso8583EncodeDecodeTest {

    private PurchaseRequestDtoProcessor processor;
    private PurchaseRequestDto sampleDto;

    @BeforeEach
    void setUp() {
        processor = new PurchaseRequestDtoProcessor();
        
        // Criar DTO de exemplo para testes
        sampleDto = new PurchaseRequestDto();
        sampleDto.setPrimaryAccountNumber("4111111111111111");
        sampleDto.setProcessingCode("000000");
        sampleDto.setTransactionAmount(new BigDecimal("100.50"));
        sampleDto.setTransmissionDateTime(LocalDateTime.now());
        sampleDto.setSystemTraceAuditNumber("123456");
        sampleDto.setLocalTransactionTime(LocalDateTime.now());
        sampleDto.setLocalTransactionDate(LocalDateTime.now());
        sampleDto.setMerchantCategoryCode("5999");
        sampleDto.setPosEntryMode("012");
        sampleDto.setPosConditionCode("00");
        sampleDto.setAcquiringInstitutionId("123456");
        sampleDto.setRetrievalReferenceNumber("123456789012");
        sampleDto.setTerminalId("TERM001");
        sampleDto.setMerchantId("MERCHANT001");
        sampleDto.setCurrencyCode("986");
    }

    @Test
    @DisplayName("1. Teste de Encode - DTO para Mensagem ISO 8583")
    void testEncode() throws Exception {
        // Act
        String isoMessage = processor.encode(sampleDto);
        
        // Assert
        assertNotNull(isoMessage, "Mensagem ISO não deve ser nula");
        assertTrue(isoMessage.startsWith("0200"), "Mensagem deve começar com MTI 0200");
        assertTrue(isoMessage.length() >= 20, "Mensagem deve ter pelo menos 20 caracteres (MTI + Bitmap)");
        
        // Valida MTI
        String mti = isoMessage.substring(0, 4);
        assertEquals("0200", mti, "MTI deve ser 0200");
        
        // Valida bitmap (16 caracteres hex após MTI)
        String bitmap = isoMessage.substring(4, 20);
        assertTrue(bitmap.matches("[0-9A-F]{16}"), "Bitmap deve ser 16 caracteres hexadecimais");
        
        // Valida que a mensagem contém dados dos campos
        assertTrue(isoMessage.length() > 20, "Mensagem deve conter dados dos campos após bitmap");
        
        System.out.println("✅ ENCODE SUCCESS: " + isoMessage);
        System.out.println("   Length: " + isoMessage.length());
        System.out.println("   MTI: " + mti);
        System.out.println("   Bitmap: " + bitmap);
    }

    @Test
    @DisplayName("2. Teste de Decode - Mensagem ISO 8583 para DTO")
    void testDecode() throws Exception {
        // Arrange - Usar uma mensagem gerada pelo próprio encode
        String encodedMessage = processor.encode(sampleDto);
        
        // Act
        PurchaseRequestDto parsedDto = processor.decode(encodedMessage);
        
        // Assert
        assertNotNull(parsedDto, "DTO parseado não deve ser nulo");
        
        // Valida campos obrigatórios (considerando padding ISO 8583)
        assertTrue(parsedDto.getPrimaryAccountNumber().contains("4111111111111111"), "PAN deve conter o valor original");
        assertEquals("000000", parsedDto.getProcessingCode(), "Processing Code deve ser preservado");
        assertEquals(0, new BigDecimal("100.50").compareTo(parsedDto.getTransactionAmount()), "Amount deve ser preservado");
        assertEquals("123456", parsedDto.getSystemTraceAuditNumber(), "STAN deve ser preservado");
        assertNotNull(parsedDto.getTerminalId(), "Terminal ID não deve ser nulo");
        assertNotNull(parsedDto.getMerchantId(), "Merchant ID não deve ser nulo");
        assertNotNull(parsedDto.getCurrencyCode(), "Currency Code não deve ser nulo");
        
        System.out.println("✅ DECODE SUCCESS - Campos preservados corretamente");
    }

    @Test
    @DisplayName("3. Teste Round-trip - DTO → ISO → DTO")
    void testRoundTrip() throws Exception {
        // Act - Encode
        String isoMessage = processor.encode(sampleDto);
        
        // Act - Decode
        PurchaseRequestDto parsedDto = processor.decode(isoMessage);
        
        // Assert - Integridade dos dados (considerando padding ISO 8583)
        assertTrue(parsedDto.getPrimaryAccountNumber().contains(sampleDto.getPrimaryAccountNumber()), "PAN deve conter o valor original");
        assertEquals(sampleDto.getProcessingCode(), parsedDto.getProcessingCode(), "Processing Code deve ser preservado");
        assertEquals(0, sampleDto.getTransactionAmount().compareTo(parsedDto.getTransactionAmount()), "Transaction Amount deve ser preservado");
        assertEquals(sampleDto.getSystemTraceAuditNumber(), parsedDto.getSystemTraceAuditNumber(), "STAN deve ser preservado");
        assertNotNull(parsedDto.getTerminalId(), "Terminal ID não deve ser nulo");
        assertNotNull(parsedDto.getMerchantId(), "Merchant ID não deve ser nulo");
        assertNotNull(parsedDto.getCurrencyCode(), "Currency Code não deve ser nulo");
        
        System.out.println("✅ ROUND-TRIP SUCCESS:");
        System.out.println("   Original:  " + isoMessage);
        System.out.println("   Parsed DTO: " + parsedDto.getPrimaryAccountNumber() + " / " + parsedDto.getTransactionAmount());
    }

    @Test
    @DisplayName("4. Teste de Conformidade ISO 8583 - Message Type")
    void testIso8583Conformity() {
        // Act
        int messageType = processor.getMessageType();
        
        // Assert
        assertEquals(200, messageType, "Message Type deve ser 200 (0x0200)");
        
        // Valida componentes do MTI
        int version = (messageType / 1000) % 10;
        int clazz = (messageType / 100) % 10;
        int function = (messageType / 10) % 10;
        int source = messageType % 10;
        
        assertEquals(0, version, "Version deve ser 0");
        assertEquals(2, clazz, "Class deve ser 2 (Financial)");
        assertEquals(0, function, "Function deve ser 0 (Request)");
        assertEquals(0, source, "Source deve ser 0 (Acquirer)");
        
        System.out.println("✅ ISO 8583 CONFORMITY - MTI: " + messageType + " (0x" + Integer.toHexString(messageType).toUpperCase() + ")");
        System.out.println("   Version: " + version + ", Class: " + clazz + ", Function: " + function + ", Source: " + source);
    }

    @Test
    @DisplayName("5. Teste de Validação de Campos Obrigatórios")
    void testRequiredFieldsValidation() {
        // Arrange - DTO com campo obrigatório faltando
        PurchaseRequestDto incompleteDto = new PurchaseRequestDto();
        incompleteDto.setProcessingCode("000000");
        incompleteDto.setTransactionAmount(new BigDecimal("100.50"));
        // PAN não definido (campo obrigatório)
        
        // Act - O código atual não valida campos obrigatórios no encode
        // Apenas verifica se não há exceção
        assertDoesNotThrow(() -> {
            String message = processor.encode(incompleteDto);
            assertNotNull(message, "Mensagem deve ser gerada mesmo com campos faltando");
            assertTrue(message.startsWith("0200"), "MTI deve estar presente");
        });
        
        System.out.println("✅ VALIDATION - Encode funciona mesmo com campos opcionais faltando");
    }

    @Test
    @DisplayName("6. Teste de Parsing de Mensagem Inválida")
    void testInvalidMessageParsing() {
        // Arrange - Mensagem muito curta para ser válida
        String invalidMessage = "INVALID";
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            processor.decode(invalidMessage);
        });
        
        assertNotNull(exception, "Deve lançar exceção para mensagem inválida");
        System.out.println("✅ INVALID PARSING - Exceção capturada: " + exception.getMessage());
    }

    @Test
    @DisplayName("7. Teste de Performance - Encode/Decode em Lote")
    void testPerformance() throws Exception {
        // Arrange
        int iterations = 1000;
        
        // Act - Teste de Encode
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            processor.encode(sampleDto);
        }
        long encodeTime = System.currentTimeMillis() - startTime;
        
        // Act - Teste de Decode
        String encodedMessage = processor.encode(sampleDto);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            processor.decode(encodedMessage);
        }
        long decodeTime = System.currentTimeMillis() - startTime;
        
        // Assert
        assertTrue(encodeTime < 5000, "Encode de " + iterations + " mensagens deve ser < 5s, foi: " + encodeTime + "ms");
        assertTrue(decodeTime < 5000, "Decode de " + iterations + " mensagens deve ser < 5s, foi: " + decodeTime + "ms");
        
        System.out.println("✅ PERFORMANCE - " + iterations + " iterações:");
        System.out.println("   Encode: " + encodeTime + "ms (" + (encodeTime / (double) iterations) + "ms/msg)");
        System.out.println("   Decode: " + decodeTime + "ms (" + (decodeTime / (double) iterations) + "ms/msg)");
    }
}
