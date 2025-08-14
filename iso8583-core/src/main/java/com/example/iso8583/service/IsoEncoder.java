package com.example.iso8583.service;

import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.domain.IsoValue;
import com.example.iso8583.utils.BitmapUtils;
import com.example.iso8583.utils.FieldFormatter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Encoder para converter mensagens IsoMessage em bytes seguindo o protocolo ISO 8583.
 * 
 * Formato da mensagem:
 * 1. MTI (4 bytes ASCII)
 * 2. Bitmap primário (8 bytes binários)
 * 3. Bitmap secundário (8 bytes binários, se presente)
 * 4. Campos de dados na ordem crescente
 */
public class IsoEncoder {

    /**
     * Codifica uma mensagem ISO 8583 em array de bytes
     */
    public byte[] encode(IsoMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        message.validate(); // Valida a estrutura da mensagem

        StringBuilder result = new StringBuilder();

        // 1. Adiciona MTI (4 dígitos ASCII)
        result.append(String.format("%04d", message.getMti()));

        // 2. Adiciona bitmap(s)
        byte[] bitmapBytes = BitmapUtils.bitmapToBytes(message.getBitmap());
        result.append(bytesToHex(bitmapBytes));

        // 3. Adiciona campos de dados em ordem crescente
        Map<Integer, IsoValue<?>> fields = message.getFields();
        for (Map.Entry<Integer, IsoValue<?>> entry : fields.entrySet()) {
            int fieldNumber = entry.getKey();
            IsoValue<?> isoValue = entry.getValue();
            
            // Pula o campo 1 (bitmap secundário é tratado automaticamente)
            if (fieldNumber == 1) {
                continue;
            }

            String encodedField = encodeField(isoValue);
            result.append(encodedField);
        }

        return result.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Codifica um campo individual
     */
    private String encodeField(IsoValue<?> isoValue) {
        return FieldFormatter.formatValue(
            isoValue.value(), 
            isoValue.type(), 
            isoValue.length()
        );
    }

    /**
     * Converte bytes para string hexadecimal
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * Calcula o tamanho estimado da mensagem codificada
     */
    public int estimateEncodedSize(IsoMessage message) {
        int size = 4; // MTI

        // Bitmap
        boolean hasSecondary = message.getBitmap().get(1);
        size += hasSecondary ? 32 : 16; // 16 ou 32 chars hex (8 ou 16 bytes)

        // Campos
        for (Map.Entry<Integer, IsoValue<?>> entry : message.getFields().entrySet()) {
            if (entry.getKey() == 1) continue; // Pula bitmap secundário
            
            IsoValue<?> isoValue = entry.getValue();
            size += FieldFormatter.getFormattedLength(
                isoValue.value(), 
                isoValue.type(), 
                isoValue.length()
            );
        }

        return size;
    }

    /**
     * Versão que aceita encoding customizado
     */
    public byte[] encode(IsoMessage message, String encoding) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        message.validate();

        StringBuilder result = new StringBuilder();

        // MTI
        result.append(String.format("%04d", message.getMti()));

        // Bitmap
        byte[] bitmapBytes = BitmapUtils.bitmapToBytes(message.getBitmap());
        result.append(bytesToHex(bitmapBytes));

        // Campos
        Map<Integer, IsoValue<?>> fields = message.getFields();
        for (Map.Entry<Integer, IsoValue<?>> entry : fields.entrySet()) {
            int fieldNumber = entry.getKey();
            if (fieldNumber == 1) continue;

            IsoValue<?> isoValue = entry.getValue();
            String encodedField = encodeField(isoValue);
            result.append(encodedField);
        }

        try {
            return result.toString().getBytes(encoding);
        } catch (Exception e) {
            throw new RuntimeException("Unsupported encoding: " + encoding, e);
        }
    }

    /**
     * Codifica apenas os campos de dados (sem MTI e bitmap)
     */
    public String encodeFieldsOnly(IsoMessage message) {
        StringBuilder result = new StringBuilder();
        
        Map<Integer, IsoValue<?>> fields = message.getFields();
        for (Map.Entry<Integer, IsoValue<?>> entry : fields.entrySet()) {
            int fieldNumber = entry.getKey();
            if (fieldNumber == 1) continue;

            IsoValue<?> isoValue = entry.getValue();
            String encodedField = encodeField(isoValue);
            result.append(encodedField);
        }
        
        return result.toString();
    }
}
