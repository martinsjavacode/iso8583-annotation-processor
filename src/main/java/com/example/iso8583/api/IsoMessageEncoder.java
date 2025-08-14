package com.example.iso8583.api;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;

public interface IsoMessageEncoder<T> {

	/**
	 * Convert the dto annotated in a IsoMessage
	 * @param dto Entry Object
	 * @param factory optional; When configured, create message with templates/packager
	 * @return IsoMessage
	 */
	IsoMessage encode(T dto, MessageFactory<IsoMessage> factory);
}
