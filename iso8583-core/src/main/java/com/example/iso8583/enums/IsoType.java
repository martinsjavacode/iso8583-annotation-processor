package com.example.iso8583.enums;

/**
 * Enum que define os tipos de dados suportados pelo protocolo ISO 8583.
 * Cada tipo tem características específicas de codificação e validação.
 */
public enum IsoType {
    /**
     * Campo numérico com tamanho fixo (ex: "000123" para length=6)
     */
    NUMERIC(true, true, false),
    
    /**
     * Campo alfanumérico com tamanho fixo (ex: "ABC123" para length=6)
     */
    ALPHA(true, true, false),
    
    /**
     * Campo de tamanho variável com 2 dígitos de comprimento (max 99 chars)
     */
    LLVAR(false, true, true),
    
    /**
     * Campo de tamanho variável com 3 dígitos de comprimento (max 999 chars)
     */
    LLLVAR(false, true, true),
    
    /**
     * Campo de tamanho variável com 4 dígitos de comprimento (max 9999 chars)
     */
    LLLLVAR(false, true, true),
    
    /**
     * Data no formato YYYYMMDDHHMMSS (14 dígitos)
     */
    DATE14(false, true, false),
    
    /**
     * Data no formato MMDDHHMMSS (10 dígitos)
     */
    DATE10(false, true, false),
    
    /**
     * Data no formato MMDD (4 dígitos)
     */
    DATE4(false, true, false),
    
    /**
     * Data no formato YYMMDDHHMMSS (12 dígitos)
     */
    DATE12(false, true, false),
    
    /**
     * Data no formato YYMMDD (6 dígitos)
     */
    DATE6(false, true, false),
    
    /**
     * Data de expiração no formato YYMM (4 dígitos)
     */
    DATE_EXP(false, true, false),
    
    /**
     * Hora no formato HHMMSS (6 dígitos)
     */
    TIME(false, true, false),
    
    /**
     * Valor monetário (12 dígitos numéricos, sem ponto decimal)
     */
    AMOUNT(false, true, false),
    
    /**
     * Campo binário com tamanho fixo
     */
    BINARY(true, false, false),
    
    /**
     * Campo binário de tamanho variável com 2 dígitos de comprimento
     */
    LLBIN(false, false, true),
    
    /**
     * Campo binário de tamanho variável com 3 dígitos de comprimento
     */
    LLLBIN(false, false, true),
    
    /**
     * Campo binário de tamanho variável com 4 dígitos de comprimento
     */
    LLLLBIN(false, false, true),
    
    /**
     * Campo BCD binário de tamanho variável com 2 dígitos de comprimento
     */
    LLBCDBIN(false, false, true),
    
    /**
     * Campo BCD binário de tamanho variável com 3 dígitos de comprimento
     */
    LLLBCDBIN(false, false, true),
    
    /**
     * Campo BCD binário de tamanho variável com 4 dígitos de comprimento
     */
    LLLLBCDBIN(false, false, true);

    private final boolean needsLength;
    private final boolean isText;
    private final boolean isVariable;

    IsoType(boolean needsLength, boolean isText, boolean isVariable) {
        this.needsLength = needsLength;
        this.isText = isText;
        this.isVariable = isVariable;
    }

    /**
     * Indica se o tipo precisa de um comprimento definido na anotação
     */
    public boolean needsLength() {
        return needsLength;
    }

    /**
     * Indica se o tipo é textual (ASCII) ou binário
     */
    public boolean isText() {
        return isText;
    }

    /**
     * Indica se o tipo tem tamanho variável
     */
    public boolean isVariable() {
        return isVariable;
    }

    /**
     * Retorna o número de dígitos usados para indicar o comprimento em tipos variáveis
     */
    public int getLengthDigits() {
        return switch (this) {
            case LLVAR, LLBIN, LLBCDBIN -> 2;
            case LLLVAR, LLLBIN, LLLBCDBIN -> 3;
            case LLLLVAR, LLLLBIN, LLLLBCDBIN -> 4;
            default -> 0;
        };
    }

    /**
     * Retorna o comprimento fixo para tipos de data/hora
     */
    public int getFixedLength() {
        return switch (this) {
            case DATE14 -> 14;
            case DATE12 -> 12;
            case DATE10 -> 10;
            case DATE6 -> 6;
            case DATE4, DATE_EXP -> 4;
            case TIME -> 6;
            case AMOUNT -> 12;
            default -> 0;
        };
    }
}
