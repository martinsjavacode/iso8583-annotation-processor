package com.example.iso8583.annotation;

import com.example.iso8583.enums.IsoType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para marcar um campo como um campo ISO 8583.
 * Similar ao @Column do JPA, define as propriedades do campo.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE) // Processada em tempo de compilação
public @interface Iso8583Field {
    
    /**
     * Número do campo ISO 8583 (2-128)
     */
    int number();
    
    /**
     * Comprimento do campo (obrigatório para tipos de tamanho fixo)
     */
    int length() default 0;
    
    /**
     * Tipo de dados do campo
     */
    IsoType type();
    
    /**
     * Se o campo é obrigatório
     */
    boolean required() default false;
    
    /**
     * Descrição do campo (para documentação)
     */
    String description() default "";
}
