package com.example.iso8583.service;

import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.domain.IsoValue;
import com.example.iso8583.utils.BitmapUtils;
import com.example.iso8583.utils.FieldFormatter;

import java.util.Map;

/**
 * Encoder para converter mensagens IsoMessage em bytes seguindo o protocolo ISO 8583.
 * <p>
 * Formato da mensagem:
 * 1. MTI (4 primeiros digitos)
 * 2. Bitmap primário (8 bytes binários)
 * 3. Bitmap secundário (8 bytes binários, se presente)
 * 4. Campos de dados na ordem crescente
 */
public class IsoEncoder {

	/**
	 * Codifica uma mensagem ISO 8583 em array de bytes
	 */
	public byte[] encode(IsoMessage message) {
		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null");
		}

		message.validate(); // Valida a estrutura da mensagem

		StringBuilder result = new StringBuilder();

		// 1. Adiciona MTI (4 dígitos)
		result.append(message.getMti());

		// 2. Adiciona bitmap(s)
		final String bitmapHex = BitmapUtils.toIsoBitmapHex(message.getBitmap());
		result.append(bitmapHex);

		// 3. Adiciona campos de dados em ordem crescente
		Map<Integer, IsoValue<?>> fields = message.getFields();
		for (Map.Entry<Integer, IsoValue<?>> entry : fields.entrySet()) {
			int fieldNumber = entry.getKey();
			IsoValue<?> isoValue = entry.getValue();

			// Pula o campo 1 (bitmap secundário é tratado automaticamente)
			if (fieldNumber == 1) {
				continue;
			}

			String encodedField = encodeField(isoValue);
			result.append(encodedField);
		}

		return result.toString().getBytes();
	}

	/**
	 * Codifica um campo individual
	 */
	private String encodeField(IsoValue<?> isoValue) {
		return FieldFormatter.encodingValue(
			isoValue.value(),
			isoValue.type(),
			isoValue.length()
		);
	}
}
