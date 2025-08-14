package com.example.iso8583.domain;

import com.example.iso8583.enums.IsoType;
import com.example.iso8583.utils.BitmapUtils;

import java.util.*;

/**
 * Representa uma mensagem ISO 8583 completa com MTI, bitmap e campos de dados.
 */
public class IsoMessage {
    private int mti;
    private BitSet bitmap;
    private final Map<Integer, IsoValue<?>> fields = new TreeMap<>();

    public IsoMessage() {
        this.bitmap = new BitSet(128);
    }

    public IsoMessage(int mti) {
        this();
        this.mti = mti;
    }

    /**
     * Define o MTI (Message Type Indicator) da mensagem
     */
    public void setMti(int mti) {
        this.mti = mti;
    }

    /**
     * Retorna o MTI da mensagem
     */
    public int getMti() {
        return mti;
    }

    /**
     * Define um campo da mensagem
     */
    public void setField(int fieldNumber, IsoValue<?> value) {
        if (fieldNumber < 2 || fieldNumber > 128) {
            throw new IllegalArgumentException("Field number must be between 2 and 128");
        }
        
        if (value != null) {
            fields.put(fieldNumber, value);
            bitmap.set(fieldNumber);
        } else {
            fields.remove(fieldNumber);
            bitmap.clear(fieldNumber);
        }
        
        updateBitmap();
    }

    /**
     * Define um campo da mensagem com tipo e comprimento
     */
    public void setField(int fieldNumber, Object value, IsoType type, int length) {
        if (value == null) {
            removeField(fieldNumber);
            return;
        }
        
        IsoValue<?> isoValue = new IsoValue<>(type, value, length);
        setField(fieldNumber, isoValue);
    }

    /**
     * Remove um campo da mensagem
     */
    public void removeField(int fieldNumber) {
        fields.remove(fieldNumber);
        bitmap.clear(fieldNumber);
        updateBitmap();
    }

    /**
     * Retorna o valor de um campo
     */
    public IsoValue<?> getField(int fieldNumber) {
        return fields.get(fieldNumber);
    }

    /**
     * Retorna o valor tipado de um campo
     */
    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(int fieldNumber, Class<T> type) {
        IsoValue<?> field = getField(fieldNumber);
        if (field == null) {
            return null;
        }
        
        Object value = field.value();
        if (type.isInstance(value)) {
            return (T) value;
        }
        
        throw new ClassCastException(
            String.format("Field %d value is %s, cannot cast to %s", 
                fieldNumber, value.getClass().getSimpleName(), type.getSimpleName())
        );
    }

    /**
     * Verifica se um campo está presente na mensagem
     */
    public boolean hasField(int fieldNumber) {
        return fields.containsKey(fieldNumber);
    }

    /**
     * Retorna todos os campos da mensagem
     */
    public Map<Integer, IsoValue<?>> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    /**
     * Retorna os números dos campos presentes
     */
    public Set<Integer> getFieldNumbers() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    /**
     * Retorna o bitmap da mensagem
     */
    public BitSet getBitmap() {
        return (BitSet) bitmap.clone();
    }

    /**
     * Define o bitmap manualmente (usado principalmente no parsing)
     */
    public void setBitmap(BitSet bitmap) {
        this.bitmap = (BitSet) bitmap.clone();
    }

    /**
     * Atualiza o bitmap baseado nos campos presentes
     */
    private void updateBitmap() {
        // Limpa o bitmap atual (exceto bit 1 se necessário)
        boolean hadSecondary = bitmap.get(1);
        bitmap.clear();
        
        // Seta os bits para os campos presentes
        Set<Integer> fieldNumbers = fields.keySet();
        boolean hasSecondaryFields = fieldNumbers.stream().anyMatch(field -> field > 64);
        
        if (hasSecondaryFields) {
            bitmap.set(1); // Indica presença do bitmap secundário
        }
        
        for (Integer fieldNumber : fieldNumbers) {
            bitmap.set(fieldNumber);
        }
    }

    /**
     * Retorna o bitmap como string hexadecimal
     */
    public String getBitmapHex() {
        return BitmapUtils.bitmapToHex(bitmap);
    }

    /**
     * Retorna o bitmap como string binária (para debug)
     */
    public String getBitmapBinary() {
        return BitmapUtils.bitmapToBinary(bitmap);
    }

    /**
     * Retorna uma representação textual da mensagem para debug
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IsoMessage{\n");
        sb.append("  MTI: ").append(String.format("%04d", mti)).append("\n");
        sb.append("  Bitmap: ").append(getBitmapHex()).append("\n");
        sb.append("  Fields:\n");
        
        for (Map.Entry<Integer, IsoValue<?>> entry : fields.entrySet()) {
            sb.append("    DE").append(String.format("%03d", entry.getKey()))
              .append(": ").append(entry.getValue()).append("\n");
        }
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Cria uma cópia da mensagem
     */
    public IsoMessage copy() {
        IsoMessage copy = new IsoMessage(this.mti);
        copy.bitmap = (BitSet) this.bitmap.clone();
        copy.fields.putAll(this.fields);
        return copy;
    }

    /**
     * Valida a estrutura básica da mensagem
     */
    public void validate() {
        if (mti < 0 || mti > 9999) {
            throw new IllegalStateException("Invalid MTI: " + mti);
        }
        
        // Verifica consistência do bitmap
        for (Integer fieldNumber : fields.keySet()) {
            if (!bitmap.get(fieldNumber)) {
                throw new IllegalStateException(
                    "Field " + fieldNumber + " is present but not marked in bitmap"
                );
            }
        }
        
        // Verifica se bitmap secundário está correto
        boolean hasSecondaryFields = fields.keySet().stream().anyMatch(field -> field > 64);
        if (hasSecondaryFields && !bitmap.get(1)) {
            throw new IllegalStateException("Secondary fields present but bit 1 not set");
        }
    }

    /**
     * Retorna estatísticas da mensagem
     */
    public MessageStats getStats() {
        int primaryFields = (int) fields.keySet().stream().filter(f -> f <= 64).count();
        int secondaryFields = (int) fields.keySet().stream().filter(f -> f > 64).count();
        
        return new MessageStats(
            fields.size(),
            primaryFields,
            secondaryFields,
            bitmap.get(1)
        );
    }

    /**
     * Record para estatísticas da mensagem
     */
    public record MessageStats(
        int totalFields,
        int primaryFields,
        int secondaryFields,
        boolean hasSecondaryBitmap
    ) {}
}
