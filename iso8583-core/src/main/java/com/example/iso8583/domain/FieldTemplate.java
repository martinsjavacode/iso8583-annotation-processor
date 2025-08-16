package com.example.iso8583.domain;

import com.example.iso8583.enums.IsoType;

public record FieldTemplate(IsoType type, int length) {
}
