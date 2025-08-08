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
	 * Versão da mensagem ISO 8583
	 */
	int version() default 0;

	/**
	 * Classe de mensagem ISO 8583.
	 */
	int clazz();
    
    /**
     * Função da mensagem ISO 8583.
     */
    int function();
    
    /**
     * Origem da mensagem ISO 8583.
     */
    int source();
}
