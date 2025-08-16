package com.example.iso8583.controller;

import com.example.iso8583.contract.Iso8583Registry;
import com.example.iso8583.dto.PurchaseRequestDto;
import com.example.iso8583.generated.GeneratedIso8583Registry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("iso8583")
public class Iso8583Controller {

	private final Iso8583Registry iso8583Registry;

	public Iso8583Controller() {
		this.iso8583Registry = new GeneratedIso8583Registry();
	}

	@PostMapping(path = "encoder", produces = "application/json")
	public byte[] encoder(@RequestBody PurchaseRequestDto dto) {
		final var encoder = iso8583Registry.getEncoder(PurchaseRequestDto.class);
		return encoder.encode(dto);
	}

	@PostMapping(path = "decoder", produces = "application/json")
	public PurchaseRequestDto decoder(@RequestBody String message) {
		final var decoder = iso8583Registry.getDecoder(PurchaseRequestDto.class);
		return decoder.decode(message);
	}
}
