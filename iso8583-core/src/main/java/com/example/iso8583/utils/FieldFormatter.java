package com.example.iso8583.utils;

import com.example.iso8583.enums.IsoType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Utilitários para formatação de campos ISO 8583 de acordo com seus tipos.
 */
public final class FieldFormatter {

    private FieldFormatter() {
        // Utility class
    }

    /**
     * Formata um valor de acordo com o tipo ISO 8583 especificado
     */
    public static String formatValue(Object value, IsoType type, int length) {
        if (value == null) {
            return "";
        }

        return switch (type) {
            case NUMERIC -> formatNumeric(value, length);
            case ALPHA -> formatAlpha(value, length);
            case LLVAR, LLLVAR, LLLLVAR -> formatVariable(value, type);
            case DATE14 -> formatDate14(value);
            case DATE12 -> formatDate12(value);
            case DATE10 -> formatDate10(value);
            case DATE6 -> formatDate6(value);
            case DATE4 -> formatDate4(value);
            case DATE_EXP -> formatDateExp(value);
            case TIME -> formatTime(value);
            case AMOUNT -> formatAmount(value);
            case BINARY -> formatBinary(value, length);
            case LLBIN, LLLBIN, LLLLBIN -> formatVariableBinary(value, type);
            default -> value.toString();
        };
    }

    /**
     * Formata campo numérico com padding de zeros à esquerda
     */
    private static String formatNumeric(Object value, int length) {
        String str = value.toString().replaceAll("[^0-9]", "");
        if (str.length() > length) {
            throw new IllegalArgumentException(
                String.format("Numeric value '%s' exceeds maximum length %d", str, length)
            );
        }
        return String.format("%0" + length + "d", Long.parseLong(str.isEmpty() ? "0" : str));
    }

    /**
     * Formata campo alfanumérico com padding de espaços à direita
     */
    private static String formatAlpha(Object value, int length) {
        String str = value.toString();
        if (str.length() > length) {
            throw new IllegalArgumentException(
                String.format("Alpha value '%s' exceeds maximum length %d", str, length)
            );
        }
        return String.format("%-" + length + "s", str);
    }

    /**
     * Formata campo de tamanho variável (LLVAR, LLLVAR, LLLLVAR)
     */
    private static String formatVariable(Object value, IsoType type) {
        String str = value.toString();
        int lengthDigits = type.getLengthDigits();
        int maxLength = (int) Math.pow(10, lengthDigits) - 1;
        
        if (str.length() > maxLength) {
            throw new IllegalArgumentException(
                String.format("Variable value '%s' exceeds maximum length %d for type %s", 
                    str, maxLength, type)
            );
        }
        
        String lengthPrefix = String.format("%0" + lengthDigits + "d", str.length());
        return lengthPrefix + str;
    }

    /**
     * Formata data no formato YYYYMMDDHHMMSS (14 dígitos)
     */
    private static String formatDate14(Object value) {
        LocalDateTime dateTime = convertToDateTime(value);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * Formata data no formato YYMMDDHHMMSS (12 dígitos)
     */
    private static String formatDate12(Object value) {
        LocalDateTime dateTime = convertToDateTime(value);
        return dateTime.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
    }

    /**
     * Formata data no formato MMDDHHMMSS (10 dígitos)
     */
    private static String formatDate10(Object value) {
        LocalDateTime dateTime = convertToDateTime(value);
        return dateTime.format(DateTimeFormatter.ofPattern("MMddHHmmss"));
    }

    /**
     * Formata data no formato YYMMDD (6 dígitos)
     */
    private static String formatDate6(Object value) {
        LocalDateTime dateTime = convertToDateTime(value);
        return dateTime.format(DateTimeFormatter.ofPattern("yyMMdd"));
    }

    /**
     * Formata data no formato MMDD (4 dígitos)
     */
    private static String formatDate4(Object value) {
        LocalDateTime dateTime = convertToDateTime(value);
        return dateTime.format(DateTimeFormatter.ofPattern("MMdd"));
    }

    /**
     * Formata data de expiração no formato YYMM (4 dígitos)
     */
    private static String formatDateExp(Object value) {
        LocalDateTime dateTime = convertToDateTime(value);
        return dateTime.format(DateTimeFormatter.ofPattern("yyMM"));
    }

    /**
     * Formata hora no formato HHMMSS (6 dígitos)
     */
    private static String formatTime(Object value) {
        LocalDateTime dateTime = convertToDateTime(value);
        return dateTime.format(DateTimeFormatter.ofPattern("HHmmss"));
    }

    /**
     * Formata valor monetário (12 dígitos, sem ponto decimal)
     */
    private static String formatAmount(Object value) {
        BigDecimal amount;
        if (value instanceof BigDecimal bd) {
            amount = bd;
        } else if (value instanceof Number num) {
            amount = BigDecimal.valueOf(num.doubleValue());
        } else {
            amount = new BigDecimal(value.toString());
        }
        
        // Converte para centavos (multiplica por 100) e remove decimais
        BigDecimal cents = amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2);
        long centValue = cents.longValue();
        
        return String.format("%012d", centValue);
    }

    /**
     * Formata campo binário
     */
    private static String formatBinary(Object value, int length) {
        byte[] bytes;
        if (value instanceof byte[] byteArray) {
            bytes = byteArray;
        } else if (value instanceof String str) {
            bytes = hexStringToBytes(str);
        } else {
            throw new IllegalArgumentException("Binary field must be byte[] or hex string");
        }
        
        if (bytes.length != length) {
            throw new IllegalArgumentException(
                String.format("Binary value length %d doesn't match expected length %d", 
                    bytes.length, length)
            );
        }
        
        return bytesToHexString(bytes);
    }

    /**
     * Formata campo binário de tamanho variável
     */
    private static String formatVariableBinary(Object value, IsoType type) {
        byte[] bytes;
        if (value instanceof byte[] byteArray) {
            bytes = byteArray;
        } else if (value instanceof String str) {
            bytes = hexStringToBytes(str);
        } else {
            throw new IllegalArgumentException("Binary field must be byte[] or hex string");
        }
        
        int lengthDigits = type.getLengthDigits();
        int maxLength = (int) Math.pow(10, lengthDigits) - 1;
        
        if (bytes.length > maxLength) {
            throw new IllegalArgumentException(
                String.format("Binary value length %d exceeds maximum %d for type %s", 
                    bytes.length, maxLength, type)
            );
        }
        
        String lengthPrefix = String.format("%0" + lengthDigits + "d", bytes.length);
        return lengthPrefix + bytesToHexString(bytes);
    }

    /**
     * Converte objeto para LocalDateTime
     */
    private static LocalDateTime convertToDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        } else if (value instanceof String str) {
            // Tenta parsear string como ISO date
            return LocalDateTime.parse(str);
        } else {
            throw new IllegalArgumentException("Date field must be LocalDateTime or ISO date string");
        }
    }

    /**
     * Converte string hexadecimal para bytes
     */
    private static byte[] hexStringToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    /**
     * Converte bytes para string hexadecimal
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * Calcula o comprimento real de um campo formatado
     */
    public static int getFormattedLength(Object value, IsoType type, int declaredLength) {
        if (value == null) {
            return 0;
        }

        return switch (type) {
            case NUMERIC, ALPHA, BINARY -> declaredLength;
            case LLVAR, LLBIN, LLBCDBIN -> 2 + getContentLength(value);
            case LLLVAR, LLLBIN, LLLBCDBIN -> 3 + getContentLength(value);
            case LLLLVAR, LLLLBIN, LLLLBCDBIN -> 4 + getContentLength(value);
            case DATE14 -> 14;
            case DATE12 -> 12;
            case DATE10 -> 10;
            case DATE6 -> 6;
            case DATE4, DATE_EXP -> 4;
            case TIME -> 6;
            case AMOUNT -> 12;
            default -> value.toString().length();
        };
    }

    private static int getContentLength(Object value) {
        if (value instanceof byte[] bytes) {
            return bytes.length;
        }
        return value.toString().length();
    }
}
