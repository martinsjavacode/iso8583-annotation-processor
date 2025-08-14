package com.example.iso8583.utils;

import java.util.BitSet;
import java.util.Set;

/**
 * Utilitários para manipulação de bitmap ISO 8583.
 * 
 * O bitmap é usado para indicar quais campos estão presentes na mensagem.
 * - Bitmap primário: campos 1-64
 * - Bitmap secundário: campos 65-128 (presente se o bit 1 do primário estiver setado)
 */
public final class BitmapUtils {

    private BitmapUtils() {
        // Utility class
    }

    /**
     * Cria um bitmap a partir de um conjunto de números de campos
     */
    public static BitSet createBitmap(Set<Integer> fieldNumbers) {
        BitSet bitmap = new BitSet(128);
        
        boolean hasSecondaryFields = fieldNumbers.stream().anyMatch(field -> field > 64);
        
        // Se há campos secundários, seta o bit 1 (indica presença do bitmap secundário)
        if (hasSecondaryFields) {
            bitmap.set(1);
        }
        
        // Seta os bits correspondentes aos campos presentes
        for (Integer fieldNumber : fieldNumbers) {
            if (fieldNumber >= 2 && fieldNumber <= 128) {
                bitmap.set(fieldNumber);
            }
        }
        
        return bitmap;
    }

    /**
     * Converte um BitSet para array de bytes (representação hexadecimal)
     */
    public static byte[] bitmapToBytes(BitSet bitmap) {
        // Determina se precisa do bitmap secundário
        boolean hasSecondary = bitmap.get(1);
        int bitmapSize = hasSecondary ? 16 : 8; // 16 bytes para bitmap duplo, 8 para simples
        
        byte[] bytes = new byte[bitmapSize];
        
        // Processa bitmap primário (bits 1-64)
        for (int i = 1; i <= 64; i++) {
            if (bitmap.get(i)) {
                int byteIndex = (i - 1) / 8;
                int bitIndex = 7 - ((i - 1) % 8);
                bytes[byteIndex] |= (1 << bitIndex);
            }
        }
        
        // Processa bitmap secundário se necessário (bits 65-128)
        if (hasSecondary) {
            for (int i = 65; i <= 128; i++) {
                if (bitmap.get(i)) {
                    int byteIndex = 8 + (i - 65) / 8;
                    int bitIndex = 7 - ((i - 65) % 8);
                    bytes[byteIndex] |= (1 << bitIndex);
                }
            }
        }
        
        return bytes;
    }

    /**
     * Converte array de bytes para BitSet
     */
    public static BitSet bytesToBitmap(byte[] bytes) {
        BitSet bitmap = new BitSet(128);
        
        // Processa bitmap primário (primeiros 8 bytes)
        for (int i = 0; i < Math.min(8, bytes.length); i++) {
            for (int bit = 0; bit < 8; bit++) {
                if ((bytes[i] & (1 << (7 - bit))) != 0) {
                    bitmap.set(i * 8 + bit + 1);
                }
            }
        }
        
        // Se há bitmap secundário (bytes 8-15)
        if (bytes.length > 8 && bitmap.get(1)) {
            for (int i = 8; i < Math.min(16, bytes.length); i++) {
                for (int bit = 0; bit < 8; bit++) {
                    if ((bytes[i] & (1 << (7 - bit))) != 0) {
                        bitmap.set((i - 8) * 8 + bit + 65);
                    }
                }
            }
        }
        
        return bitmap;
    }

    /**
     * Converte bitmap para representação hexadecimal
     */
    public static String bitmapToHex(BitSet bitmap) {
        byte[] bytes = bitmapToBytes(bitmap);
        StringBuilder hex = new StringBuilder();
        
        for (byte b : bytes) {
            hex.append(String.format("%02X", b & 0xFF));
        }
        
        return hex.toString();
    }

    /**
     * Converte string hexadecimal para bitmap
     */
    public static BitSet hexToBitmap(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        
        return bytesToBitmap(bytes);
    }

    /**
     * Converte bitmap para representação binária (para debug)
     */
    public static String bitmapToBinary(BitSet bitmap) {
        StringBuilder binary = new StringBuilder();
        
        // Bitmap primário
        for (int i = 1; i <= 64; i++) {
            binary.append(bitmap.get(i) ? '1' : '0');
            if (i % 8 == 0) binary.append(' '); // Separador visual
        }
        
        // Bitmap secundário se presente
        if (bitmap.get(1)) {
            binary.append('\n');
            for (int i = 65; i <= 128; i++) {
                binary.append(bitmap.get(i) ? '1' : '0');
                if ((i - 64) % 8 == 0) binary.append(' '); // Separador visual
            }
        }
        
        return binary.toString().trim();
    }
}
