package com.example.iso8583.controller;

import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.service.Iso8583MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller REST reativo para demonstrar o uso das mensagens ISO 8583
 * processadas automaticamente pelas anotações.
 */
@RestController
@RequestMapping("/api/iso8583")
public class Iso8583Controller {

    private static final Logger logger = LoggerFactory.getLogger(Iso8583Controller.class);
    
    private final Iso8583MessageService messageService;

    public Iso8583Controller(Iso8583MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Endpoint para processar uma requisição de compra.
     * Converte o DTO em mensagem ISO 8583.
     */
    @PostMapping(value = "/purchase", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> processPurchase(@RequestBody PurchaseRequestDto request) {
        logger.info("Recebida requisição de compra via REST");
        return messageService.processPurchaseRequest(request);
    }

    /**
     * Endpoint para parsear uma mensagem ISO 8583.
     * Converte a mensagem ISO em DTO.
     */
    @PostMapping(value = "/parse", 
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PurchaseRequestDto> parseMessage(@RequestBody String isoMessage) {
        logger.info("Recebida mensagem ISO 8583 para parsing via REST");
        return messageService.parseIsoMessage(isoMessage);
    }

    /**
     * Endpoint para demonstrar o processamento completo.
     * DTO -> ISO -> DTO (round-trip)
     */
    @PostMapping(value = "/simulate", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PurchaseRequestDto> simulateProcessing(@RequestBody PurchaseRequestDto request) {
        logger.info("Simulando processamento completo via REST");
        return messageService.simulateFullProcessing(request);
    }

    /**
     * Endpoint para gerar uma requisição de exemplo.
     */
    @GetMapping(value = "/sample", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PurchaseRequestDto> getSampleRequest() {
        logger.info("Gerando requisição de exemplo via REST");
        return messageService.createSamplePurchaseRequest();
    }

    /**
     * Endpoint para obter estatísticas do processamento.
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Iso8583MessageService.ProcessingStats> getStats() {
        logger.info("Obtendo estatísticas via REST");
        return messageService.getProcessingStats();
    }

    /**
     * Endpoint de health check.
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HealthStatus> health() {
        return Mono.just(new HealthStatus("UP", "ISO 8583 Annotation Processor funcionando"));
    }

    /**
     * Record para status de saúde
     */
    public record HealthStatus(String status, String message) {}
}
