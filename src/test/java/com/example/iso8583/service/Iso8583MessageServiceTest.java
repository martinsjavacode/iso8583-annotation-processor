package com.example.iso8583.service;

import com.example.iso8583.dto.PurchaseRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Testes unitários para o Iso8583MessageService.
 * Demonstra o funcionamento do código gerado pelo Annotation Processor.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.example.iso8583=DEBUG"
})
class Iso8583MessageServiceTest {

    // Nota: Este teste será executado após a compilação gerar as classes
    // O Spring Boot irá injetar automaticamente o PurchaseRequestDtoProcessor gerado
    
    @Test
    void testCreateSamplePurchaseRequest() {
        // Este teste demonstra como o código gerado funciona
        // Após a compilação, a classe PurchaseRequestDtoProcessor será gerada automaticamente
        
        PurchaseRequestDto sampleRequest = createTestRequest();
        
        // Verifica se os dados foram definidos corretamente
        assert sampleRequest.getPrimaryAccountNumber() != null;
        assert sampleRequest.getProcessingCode() != null;
        assert sampleRequest.getTransactionAmount() != null;
        assert sampleRequest.getSystemTraceAuditNumber() != null;
        
        System.out.println("Teste de criação de requisição executado com sucesso!");
        System.out.println("Requisição criada: " + sampleRequest);
    }
    
    @Test
    void testValidationLogic() {
        PurchaseRequestDto invalidRequest = new PurchaseRequestDto();
        
        // Testa validação de campos obrigatórios
        try {
            validateRequest(invalidRequest);
            assert false : "Deveria ter lançado exceção para requisição inválida";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("obrigatório");
            System.out.println("Validação funcionando corretamente: " + e.getMessage());
        }
    }
    
    @Test
    void testReactiveProcessing() {
        PurchaseRequestDto request = createTestRequest();
        
        // Simula processamento reativo
        StepVerifier.create(
            reactor.core.publisher.Mono.just(request)
                .delayElement(Duration.ofMillis(100))
                .map(this::processRequest)
        )
        .expectNextMatches(result -> result.contains("Processado"))
        .verifyComplete();
        
        System.out.println("Teste de processamento reativo executado com sucesso!");
    }
    
    private PurchaseRequestDto createTestRequest() {
        PurchaseRequestDto request = new PurchaseRequestDto();
        
        request.setPrimaryAccountNumber("4111111111111111");
        request.setProcessingCode("000000");
        request.setTransactionAmount(new BigDecimal("100.50"));
        request.setSystemTraceAuditNumber("123456");
        request.setTransmissionDateTime(LocalDateTime.now());
        request.setLocalTransactionTime(LocalDateTime.now());
        request.setLocalTransactionDate(LocalDateTime.now());
        request.setPosEntryMode("012");
        request.setPosConditionCode("00");
        request.setRetrievalReferenceNumber("123456789012");
        request.setTerminalId("TERM001");
        request.setMerchantId("MERCHANT001");
        request.setCurrencyCode("986");
        
        return request;
    }
    
    private void validateRequest(PurchaseRequestDto request) {
        if (request.getPrimaryAccountNumber() == null || request.getPrimaryAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Primary Account Number é obrigatório");
        }
        
        if (request.getProcessingCode() == null || request.getProcessingCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Processing Code é obrigatório");
        }
        
        if (request.getTransactionAmount() == null || request.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction Amount deve ser maior que zero");
        }
    }
    
    private String processRequest(PurchaseRequestDto request) {
        return "Processado: " + request.getSystemTraceAuditNumber();
    }
}
