package com.example.iso8583.contract;

import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.service.IsoMessageFactory;

/**
 * Interface para decoders de mensagens ISO 8583.
 * Cada DTO anotado terá um decoder gerado automaticamente.
 */
public interface IsoMessageDecoder<T> {

	/**
	 * Converte uma IsoMessage em DTO tipado
	 *
	 * @param isoMessage Mensagem ISO 8583
	 * @return DTO tipado
	 */
	T fromIsoMessage(IsoMessage isoMessage);

	/**
	 * Converte bytes ISO 8583 em DTO tipado
	 *
	 * @param data mensagem ISO 8583
	 * @return DTO tipado
	 */
	T decode(String data);

	/**
	 * Converte bytes ISO 8583 em DTO tipado usando uma factory específica
	 *
	 * @param data    Array de bytes da mensagem ISO 8583
	 * @param factory Factory com configurações específicas
	 * @return DTO tipado
	 */
	T decode(String data, IsoMessageFactory factory);
}
