package com.example.iso8583.utils;

import com.example.iso8583.enums.IsoType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class FieldFormatterTest {

    @Test
    void shouldFormatNumericField() {
        String result = FieldFormatter.formatValue("123", IsoType.NUMERIC, 6);
        
        assertThat(result).isEqualTo("000123");
    }

    @Test
    void shouldFormatAlphaField() {
        String result = FieldFormatter.formatValue("ABC", IsoType.ALPHA, 6);
        
        assertThat(result).isEqualTo("ABC   ");
    }

    @Test
    void shouldFormatLLVARField() {
        String result = FieldFormatter.formatValue("HELLO", IsoType.LLVAR, 0);
        
        assertThat(result).isEqualTo("05HELLO");
    }

    @Test
    void shouldFormatLLLVARField() {
        String result = FieldFormatter.formatValue("HELLO WORLD", IsoType.LLLVAR, 0);
        
        assertThat(result).isEqualTo("011HELLO WORLD");
    }

    @Test
    void shouldFormatAmountField() {
        BigDecimal amount = new BigDecimal("123.45");
        String result = FieldFormatter.formatValue(amount, IsoType.AMOUNT, 12);
        
        assertThat(result).isEqualTo("000000012345");
    }

    @Test
    void shouldFormatDateTimeFields() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 8, 14, 15, 30, 45);
        
        String date14 = FieldFormatter.formatValue(dateTime, IsoType.DATE14, 14);
        String date10 = FieldFormatter.formatValue(dateTime, IsoType.DATE10, 10);
        String date4 = FieldFormatter.formatValue(dateTime, IsoType.DATE4, 4);
        String time = FieldFormatter.formatValue(dateTime, IsoType.TIME, 6);
        
        assertThat(date14).isEqualTo("20240814153045");
        assertThat(date10).isEqualTo("0814153045");
        assertThat(date4).isEqualTo("0814");
        assertThat(time).isEqualTo("153045");
    }

    @Test
    void shouldThrowExceptionForOversizedNumericField() {
        assertThatThrownBy(() -> 
            FieldFormatter.formatValue("1234567", IsoType.NUMERIC, 6)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("exceeds maximum length");
    }

    @Test
    void shouldThrowExceptionForOversizedAlphaField() {
        assertThatThrownBy(() -> 
            FieldFormatter.formatValue("TOOLONG", IsoType.ALPHA, 6)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("exceeds maximum length");
    }

    @Test
    void shouldCalculateCorrectFormattedLength() {
        int numericLength = FieldFormatter.getFormattedLength("123", IsoType.NUMERIC, 6);
        int llvarLength = FieldFormatter.getFormattedLength("HELLO", IsoType.LLVAR, 0);
        int lllvarLength = FieldFormatter.getFormattedLength("HELLO WORLD", IsoType.LLLVAR, 0);
        
        assertThat(numericLength).isEqualTo(6);
        assertThat(llvarLength).isEqualTo(7); // 2 + 5
        assertThat(lllvarLength).isEqualTo(14); // 3 + 11
    }
}
