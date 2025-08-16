package com.example.iso8583.domain;

public record DecodeResult<T>(T value, int nextIndex) {
}
