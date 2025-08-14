package com.example.iso8583.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para marcar uma classe como uma mensagem ISO 8583.
 * Similar ao @Entity do JPA, define o tipo da mensagem.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE) // Processada em tempo de compilação
public @interface Iso8583Message {
	/**
	 * MTI (Message Type Indicator) da mensagem ISO 8583
	 * O MTI eh composto por 4 digitos
	 * - 1º digito: versao da ISO (0-3)
	 * - 2º digito: classe da mensagem (0-9)
	 * - 3º digito: funcao da mensagem (0-9)
	 * - 4º digito: origem da mensagem (0-9)
	 */
	int mti();
}
