# ISO 8583 Annotation Processor

<!-- Language Selector -->
<!-- Language Selector -->
<div align="center">

**🌐 Choose your language | Escolha seu idioma**

[![🇺🇸 Inglês](https://img.shields.io/badge/🇺🇸-English-blue?style=for-the-badge)](README.md)
[![🇧🇷 Português](https://img.shields.io/badge/🇧🇷-Português-blue?style=for-the-badge)](README.pt.md)

</div>

---


---

Um projeto demonstrativo avançado que implementa **JSR 269 (Java Annotation Processing)** para processamento automático de mensagens ISO 8583, gerando código em tempo de compilação **sem uso de Reflection, XML ou configurações externas**.

## 🎯 Objetivo

Demonstrar como criar um sistema baseado em anotações personalizadas (estilo JPA) que processa automaticamente classes DTO como mensagens ISO 8583, gerando parsers e builders de alta performance com **configuração totalmente programática**.

## 🛠️ Stack Tecnológica

- **Java 21** - Linguagem base com recursos modernos
- **Spring Boot 3.5.4** - Framework de aplicação
- **Spring WebFlux** - Programação reativa
- **J8583 3.0.0** - Biblioteca para manipulação ISO 8583
- **JSR 269** - API de processamento de anotações
- **JavaPoet 1.13.0** - Geração programática de código Java
- **Maven** - Gerenciamento de dependências e build

## 🚀 Início Rápido

### Compilação
```bash
# Compilação automatizada (Recomendado)
./compile-iso8583.sh

# Compilação manual
mvn clean compile
```

## 📊 Métricas de Performance

Baseado nos resultados de testes abrangentes da nossa suíte de testes:
- **Performance de Encode**: ~30μs por mensagem
- **Performance de Decode**: ~6μs por mensagem
- **Resultados dos Testes**: 15/15 testes aprovados ✅

---

**Desenvolvido com ❤️ para demonstrar o poder da JSR 269 e processamento de mensagens ISO 8583**
