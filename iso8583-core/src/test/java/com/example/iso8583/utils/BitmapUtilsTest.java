package com.example.iso8583.utils;

import org.junit.jupiter.api.Test;
import java.util.BitSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class BitmapUtilsTest {

    @Test
    void shouldCreateBitmapWithPrimaryFieldsOnly() {
        Set<Integer> fields = Set.of(2, 3, 4, 11, 12);
        
        BitSet bitmap = BitmapUtils.createBitmap(fields);
        
        assertThat(bitmap.get(1)).isFalse(); // Sem bitmap secundário
        assertThat(bitmap.get(2)).isTrue();
        assertThat(bitmap.get(3)).isTrue();
        assertThat(bitmap.get(4)).isTrue();
        assertThat(bitmap.get(11)).isTrue();
        assertThat(bitmap.get(12)).isTrue();
    }

    @Test
    void shouldCreateBitmapWithSecondaryFields() {
        Set<Integer> fields = Set.of(2, 3, 65, 128);
        
        BitSet bitmap = BitmapUtils.createBitmap(fields);
        
        assertThat(bitmap.get(1)).isTrue(); // Com bitmap secundário
        assertThat(bitmap.get(2)).isTrue();
        assertThat(bitmap.get(3)).isTrue();
        assertThat(bitmap.get(65)).isTrue();
        assertThat(bitmap.get(128)).isTrue();
    }

    @Test
    void shouldConvertBitmapToHexAndBack() {
        Set<Integer> fields = Set.of(2, 3, 4, 11, 12, 65);
        BitSet originalBitmap = BitmapUtils.createBitmap(fields);
        
        String hex = BitmapUtils.bitmapToHex(originalBitmap);
        BitSet convertedBitmap = BitmapUtils.hexToBitmap(hex);
        
        assertThat(convertedBitmap).isEqualTo(originalBitmap);
    }

    @Test
    void shouldConvertBitmapToBytesAndBack() {
        Set<Integer> fields = Set.of(2, 3, 4, 11, 12);
        BitSet originalBitmap = BitmapUtils.createBitmap(fields);
        
        byte[] bytes = BitmapUtils.bitmapToBytes(originalBitmap);
        BitSet convertedBitmap = BitmapUtils.bytesToBitmap(bytes);
        
        assertThat(convertedBitmap).isEqualTo(originalBitmap);
    }

    @Test
    void shouldGenerateCorrectBinaryRepresentation() {
        Set<Integer> fields = Set.of(2, 3, 4);
        BitSet bitmap = BitmapUtils.createBitmap(fields);
        
        String binary = BitmapUtils.bitmapToBinary(bitmap);
        
        assertThat(binary).startsWith("01110000"); // Bits 2, 3, 4 setados
    }
}
