package com.example.iso8583.api;

public interface EncoderRegistry {
	<T> IsoMessageEncoder<T> getEncoder(Class<T> dtoType);
}
