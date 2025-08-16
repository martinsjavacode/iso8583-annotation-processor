package com.example.iso8583.contract;

public interface Iso8583Registry {
	<T> IsoMessageEncoder<T> getEncoder(Class<T> dtoType);

	<T> IsoMessageDecoder<T> getDecoder(Class<T> dtoType);
}
