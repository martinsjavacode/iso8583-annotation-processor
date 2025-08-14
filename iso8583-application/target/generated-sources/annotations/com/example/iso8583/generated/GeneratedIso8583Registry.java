package com.example.iso8583.generated;

import com.example.iso8583.contract.EncoderRegistry;
import com.example.iso8583.contract.IsoMessageDecoder;
import com.example.iso8583.contract.IsoMessageEncoder;
import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.dto.generated.PurchaseRequestDtoDecoder;
import com.example.iso8583.dto.generated.PurchaseRequestDtoEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry gerado automaticamente.
 * Não edite este arquivo.
 */
public final class GeneratedIso8583Registry implements EncoderRegistry {
  private final Map<Class, IsoMessageEncoder> encoders = new HashMap();

  private final Map<Class, IsoMessageDecoder> decoders = new HashMap();

  public GeneratedIso8583Registry() {
    encoders.put(PurchaseRequestDto.class, new PurchaseRequestDtoEncoder());
    decoders.put(PurchaseRequestDto.class, new PurchaseRequestDtoDecoder());
  }

  @Override
  public <T> IsoMessageEncoder<T> getEncoder(Class<T> dtoType) {
    IsoMessageEncoder encoder = encoders.get(dtoType);
    if (encoder == null) {
      throw new IllegalStateException("No encoder found for " + dtoType.getName());
    }
    return (IsoMessageEncoder<T>) encoder;
  }

  public <T> IsoMessageDecoder<T> getDecoder(Class<T> dtoType) {
    IsoMessageDecoder decoder = decoders.get(dtoType);
    if (decoder == null) {
      throw new IllegalStateException("No decoder found for " + dtoType.getName());
    }
    return (IsoMessageDecoder<T>) decoder;
  }
}
