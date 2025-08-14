package com.example.iso8583.service;

import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.domain.IsoValue;
import com.example.iso8583.enums.IsoType;
import com.example.iso8583.utils.BitmapUtils;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Map;

/**
 * Decoder para converter bytes em mensagens IsoMessage seguindo o protocolo ISO 8583.
 * 
 * Este decoder básico extrai MTI e bitmap, mas para decodificar campos específicos
 * é necessário conhecer a configuração de cada campo (tipo, tamanho, etc.).
 */
public class IsoDecoder {

    /**
     * Decodifica uma mensagem ISO 8583 básica (MTI + bitmap)
     */
    public IsoMessage decode(byte[] data) {
        if (data == null || data.length < 20) { // Mínimo: 4 (MTI) + 16 (bitmap hex)
            throw new IllegalArgumentException("Invalid message data");
        }

        String messageStr = new String(data, StandardCharsets.US_ASCII);
        return decode(messageStr);
    }

    /**
     * Decodifica uma mensagem ISO 8583 a partir de string
     */
    public IsoMessage decode(String messageStr) {
        if (messageStr == null || messageStr.length() < 20) {
            throw new IllegalArgumentException("Invalid message string");
        }

        IsoMessage message = new IsoMessage();
        int currentIndex = 0;

        // 1. Extrai MTI (4 caracteres)
        String mtiStr = messageStr.substring(currentIndex, currentIndex + 4);
        try {
            int mti = Integer.parseInt(mtiStr);
            message.setMti(mti);
            currentIndex += 4;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid MTI: " + mtiStr, e);
        }

        // 2. Extrai bitmap primário (16 caracteres hex = 8 bytes)
        if (messageStr.length() < currentIndex + 16) {
            throw new IllegalArgumentException("Message too short for primary bitmap");
        }

        String primaryBitmapHex = messageStr.substring(currentIndex, currentIndex + 16);
        BitSet bitmap = BitmapUtils.hexToBitmap(primaryBitmapHex);
        currentIndex += 16;

        // 3. Verifica se há bitmap secundário
        if (bitmap.get(1)) {
            if (messageStr.length() < currentIndex + 16) {
                throw new IllegalArgumentException("Message too short for secondary bitmap");
            }
            
            String secondaryBitmapHex = messageStr.substring(currentIndex, currentIndex + 16);
            BitSet secondaryBitmap = BitmapUtils.hexToBitmap(secondaryBitmapHex);
            
            // Combina bitmaps (campos 65-128)
            for (int i = 65; i <= 128; i++) {
                if (secondaryBitmap.get(i - 64)) {
                    bitmap.set(i);
                }
            }
            currentIndex += 16;
        }

        message.setBitmap(bitmap);

        // Retorna mensagem com MTI e bitmap decodificados
        // Para decodificar campos específicos, use decodeWithTemplate()
        return message;
    }

    /**
     * Decodifica uma mensagem usando um template de configuração de campos
     */
    public IsoMessage decodeWithTemplate(byte[] data, Map<Integer, FieldTemplate> fieldTemplates) {
        String messageStr = new String(data, StandardCharsets.US_ASCII);
        return decodeWithTemplate(messageStr, fieldTemplates);
    }

    /**
     * Decodifica uma mensagem usando um template de configuração de campos
     */
    public IsoMessage decodeWithTemplate(String messageStr, Map<Integer, FieldTemplate> fieldTemplates) {
        // Primeiro decodifica MTI e bitmap
        IsoMessage message = decode(messageStr);
        
        // Calcula posição inicial dos campos de dados
        int currentIndex = 4 + 16; // MTI + bitmap primário
        if (message.getBitmap().get(1)) {
            currentIndex += 16; // bitmap secundário
        }

        // Decodifica cada campo presente no bitmap
        BitSet bitmap = message.getBitmap();
        for (int fieldNumber = 2; fieldNumber <= 128; fieldNumber++) {
            if (!bitmap.get(fieldNumber)) {
                continue; // Campo não presente
            }

            FieldTemplate template = fieldTemplates.get(fieldNumber);
            if (template == null) {
                throw new IllegalStateException(
                    "No template found for field " + fieldNumber + " but field is present in bitmap"
                );
            }

            try {
                DecodeResult result = decodeField(messageStr, currentIndex, template);
                
                IsoValue<?> isoValue = new IsoValue<>(
                    template.type(), 
                    result.value(), 
                    template.length()
                );
                
                message.setField(fieldNumber, isoValue);
                currentIndex = result.nextIndex();
                
            } catch (Exception e) {
                throw new RuntimeException(
                    "Error decoding field " + fieldNumber + " at position " + currentIndex, e
                );
            }
        }

        return message;
    }

    /**
     * Decodifica um campo individual
     */
    private DecodeResult decodeField(String messageStr, int startIndex, FieldTemplate template) {
        IsoType type = template.type();
        int declaredLength = template.length();

        return switch (type) {
            case NUMERIC, ALPHA, BINARY -> decodeFixedField(messageStr, startIndex, declaredLength);
            case LLVAR, LLBIN, LLBCDBIN -> decodeVariableField(messageStr, startIndex, 2);
            case LLLVAR, LLLBIN, LLLBCDBIN -> decodeVariableField(messageStr, startIndex, 3);
            case LLLLVAR, LLLLBIN, LLLLBCDBIN -> decodeVariableField(messageStr, startIndex, 4);
            case DATE14 -> decodeFixedField(messageStr, startIndex, 14);
            case DATE12 -> decodeFixedField(messageStr, startIndex, 12);
            case DATE10 -> decodeFixedField(messageStr, startIndex, 10);
            case DATE6 -> decodeFixedField(messageStr, startIndex, 6);
            case DATE4, DATE_EXP -> decodeFixedField(messageStr, startIndex, 4);
            case TIME -> decodeFixedField(messageStr, startIndex, 6);
            case AMOUNT -> decodeFixedField(messageStr, startIndex, 12);
            default -> throw new UnsupportedOperationException("Unsupported field type: " + type);
        };
    }

    /**
     * Decodifica campo de tamanho fixo
     */
    private DecodeResult decodeFixedField(String messageStr, int startIndex, int length) {
        if (messageStr.length() < startIndex + length) {
            throw new IllegalArgumentException(
                String.format("Message too short for fixed field of length %d at position %d", 
                    length, startIndex)
            );
        }

        String value = messageStr.substring(startIndex, startIndex + length);
        return new DecodeResult(value, startIndex + length);
    }

    /**
     * Decodifica campo de tamanho variável
     */
    private DecodeResult decodeVariableField(String messageStr, int startIndex, int lengthDigits) {
        if (messageStr.length() < startIndex + lengthDigits) {
            throw new IllegalArgumentException(
                String.format("Message too short for variable field length indicator (%d digits) at position %d", 
                    lengthDigits, startIndex)
            );
        }

        // Extrai o comprimento
        String lengthStr = messageStr.substring(startIndex, startIndex + lengthDigits);
        int contentLength;
        try {
            contentLength = Integer.parseInt(lengthStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid length indicator: " + lengthStr, e);
        }

        int contentStart = startIndex + lengthDigits;
        if (messageStr.length() < contentStart + contentLength) {
            throw new IllegalArgumentException(
                String.format("Message too short for variable field content (length %d) at position %d", 
                    contentLength, contentStart)
            );
        }

        String value = messageStr.substring(contentStart, contentStart + contentLength);
        return new DecodeResult(value, contentStart + contentLength);
    }

    /**
     * Valida se uma string representa uma mensagem ISO 8583 válida
     */
    public boolean isValidMessage(String messageStr) {
        try {
            decode(messageStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrai apenas o MTI de uma mensagem
     */
    public int extractMti(byte[] data) {
        if (data == null || data.length < 4) {
            throw new IllegalArgumentException("Data too short for MTI");
        }

        String mtiStr = new String(data, 0, 4, StandardCharsets.US_ASCII);
        try {
            return Integer.parseInt(mtiStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid MTI: " + mtiStr, e);
        }
    }

    /**
     * Record para template de campo
     */
    public record FieldTemplate(IsoType type, int length) {}

    /**
     * Record para resultado de decodificação
     */
    private record DecodeResult(String value, int nextIndex) {}
}
