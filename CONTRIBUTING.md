# Contribuindo para ISO 8583 Annotation Processor

Obrigado pelo interesse em contribuir! Este documento fornece diretrizes para contribuições.

## 🚀 Como Contribuir

### 1. Fork e Clone

```bash
# Fork o repositório no GitHub
# Clone seu fork
git clone https://github.com/SEU_USUARIO/iso8583-annotation-processor.git
cd iso8583-annotation-processor
```

### 2. Configurar Ambiente

```bash
# Verificar pré-requisitos
java -version  # Java 21+
mvn -version   # Maven 3.8+

# Compilar projeto
mvn clean compile

# Executar testes
mvn test
```

### 3. Criar Branch

```bash
# Criar branch para sua feature/fix
git checkout -b feature/nova-funcionalidade
# ou
git checkout -b fix/correcao-bug
```

## 📋 Diretrizes de Desenvolvimento

### Estrutura do Projeto

```
iso8583-annotation-processor/
├── iso8583-core/           # Implementação base - SEM dependências externas
├── iso8583-processor/      # Annotation processor - JSR 269
└── iso8583-application/    # Aplicação exemplo - Spring Boot
```

### Padrões de Código

#### Java
- **Java 21**: Use features modernas (records, pattern matching, etc.)
- **Null Safety**: Prefira `Optional` para valores opcionais
- **Immutability**: Use records e classes imutáveis quando possível
- **Clean Code**: Métodos pequenos, nomes descritivos

### Testes

#### Cobertura Mínima
- **Unitários**: > 80% de cobertura
- **Integração**: Cenários principais cobertos
- **Annotation Processor**: Testes de geração de código

## 🧪 Executando Testes

### Todos os Testes
```bash
mvn test
```

### Testes Específicos
```bash
# Por módulo
mvn test -pl iso8583-core
mvn test -pl iso8583-processor
mvn test -pl iso8583-application
```

## 📝 Commits

### Conventional Commits

Use o formato [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>[optional scope]: <description>
```

#### Tipos
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Documentação
- `refactor`: Refatoração
- `test`: Testes

## 🔍 Pull Request

### Checklist
- [ ] Código segue padrões estabelecidos
- [ ] Testes adicionados/atualizados
- [ ] Documentação atualizada
- [ ] Build passa sem erros

## 📞 Contato

Para dúvidas sobre contribuições:
- Abra uma [Issue](../../issues)
- Consulte a [documentação](README.md)

---

**Obrigado por contribuir!** 🎉
