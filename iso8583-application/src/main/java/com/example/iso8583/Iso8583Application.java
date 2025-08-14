package com.example.iso8583;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Classe principal da aplicação Spring Boot que demonstra o uso
 * de JSR 269 (Annotation Processing) para processamento automático
 * de mensagens ISO 8583.
 * 
 * Esta aplicação utiliza:
 * - Java 21
 * - Spring Boot 3.5.4
 * - Spring WebFlux (programação reativa)
 * - J8583 para manipulação de mensagens ISO 8583
 * - JSR 269 para processamento de anotações em tempo de compilação
 */
@SpringBootApplication
@EnableWebFlux
public class Iso8583Application {

    public static void main(String[] args) {
        SpringApplication.run(Iso8583Application.class, args);
    }
}
