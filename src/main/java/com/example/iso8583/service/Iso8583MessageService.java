package com.example.iso8583.service;

import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.dto.generated.PurchaseRequestDtoProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Serviço reativo para processamento de mensagens ISO 8583.
 * Utiliza as classes geradas automaticamente pelo Annotation Processor.
 */
@Service
public class Iso8583MessageService {

    private static final Logger logger = LoggerFactory.getLogger(Iso8583MessageService.class);
    
    private final PurchaseRequestDtoProcessor purchaseProcessor;

    public Iso8583MessageService(PurchaseRequestDtoProcessor purchaseProcessor) {
        this.purchaseProcessor = purchaseProcessor;
    }

    /**
     * Processa uma requisição de compra de forma reativa.
     * Converte o DTO em mensagem ISO 8583 e vice-versa.
     */
    public Mono<String> processPurchaseRequest(PurchaseRequestDto request) {
        return Mono.fromCallable(() -> {
            logger.info("Processando requisição de compra: {}", request);
            
            // Valida os dados obrigatórios
            validatePurchaseRequest(request);
            
            // Gera a mensagem ISO 8583 usando o processor gerado
            String isoMessage = purchaseProcessor.encode(request);
            
            logger.info("Mensagem ISO 8583 gerada com sucesso. Tipo: {}", 
                purchaseProcessor.getMessageType());
            
            return isoMessage;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> logger.error("Erro ao processar requisição de compra", error));
    }

    /**
     * Parseia uma mensagem ISO 8583 em um DTO de forma reativa.
     */
    public Mono<PurchaseRequestDto> parseIsoMessage(String isoMessage) {
        return Mono.fromCallable(() -> {
            logger.info("Parseando mensagem ISO 8583");
            
            // Parseia usando o processor gerado
            PurchaseRequestDto dto = purchaseProcessor.decode(isoMessage);
            
            logger.info("Mensagem parseada com sucesso: {}", dto);
            
            return dto;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(error -> logger.error("Erro ao parsear mensagem ISO 8583", error));
    }

    /**
     * Simula o processamento completo: DTO -> ISO -> DTO
     */
    public Mono<PurchaseRequestDto> simulateFullProcessing(PurchaseRequestDto originalRequest) {
        return processPurchaseRequest(originalRequest)
            .flatMap(this::parseIsoMessage)
            .doOnNext(parsedRequest -> {
                logger.info("Processamento completo finalizado");
                logger.info("Original: {}", originalRequest);
                logger.info("Parseado: {}", parsedRequest);
            });
    }

    /**
     * Cria uma requisição de compra de exemplo para testes
     */
    public Mono<PurchaseRequestDto> createSamplePurchaseRequest() {
        return Mono.fromCallable(() -> {
            PurchaseRequestDto request = new PurchaseRequestDto();
            
            // Dados obrigatórios
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
            request.setCurrencyCode("986"); // BRL
            
            // Dados opcionais
            request.setMerchantCategoryCode("5411");
            request.setAcquiringInstitutionId("12345678901");
            
            logger.info("Requisição de exemplo criada: {}", request);
            return request;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Valida os campos obrigatórios da requisição
     */
    private void validatePurchaseRequest(PurchaseRequestDto request) {
        if (request.getPrimaryAccountNumber() == null || request.getPrimaryAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Primary Account Number é obrigatório");
        }
        
        if (request.getProcessingCode() == null || request.getProcessingCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Processing Code é obrigatório");
        }
        
        if (request.getTransactionAmount() == null || request.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction Amount deve ser maior que zero");
        }
        
        if (request.getSystemTraceAuditNumber() == null || request.getSystemTraceAuditNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("System Trace Audit Number é obrigatório");
        }
        
        // Validações adicionais podem ser adicionadas aqui
        logger.debug("Validação da requisição concluída com sucesso");
    }

    /**
     * Retorna estatísticas do processamento
     */
    public Mono<ProcessingStats> getProcessingStats() {
        return Mono.fromCallable(() -> {
            return new ProcessingStats(
                purchaseProcessor.getMessageType(),
                "Purchase Request",
                "Ativo"
            );
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Record para estatísticas de processamento
     */
    public record ProcessingStats(
        int messageType,
        String messageName,
        String status
    ) {}
}
