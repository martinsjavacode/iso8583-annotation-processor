package com.example.iso8583.controller;

import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.service.Iso8583Service;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller REST que demonstra o uso da nossa implementação ISO 8583.
 * 
 * Nota: Alguns endpoints serão habilitados após a geração do código pelo annotation processor.
 */
@RestController
@RequestMapping("/api/iso8583")
public class Iso8583Controller {

    private final Iso8583Service iso8583Service;

    public Iso8583Controller(Iso8583Service iso8583Service) {
        this.iso8583Service = iso8583Service;
    }

    /**
     * Endpoint para criar um exemplo de mensagem de compra
     */
    @GetMapping("/sample")
    public Mono<PurchaseRequestDto> getSamplePurchaseRequest() {
        return Mono.just(iso8583Service.createSamplePurchaseRequest());
    }

    /**
     * Endpoint para codificar uma mensagem de compra
     * TODO: Habilitar após geração do código
     */
    @PostMapping("/encode")
    public Mono<Map<String, Object>> encodePurchaseRequest(@RequestBody PurchaseRequestDto dto) {
        return Mono.just(Map.of(
            "success", false,
            "message", "Encoder will be available after code generation",
            "dto", dto
        ));
    }

    /**
     * Endpoint para decodificar uma mensagem ISO 8583
     * TODO: Habilitar após geração do código
     */
    @PostMapping("/decode")
    public Mono<Map<String, Object>> decodePurchaseRequest(@RequestBody Map<String, String> request) {
        return Mono.just(Map.of(
            "success", false,
            "message", "Decoder will be available after code generation"
        ));
    }

    /**
     * Endpoint para demonstrar o ciclo completo
     * TODO: Habilitar após geração do código
     */
    @PostMapping("/demo")
    public Mono<Map<String, Object>> demonstrateFullCycle(@RequestBody PurchaseRequestDto dto) {
        return Mono.just(Map.of(
            "success", false,
            "message", "Full cycle demo will be available after code generation",
            "dto", dto
        ));
    }

    /**
     * Endpoint para obter informações sobre a implementação
     */
    @GetMapping("/info")
    public Mono<Map<String, Object>> getImplementationInfo() {
        return Mono.just(Map.of(
            "implementation", "Custom ISO 8583 Implementation",
            "version", "1.0.0-SNAPSHOT",
            "status", "Code generation pending",
            "features", new String[]{
                "Annotation-based field mapping",
                "Automatic encoder/decoder generation",
                "Support for all ISO 8583 field types",
                "Primary and secondary bitmap support",
                "Type-safe field validation",
                "Modular architecture"
            },
            "supportedTypes", new String[]{
                "NUMERIC", "ALPHA", "LLVAR", "LLLVAR", "LLLLVAR",
                "DATE14", "DATE12", "DATE10", "DATE6", "DATE4", "DATE_EXP",
                "TIME", "AMOUNT", "BINARY", "LLBIN", "LLLBIN", "LLLLBIN"
            }
        ));
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public Mono<Map<String, String>> healthCheck() {
        return Mono.just(Map.of(
            "status", "UP",
            "service", "ISO 8583 Service",
            "timestamp", java.time.Instant.now().toString()
        ));
    }

    // Métodos utilitários

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b & 0xFF));
        }
        return hex.toString();
    }

    private byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private boolean compareMainFields(PurchaseRequestDto dto1, PurchaseRequestDto dto2) {
        return dto1.getPrimaryAccountNumber().equals(dto2.getPrimaryAccountNumber()) &&
               dto1.getProcessingCode().equals(dto2.getProcessingCode()) &&
               dto1.getTransactionAmount().compareTo(dto2.getTransactionAmount()) == 0 &&
               dto1.getSystemTraceAuditNumber().equals(dto2.getSystemTraceAuditNumber()) &&
               dto1.getTerminalId().equals(dto2.getTerminalId()) &&
               dto1.getMerchantId().equals(dto2.getMerchantId()) &&
               dto1.getCurrencyCode().equals(dto2.getCurrencyCode());
    }
}
