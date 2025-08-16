# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-SNAPSHOT] - 2024-08-16

### Added
- Implementação completa do protocolo ISO 8583 sem dependências externas
- Annotation processor JSR 269 para geração automática de código
- Records modernos `DecodeResult<T>` e `FieldTemplate` para melhor type safety
- Registry unificado `Iso8583Registry` para gerenciamento de encoders/decoders
- Suporte completo a todos os tipos de campo ISO 8583
- Aplicação Spring Boot 3.5.4 de exemplo com REST API
- Documentação completa com exemplos práticos
- Configuração `.editorconfig` para consistência de código

### Changed
- **BREAKING CHANGE**: Removido `Iso8583Service` em favor do registry gerado
- **BREAKING CHANGE**: Renomeado `EncoderRegistry` para `Iso8583Registry`
- Refatorada arquitetura core com separação de responsabilidades
- Melhorada geração de código com validações em tempo de compilação
- Atualizada documentação com nova arquitetura

### Removed
- **BREAKING CHANGE**: Removidos testes de integração obsoletos
- **BREAKING CHANGE**: Removido `Iso8583Utils` não utilizado
- Classes internas movidas para records públicos

### Technical Details
- Java 21 com features modernas (records, pattern matching)
- Maven 3.8+ com plugins atualizados
- Spring Boot 3.5.4 para aplicação de exemplo
- Zero dependências externas no core
- Geração de código otimizada sem reflexão em runtime

### Migration Notes
- Substitua `Iso8583Service` por `GeneratedIso8583Registry`
- Use `Iso8583Registry` ao invés de `EncoderRegistry`
- Verifique imports após refatoração de packages
- Consulte `MIGRATION_GUIDE.md` para detalhes completos

## [Unreleased]

### Planned
- Suporte a campos binários avançados
- Templates de mensagem pré-configurados
- Validações customizadas via anotações
- Métricas de performance integradas
- Plugin Maven para geração de código
- Suporte a múltiplos formatos de bitmap
