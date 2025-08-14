package com.example.iso8583.domain;

import com.example.iso8583.enums.IsoType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class IsoMessageTest {

    @Test
    void shouldCreateMessageWithMTI() {
        IsoMessage message = new IsoMessage(0x200);
        
        assertThat(message.getMti()).isEqualTo(0x200);
        assertThat(message.getFields()).isEmpty();
    }

    @Test
    void shouldAddAndRetrieveFields() {
        IsoMessage message = new IsoMessage(0x200);
        IsoValue<String> field2 = new IsoValue<>(IsoType.LLVAR, "4111111111111111", 0);
        
        message.setField(2, field2);
        
        assertThat(message.hasField(2)).isTrue();
        assertThat(message.getField(2)).isEqualTo(field2);
        assertThat(message.getBitmap().get(2)).isTrue();
    }

    @Test
    void shouldRemoveFields() {
        IsoMessage message = new IsoMessage(0x200);
        IsoValue<String> field2 = new IsoValue<>(IsoType.LLVAR, "4111111111111111", 0);
        
        message.setField(2, field2);
        message.removeField(2);
        
        assertThat(message.hasField(2)).isFalse();
        assertThat(message.getField(2)).isNull();
        assertThat(message.getBitmap().get(2)).isFalse();
    }

    @Test
    void shouldSetSecondaryBitmapWhenNeeded() {
        IsoMessage message = new IsoMessage(0x200);
        IsoValue<String> field65 = new IsoValue<>(IsoType.ALPHA, "TEST", 4);
        
        message.setField(65, field65);
        
        assertThat(message.getBitmap().get(1)).isTrue(); // Bitmap secundário
        assertThat(message.getBitmap().get(65)).isTrue();
    }

    @Test
    void shouldGetTypedFieldValue() {
        IsoMessage message = new IsoMessage(0x200);
        IsoValue<String> field2 = new IsoValue<>(IsoType.LLVAR, "4111111111111111", 0);
        
        message.setField(2, field2);
        
        String value = message.getFieldValue(2, String.class);
        assertThat(value).isEqualTo("4111111111111111");
    }

    @Test
    void shouldThrowExceptionForWrongFieldType() {
        IsoMessage message = new IsoMessage(0x200);
        IsoValue<String> field2 = new IsoValue<>(IsoType.LLVAR, "4111111111111111", 0);
        
        message.setField(2, field2);
        
        assertThatThrownBy(() -> message.getFieldValue(2, Integer.class))
            .isInstanceOf(ClassCastException.class);
    }

    @Test
    void shouldValidateMessage() {
        IsoMessage message = new IsoMessage(0x200);
        IsoValue<String> field2 = new IsoValue<>(IsoType.LLVAR, "4111111111111111", 0);
        
        message.setField(2, field2);
        
        assertThatCode(() -> message.validate()).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForInvalidMTI() {
        IsoMessage message = new IsoMessage(-1);
        
        assertThatThrownBy(() -> message.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Invalid MTI");
    }

    @Test
    void shouldCopyMessage() {
        IsoMessage original = new IsoMessage(0x200);
        IsoValue<String> field2 = new IsoValue<>(IsoType.LLVAR, "4111111111111111", 0);
        original.setField(2, field2);
        
        IsoMessage copy = original.copy();
        
        assertThat(copy.getMti()).isEqualTo(original.getMti());
        assertThat(copy.hasField(2)).isTrue();
        assertThat(copy.getField(2)).isEqualTo(original.getField(2));
        assertThat(copy).isNotSameAs(original);
    }

    @Test
    void shouldProvideMessageStats() {
        IsoMessage message = new IsoMessage(0x200);
        message.setField(2, new IsoValue<>(IsoType.LLVAR, "4111111111111111", 0));
        message.setField(3, new IsoValue<>(IsoType.NUMERIC, "000000", 6));
        message.setField(65, new IsoValue<>(IsoType.ALPHA, "TEST", 4));
        
        IsoMessage.MessageStats stats = message.getStats();
        
        assertThat(stats.totalFields()).isEqualTo(3);
        assertThat(stats.primaryFields()).isEqualTo(2);
        assertThat(stats.secondaryFields()).isEqualTo(1);
        assertThat(stats.hasSecondaryBitmap()).isTrue();
    }
}
