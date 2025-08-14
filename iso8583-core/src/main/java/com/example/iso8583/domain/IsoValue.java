package com.example.iso8583.domain;

import com.example.iso8583.enums.IsoType;

public record IsoValue<T>(IsoType type, T value, int length) {
	@Override
	public String toString() {
		return value.toString();
	}
}

