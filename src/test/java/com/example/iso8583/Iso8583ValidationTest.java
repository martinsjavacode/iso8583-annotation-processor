package com.example.iso8583;

import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.dto.generated.PurchaseRequestDtoProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de validação para encode/decode ISO 8583
 */
@DisplayName("Validação ISO 8583 - Encode/Decode")
class Iso8583ValidationTest {

    private PurchaseRequestDtoProcessor processor;
    private PurchaseRequestDto sampleDto;

    @BeforeEach
    void setUp() {
        processor = new PurchaseRequestDtoProcessor();
        
        sampleDto = new PurchaseRequestDto();
        sampleDto.setPrimaryAccountNumber("1234567890123456");
        sampleDto.setProcessingCode("000000");
        sampleDto.setTransactionAmount(new BigDecimal("100.50"));
        sampleDto.setSystemTraceAuditNumber("123456");
        sampleDto.setTerminalId("TERM001");
        sampleDto.setMerchantId("MERCHANT001");
        sampleDto.setCurrencyCode("986");
    }

    @Test
    @DisplayName("✅ Teste de Encode - DTO para ISO 8583")
    void testEncode() throws Exception {
        // Act
        String encoded = processor.encode(sampleDto);
        
        // Assert
        assertNotNull(encoded, "Mensagem encodada não deve ser nula");
        assertTrue(encoded.length() > 20, "Mensagem deve ter tamanho mínimo");
        
        // Validar MTI
        assertEquals("0200", encoded.substring(0, 4), "MTI deve ser 0200");
        
        // Validar bitmap (16 caracteres hex)
        String bitmap = encoded.substring(4, 20);
        assertEquals(16, bitmap.length(), "Bitmap deve ter 16 caracteres");
        assertTrue(bitmap.matches("[0-9A-F]+"), "Bitmap deve ser hexadecimal");
        
        System.out.println("✅ ENCODE SUCCESS: " + encoded);
        System.out.println("   Length: " + encoded.length());
        System.out.println("   MTI: " + encoded.substring(0, 4));
        System.out.println("   Bitmap: " + encoded.substring(4, 20));
    }

    @Test
    @DisplayName("✅ Teste de Decode - ISO 8583 para DTO")
    void testDecode() throws Exception {
        // Arrange
        String encoded = processor.encode(sampleDto);
        
        // Act
        PurchaseRequestDto decoded = processor.decode(encoded);
        
        // Assert
        assertNotNull(decoded, "DTO decodificado não deve ser nulo");
        
        // Validar campos principais (considerando padding ISO 8583)
        assertTrue(decoded.getPrimaryAccountNumber().contains(sampleDto.getPrimaryAccountNumber()), 
                    "PAN deve conter o valor original");
        assertEquals(0, sampleDto.getTransactionAmount().compareTo(decoded.getTransactionAmount()), 
                    "Amount deve ser preservado");
        assertEquals(sampleDto.getTerminalId(), decoded.getTerminalId(), 
                    "Terminal ID deve ser preservado");
        
        System.out.println("✅ DECODE SUCCESS:");
        System.out.println("   PAN: " + decoded.getPrimaryAccountNumber());
        System.out.println("   Amount: " + decoded.getTransactionAmount());
        System.out.println("   Terminal: '" + decoded.getTerminalId() + "'");
    }

    @Test
    @DisplayName("✅ Teste Round-trip - DTO → ISO → DTO")
    void testRoundTrip() throws Exception {
        // Act
        String encoded = processor.encode(sampleDto);
        PurchaseRequestDto decoded = processor.decode(encoded);
        String reEncoded = processor.encode(decoded);
        
        // Assert
        assertEquals(encoded.length(), reEncoded.length(), 
                    "Mensagens re-encodadas devem ter mesmo tamanho");
        
        // MTI deve ser preservado
        assertEquals(encoded.substring(0, 4), reEncoded.substring(0, 4), 
                    "MTI deve ser preservado no round-trip");
        
        System.out.println("✅ ROUND-TRIP SUCCESS:");
        System.out.println("   Original:  " + encoded);
        System.out.println("   Re-encoded:" + reEncoded);
    }

    @Test
    @DisplayName("✅ Teste de Conformidade ISO 8583")
    void testIso8583Conformity() {
        // Act
        int messageType = processor.getMessageType();
        
        // Assert
        assertEquals(200, messageType, "Message Type deve ser 200 (0x0200)");
        
        System.out.println("✅ ISO 8583 CONFORMITY:");
        System.out.println("   MTI: " + messageType + " (0x" + 
                          Integer.toHexString(messageType).toUpperCase() + ")");
    }

    @Test
    @DisplayName("✅ Teste de Performance")
    void testPerformance() throws Exception {
        // Arrange
        int iterations = 1000;
        
        // Test Encode Performance
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            processor.encode(sampleDto);
        }
        long encodeTime = System.nanoTime() - startTime;
        
        // Test Decode Performance
        String encoded = processor.encode(sampleDto);
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            processor.decode(encoded);
        }
        long decodeTime = System.nanoTime() - startTime;
        
        // Assert
        assertTrue(encodeTime < 1_000_000_000L, "Encode deve ser < 1s para " + iterations + " iterações");
        assertTrue(decodeTime < 1_000_000_000L, "Decode deve ser < 1s para " + iterations + " iterações");
        
        System.out.println("✅ PERFORMANCE (" + iterations + " iterações):");
        System.out.println("   Encode: " + (encodeTime / 1_000_000) + "ms (" + 
                          (encodeTime / iterations / 1_000) + "μs/msg)");
        System.out.println("   Decode: " + (decodeTime / 1_000_000) + "ms (" + 
                          (decodeTime / iterations / 1_000) + "μs/msg)");
    }
}
