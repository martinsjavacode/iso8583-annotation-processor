package com.example.iso8583.service;

import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.domain.IsoValue;
import com.example.iso8583.enums.IsoType;
import com.example.iso8583.service.IsoDecoder.FieldTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory para criar e configurar mensagens ISO 8583.
 * Permite definir templates de campos e criar mensagens pré-configuradas.
 */
public class IsoMessageFactory {
    
    private final Map<Integer, FieldTemplate> fieldTemplates = new HashMap<>();
    private final IsoEncoder encoder = new IsoEncoder();
    private final IsoDecoder decoder = new IsoDecoder();

    /**
     * Cria uma nova mensagem com MTI especificado
     */
    public IsoMessage newMessage(int mti) {
        return new IsoMessage(mti);
    }

    /**
     * Adiciona um template de campo à factory
     */
    public IsoMessageFactory addFieldTemplate(int fieldNumber, IsoType type, int length) {
        fieldTemplates.put(fieldNumber, new FieldTemplate(type, length));
        return this;
    }

    /**
     * Remove um template de campo
     */
    public IsoMessageFactory removeFieldTemplate(int fieldNumber) {
        fieldTemplates.remove(fieldNumber);
        return this;
    }

    /**
     * Retorna uma cópia dos templates configurados
     */
    public Map<Integer, FieldTemplate> getFieldTemplates() {
        return new HashMap<>(fieldTemplates);
    }

    /**
     * Cria uma mensagem com campos pré-definidos baseados nos templates
     */
    public IsoMessage newMessageWithDefaults(int mti) {
        IsoMessage message = new IsoMessage(mti);
        
        // Adiciona campos com valores padrão baseados nos templates
        for (Map.Entry<Integer, FieldTemplate> entry : fieldTemplates.entrySet()) {
            int fieldNumber = entry.getKey();
            FieldTemplate template = entry.getValue();
            
            Object defaultValue = getDefaultValue(template.type());
            if (defaultValue != null) {
                IsoValue<?> isoValue = new IsoValue<>(
                    template.type(), 
                    defaultValue, 
                    template.length()
                );
                message.setField(fieldNumber, isoValue);
            }
        }
        
        return message;
    }

    /**
     * Codifica uma mensagem usando esta factory
     */
    public byte[] encode(IsoMessage message) {
        return encoder.encode(message);
    }

    /**
     * Decodifica uma mensagem usando os templates desta factory
     */
    public IsoMessage decode(byte[] data) {
        if (fieldTemplates.isEmpty()) {
            // Se não há templates, faz decodificação básica (apenas MTI e bitmap)
            return decoder.decode(data);
        } else {
            // Usa templates para decodificação completa
            return decoder.decodeWithTemplate(data, fieldTemplates);
        }
    }

    /**
     * Decodifica uma mensagem a partir de string
     */
    public IsoMessage decode(String messageStr) {
        if (fieldTemplates.isEmpty()) {
            return decoder.decode(messageStr);
        } else {
            return decoder.decodeWithTemplate(messageStr, fieldTemplates);
        }
    }

    /**
     * Valida se uma mensagem está conforme os templates configurados
     */
    public boolean validateMessage(IsoMessage message) {
        try {
            for (Map.Entry<Integer, IsoValue<?>> entry : message.getFields().entrySet()) {
                int fieldNumber = entry.getKey();
                IsoValue<?> isoValue = entry.getValue();
                
                FieldTemplate template = fieldTemplates.get(fieldNumber);
                if (template != null) {
                    // Verifica se o tipo está correto
                    if (!template.type().equals(isoValue.type())) {
                        return false;
                    }
                    
                    // Verifica comprimento para tipos que precisam
                    if (template.type().needsLength() && template.length() != isoValue.length()) {
                        return false;
                    }
                }
            }
            
            message.validate(); // Validação estrutural básica
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cria uma factory pré-configurada para mensagens financeiras comuns
     */
    public static IsoMessageFactory createFinancialFactory() {
        return new IsoMessageFactory()
            .addFieldTemplate(2, IsoType.LLVAR, 0)      // PAN
            .addFieldTemplate(3, IsoType.NUMERIC, 6)    // Processing Code
            .addFieldTemplate(4, IsoType.AMOUNT, 12)    // Transaction Amount
            .addFieldTemplate(7, IsoType.DATE10, 10)    // Transmission Date/Time
            .addFieldTemplate(11, IsoType.NUMERIC, 6)   // STAN
            .addFieldTemplate(12, IsoType.TIME, 6)      // Local Transaction Time
            .addFieldTemplate(13, IsoType.DATE4, 4)     // Local Transaction Date
            .addFieldTemplate(18, IsoType.NUMERIC, 4)   // Merchant Category Code
            .addFieldTemplate(22, IsoType.NUMERIC, 3)   // POS Entry Mode
            .addFieldTemplate(25, IsoType.NUMERIC, 2)   // POS Condition Code
            .addFieldTemplate(32, IsoType.LLVAR, 0)     // Acquiring Institution ID
            .addFieldTemplate(37, IsoType.ALPHA, 12)    // Retrieval Reference Number
            .addFieldTemplate(41, IsoType.ALPHA, 8)     // Terminal ID
            .addFieldTemplate(42, IsoType.ALPHA, 15)    // Merchant ID
            .addFieldTemplate(49, IsoType.NUMERIC, 3);  // Currency Code
    }

    /**
     * Cria uma factory pré-configurada para mensagens de rede
     */
    public static IsoMessageFactory createNetworkFactory() {
        return new IsoMessageFactory()
            .addFieldTemplate(7, IsoType.DATE10, 10)    // Transmission Date/Time
            .addFieldTemplate(11, IsoType.NUMERIC, 6)   // STAN
            .addFieldTemplate(24, IsoType.NUMERIC, 3)   // Network International ID
            .addFieldTemplate(70, IsoType.NUMERIC, 3);  // Network Management Code
    }

    /**
     * Retorna valor padrão para um tipo de campo
     */
    private Object getDefaultValue(IsoType type) {
        return switch (type) {
            case NUMERIC -> "0";
            case ALPHA -> "";
            case LLVAR, LLLVAR, LLLLVAR -> "";
            case AMOUNT -> "000000000000";
            case DATE14 -> "00000000000000";
            case DATE12 -> "000000000000";
            case DATE10 -> "0000000000";
            case DATE6 -> "000000";
            case DATE4, DATE_EXP -> "0000";
            case TIME -> "000000";
            case BINARY, LLBIN, LLLBIN, LLLLBIN -> new byte[0];
            default -> null;
        };
    }

    /**
     * Cria uma cópia desta factory
     */
    public IsoMessageFactory copy() {
        IsoMessageFactory copy = new IsoMessageFactory();
        copy.fieldTemplates.putAll(this.fieldTemplates);
        return copy;
    }

    /**
     * Limpa todos os templates configurados
     */
    public IsoMessageFactory clearTemplates() {
        fieldTemplates.clear();
        return this;
    }

    /**
     * Retorna informações sobre a factory
     */
    public FactoryInfo getInfo() {
        int primaryFields = (int) fieldTemplates.keySet().stream().filter(f -> f <= 64).count();
        int secondaryFields = (int) fieldTemplates.keySet().stream().filter(f -> f > 64).count();
        
        return new FactoryInfo(
            fieldTemplates.size(),
            primaryFields,
            secondaryFields
        );
    }

    /**
     * Record para informações da factory
     */
    public record FactoryInfo(
        int totalTemplates,
        int primaryFieldTemplates,
        int secondaryFieldTemplates
    ) {}
}
