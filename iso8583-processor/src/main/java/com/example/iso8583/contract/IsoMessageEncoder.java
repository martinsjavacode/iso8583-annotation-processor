package com.example.iso8583.contract;

import com.example.iso8583.domain.IsoMessage;
import com.example.iso8583.service.IsoMessageFactory;

import java.util.BitSet;

/**
 * Interface para encoders de mensagens ISO 8583.
 * Cada DTO anotado terá um encoder gerado automaticamente.
 */
public interface IsoMessageEncoder<T> {

	/**
	 * Converte o DTO anotado em uma IsoMessage
	 *
	 * @param dto Entry Object
	 * @return IsoMessage configurada
	 */
	IsoMessage toIsoMessage(T dto);

	/**
	 * Cria o bitmap da ISO 8583
	 *
	 * @return Bitset bitmap ISO 8583
	 */
	BitSet isoBitSetGenerator();

	/**
	 * Converte o DTO anotado em bytes ISO 8583
	 *
	 * @param dto Entry Object
	 * @return Array de bytes da mensagem ISO 8583
	 */
	byte[] encode(T dto);

	/**
	 * Converte o DTO anotado em bytes ISO 8583 usando uma factory específica
	 *
	 * @param dto     Entry Object
	 * @param factory Factory com configurações específicas
	 * @return Array de bytes da mensagem ISO 8583
	 */
	byte[] encode(T dto, IsoMessageFactory factory);
}
