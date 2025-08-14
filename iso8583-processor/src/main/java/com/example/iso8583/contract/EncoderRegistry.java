package com.example.iso8583.contract;

public interface EncoderRegistry {
	<T> IsoMessageEncoder<T> getEncoder(Class<T> dtoType);
}
