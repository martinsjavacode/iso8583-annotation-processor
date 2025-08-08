# ISO 8583 Annotation Processor

<!-- Language Selector -->
<!-- Language Selector -->
<div align="center">

**🌐 Choose your language | Escolha seu idioma**

[![🇺🇸 English](https://img.shields.io/badge/🇺🇸-English-blue?style=for-the-badge)](README.md)
[![🇧🇷 PORTUGUESE](https://img.shields.io/badge/🇧🇷-Português-blue?style=for-the-badge)](README.pt.md)

</div>

---


---

An advanced demonstration project implementing **JSR 269 (Java Annotation Processing)** for automatic ISO 8583 message processing, generating compile-time code **without Reflection, XML, or external configurations**.

## 🎯 Objective

Demonstrate how to create a custom annotation-based system (JPA-style) that automatically processes DTO classes as ISO 8583 messages, generating high-performance parsers and builders with **fully programmatic configuration**.

## 🛠️ Technology Stack

- **Java 21** - Base language with modern features
- **Spring Boot 3.5.4** - Application framework
- **Spring WebFlux** - Reactive programming
- **J8583 3.0.0** - ISO 8583 manipulation library
- **JSR 269** - Annotation processing API
- **JavaPoet 1.13.0** - Programmatic Java code generation
- **Maven** - Dependency management and build

## 🚀 Quick Start

### Compilation
```bash
# Automated compilation (Recommended)
./compile-iso8583.sh

# Manual compilation
mvn clean compile
```

## 📊 Performance Metrics

Based on comprehensive test results from our test suite:
- **Encode Performance**: ~30μs per message
- **Decode Performance**: ~6μs per message
- **Test Results**: 15/15 tests passing ✅

---

**Developed with ❤️ to demonstrate the power of JSR 269 and high-performance ISO 8583**
