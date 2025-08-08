# ISO 8583 Annotation Processor

Este projeto demonstra o uso da **JSR 269 (Java Annotation Processing)** para processar automaticamente anotações personalizadas no estilo JPA, gerando código para manipulação de mensagens ISO 8583 **sem uso de Reflection, Aspects ou XML**.

## 🎯 Objetivo

Criar um sistema baseado em anotações que permita definir classes DTO como mensagens ISO 8583, processando essas anotações em tempo de compilação para gerar automaticamente parsers e builders de mensagens com **configuração totalmente programática**.

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring WebFlux** (programação reativa)
- **J8583** para manipulação de mensagens ISO 8583
- **JSR 269** para processamento de anotações
- **JavaPoet** para geração de código
- **Maven** para build

## 🔄 Atualizações Recentes

### Estrutura de Anotações Atualizada

As anotações foram reformuladas para melhor flexibilidade e alinhamento com o padrão ISO 8583:

#### @Iso8583Message
```java
@Iso8583Message(version = 0, clazz = 2, function = 0, source = 0)
```
- **version**: Versão da mensagem (0-3)
- **clazz**: Classe da mensagem (0-9) 
- **function**: Função da mensagem (0-9)
- **source**: Origem da mensagem (0-9)

Estes valores são combinados para formar o Message Type Indicator (MTI). Por exemplo:
- version=0, clazz=2, function=0, source=0 → MTI = 0x0800

#### @Iso8583Field
```java
@Iso8583Field(field = 2, isoType = IsoType.NUMERIC, length = 19, required = true)
```
- **field**: Número do campo ISO (1-128)
- **isoType**: Tipo do campo usando enum IsoType da biblioteca j8583
- **length**: Comprimento do campo
- **required**: Se o campo é obrigatório

### Formato de Mensagem Simplificado

O processador agora gera mensagens no formato:
```
ISO8583:2=4111111111111111|3=000000|4=100.50|11=123456|41=TERM001|42=MERCHANT001|49=986
```

### Pipeline de Processamento Completo

O sistema suporta conversão bidirecional completa:
1. **DTO → ISO Message**: Converte objeto Java para string ISO 8583
2. **ISO Message → DTO**: Parseia string ISO 8583 de volta para objeto Java
3. **Round-trip**: Conversão completa ida e volta mantendo integridade dos dados

## 📋 O que é JSR 269?

A JSR 269 (Pluggable Annotation Processing API) permite processar anotações em tempo de compilação, oferecendo:

- **Performance**: Elimina Reflection em runtime
- **Type Safety**: Erros detectados em tempo de compilação
- **Manutenibilidade**: Código gerado automaticamente
- **Flexibilidade**: Processamento customizado de anotações

## 🚫 Sem XML - Totalmente Programático

Este projeto **NÃO utiliza arquivos XML** para configuração. Toda a configuração da MessageFactory do J8583 é feita programaticamente no código gerado pelo Annotation Processor:

```java
// ❌ Não fazemos isso:
messageFactory.setConfigPath("/j8583.xml");

// ✅ Fazemos isso:
messageFactory.setCharacterEncoding("UTF-8");
messageFactory.setAssignDate(true);
messageFactory.setFieldLength(2, 19);
messageFactory.setFieldType(2, IsoType.LLVAR);
```

## 🏗️ Arquitetura do Projeto

```
src/main/java/
├── com/example/iso8583/
│   ├── annotation/
│   │   ├── Iso8583Message.java      # Anotação para classes de mensagem
│   │   └── Iso8583Field.java        # Anotação para campos
│   ├── processor/
│   │   └── Iso8583AnnotationProcessor.java  # Processador JSR 269
│   ├── dto/
│   │   ├── PurchaseRequestDto.java  # DTO anotado de exemplo
│   │   └── generated/               # Classes geradas automaticamente
│   │       └── PurchaseRequestDtoProcessor.java
│   ├── config/
│   │   └── Iso8583Configuration.java # Configuração Spring
│   ├── service/
│   │   └── Iso8583MessageService.java  # Serviço reativo
│   ├── controller/
│   │   └── Iso8583Controller.java   # Controller REST
│   └── Iso8583Application.java      # Classe principal
```

## 🔧 Como Funciona

### 1. Definição das Anotações

```java
@Iso8583Message(version = 0, clazz = 2, function = 0, source = 0)
public class PurchaseRequestDto {
    
    @Iso8583Field(field = 2, isoType = IsoType.NUMERIC, length = 19, required = true)
    private String primaryAccountNumber;
    
    @Iso8583Field(field = 3, isoType = IsoType.NUMERIC, length = 6, required = true)
    private String processingCode;
    
    @Iso8583Field(field = 4, isoType = IsoType.AMOUNT, length = 12, required = true)
    private BigDecimal transactionAmount;
    
    // ... outros campos
}
```

### 2. Processamento em Tempo de Compilação

O `Iso8583AnnotationProcessor` detecta as anotações e gera automaticamente:

```java
@Component
public final class PurchaseRequestDtoProcessor {
    
    private final List<FieldConfig> fieldConfigs;
    
    public PurchaseRequestDtoProcessor() {
        this.fieldConfigs = Arrays.asList(
            new FieldConfig("primaryAccountNumber", IsoType.NUMERIC, 19, true, "Primary Account Number"),
            new FieldConfig("processingCode", IsoType.NUMERIC, 6, true, "Processing Code"),
            new FieldConfig("transactionAmount", IsoType.AMOUNT, 12, true, "Transaction Amount"),
            // ... configuração de todos os campos
        );
    }
    
    public PurchaseRequestDto parse(String isoMessage) throws Exception {
        // Remove prefixo "ISO8583:" e processa campos
        String cleanMessage = isoMessage.replace("ISO8583:", "");
        // Parsing automático baseado nas configurações
    }
    
    public String build(PurchaseRequestDto dto) throws Exception {
        // Constrói mensagem no formato "ISO8583:field=value|field=value"
    }
    
    public int getMessageType() {
        return 2048; // 0x0800 baseado na anotação
    }
}
```

### 3. Uso no Spring WebFlux

```java
@Service
public class Iso8583MessageService {
    
    public Mono<String> processPurchaseRequest(PurchaseRequestDto request) {
        return Mono.fromCallable(() -> purchaseProcessor.build(request))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
```

## 🚀 Como Executar

### 1. Compilação

```bash
mvn clean compile
```

Durante a compilação, o Annotation Processor será executado automaticamente e gerará as classes necessárias em `target/generated-sources/annotations/`.

**Nota**: Em alguns casos, pode ser necessário compilar manualmente o annotation processor primeiro:

```bash
# Gerar classpath
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt

# Compilar annotation processor
javac -cp $(cat cp.txt) -d target/classes src/main/java/com/example/iso8583/processor/Iso8583AnnotationProcessor.java

# Compilar DTO com annotation processor
javac -cp $(cat cp.txt):target/classes -processor com.example.iso8583.processor.Iso8583AnnotationProcessor -d target/classes src/main/java/com/example/iso8583/dto/PurchaseRequestDto.java
```

### 2. Execução

```bash
mvn spring-boot:run
```

### 3. Testes

```bash
mvn test
```

### 4. Demonstração Completa

```bash
./demo.sh
```

## 📡 Endpoints Disponíveis

- `POST /api/iso8583/purchase` - Processa requisição de compra (DTO → ISO)
- `POST /api/iso8583/parse` - Parseia mensagem ISO (ISO → DTO)
- `POST /api/iso8583/simulate` - Simulação completa (DTO → ISO → DTO)
- `GET /api/iso8583/sample` - Gera requisição de exemplo
- `GET /api/iso8583/stats` - Estatísticas do processamento
- `GET /api/iso8583/health` - Health check

## 🧪 Exemplo de Uso

### Criar uma requisição de exemplo:

```bash
curl -X GET http://localhost:8080/api/iso8583/sample
```

**Resposta:**
```json
{
  "primaryAccountNumber": "4111111111111111",
  "processingCode": "000000",
  "transactionAmount": 100.50,
  "systemTraceAuditNumber": "123456",
  "terminalId": "TERM001",
  "merchantId": "MERCHANT001",
  "currencyCode": "986"
}
```

### Processar uma compra (DTO → ISO):

```bash
curl -X POST http://localhost:8080/api/iso8583/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "primaryAccountNumber": "4111111111111111",
    "processingCode": "000000",
    "transactionAmount": 100.50,
    "systemTraceAuditNumber": "123456",
    "terminalId": "TERM001",
    "merchantId": "MERCHANT001",
    "currencyCode": "986"
  }'
```

**Resposta:**
```
ISO8583:2=4111111111111111|3=000000|4=100.50|11=123456|41=TERM001|42=MERCHANT001|49=986
```

### Parsear mensagem ISO (ISO → DTO):

```bash
curl -X POST http://localhost:8080/api/iso8583/parse \
  -H "Content-Type: text/plain" \
  -d "ISO8583:2=4111111111111111|3=000000|4=100.50|11=123456|41=TERM001|42=MERCHANT001|49=986"
```

**Resposta:**
```json
{
  "primaryAccountNumber": "4111111111111111",
  "processingCode": "000000",
  "transactionAmount": 100.50,
  "systemTraceAuditNumber": "123456",
  "terminalId": "TERM001",
  "merchantId": "MERCHANT001",
  "currencyCode": "986"
}
```

### Simulação completa (Round-trip):

```bash
curl -X POST http://localhost:8080/api/iso8583/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "primaryAccountNumber": "4111111111111111",
    "processingCode": "000000",
    "transactionAmount": 100.50,
    "systemTraceAuditNumber": "123456",
    "terminalId": "TERM001",
    "merchantId": "MERCHANT001",
    "currencyCode": "986"
  }'
```

**Resposta:**
```json
{
  "originalDto": { /* DTO original */ },
  "isoMessage": "ISO8583:2=4111111111111111|3=000000|4=100.50|11=123456|41=TERM001|42=MERCHANT001|49=986",
  "parsedDto": { /* DTO parseado de volta */ },
  "success": true
}
```

## 🎨 Vantagens da Abordagem

### ✅ Sem Reflection
- Performance superior em runtime
- Menor consumo de memória
- Detecção de erros em tempo de compilação

### ✅ Configuração Simplificada
- Formato de mensagem legível: `ISO8583:field=value|field=value`
- Parsing direto sem dependência de bibliotecas complexas
- Type safety na configuração
- Melhor integração com IDEs
- Refactoring seguro

### ✅ Type Safety
- Validação de tipos em tempo de compilação
- IDE com autocomplete completo
- Conversão automática de tipos (String ↔ BigDecimal, LocalDateTime)
- Refactoring seguro

### ✅ Manutenibilidade
- Código gerado automaticamente
- Consistência garantida
- Menos código boilerplate
- Configuração centralizada via anotações

### ✅ Flexibilidade
- Configuração via anotações
- Extensível para novos tipos de mensagem
- Integração natural com Spring
- Suporte a conversão bidirecional completa

### ✅ Testabilidade
- Pipeline completo testado (DTO → ISO → DTO)
- Validação de integridade dos dados
- Testes unitários automatizados
- Health checks integrados

## 🔍 Configuração Programática vs XML

### ❌ Abordagem Tradicional (XML):
```xml
<field num="2" type="LLVAR" length="19"/>
<field num="3" type="NUMERIC" length="6"/>
```

### ✅ Nossa Abordagem (Anotações + Código Gerado):
```java
// Anotação no DTO
@Iso8583Field(field = 2, isoType = IsoType.NUMERIC, length = 19, required = true)
private String primaryAccountNumber;

// Código gerado automaticamente
new FieldConfig("primaryAccountNumber", IsoType.NUMERIC, 19, true, "Primary Account Number")
```

### 🔄 Formato de Mensagem Simplificado

**Entrada (DTO):**
```json
{
  "primaryAccountNumber": "4111111111111111",
  "processingCode": "000000",
  "transactionAmount": 100.50
}
```

**Saída (ISO Message):**
```
ISO8583:2=4111111111111111|3=000000|4=100.50
```

**Parsing de volta:**
```java
// Automaticamente converte string para tipos apropriados
dto.setTransactionAmount(new BigDecimal("100.50"));
dto.setTransmissionDateTime(LocalDateTime.now());
```

## 🔍 Estrutura das Classes Geradas

Para cada DTO anotado, o processor gera:

```java
@Component
public final class [DTO_NAME]Processor {
    
    private final List<FieldConfig> fieldConfigs;
    
    public [DTO_NAME]Processor() {
        // Configuração baseada nas anotações @Iso8583Field
        this.fieldConfigs = Arrays.asList(
            new FieldConfig("fieldName", IsoType.NUMERIC, length, required, "description"),
            // ... todos os campos configurados
        );
    }
    
    public [DTO_TYPE] parse(String isoMessage) throws Exception {
        // 1. Remove prefixo "ISO8583:"
        String cleanMessage = isoMessage.replace("ISO8583:", "");
        
        // 2. Divide campos por "|"
        String[] fieldPairs = cleanMessage.split("\\|");
        
        // 3. Cria instância do DTO
        [DTO_TYPE] dto = new [DTO_TYPE]();
        
        // 4. Processa cada campo com conversão de tipo
        for (String pair : fieldPairs) {
            String[] parts = pair.split("=", 2);
            // Conversão automática baseada no tipo do campo
        }
        
        return dto;
    }
    
    public String build([DTO_TYPE] dto) throws Exception {
        // 1. Coleta valores dos campos do DTO
        // 2. Formata como "field=value|field=value"
        // 3. Adiciona prefixo "ISO8583:"
        return "ISO8583:" + formattedFields;
    }
    
    public int getMessageType() {
        // Calcula MTI baseado na anotação @Iso8583Message
        return (version * 1000) + (clazz * 100) + (function * 10) + source;
    }
}
```

### Record FieldConfig

```java
public record FieldConfig(
    String fieldName,
    IsoType isoType,
    int length,
    boolean required,
    String description
) {}
```

## 📚 Conceitos Demonstrados

- **JSR 269**: Processamento de anotações em tempo de compilação
- **JavaPoet**: Geração programática de código Java
- **Spring WebFlux**: Programação reativa
- **J8583**: Manipulação de mensagens ISO 8583 sem XML
- **SOLID**: Princípios de design aplicados
- **Clean Code**: Código limpo e bem estruturado

## 🎓 Aprendizados

Este projeto demonstra como:

1. Criar anotações personalizadas eficazes
2. Implementar um Annotation Processor robusto
3. Gerar código Java programaticamente
4. Configurar J8583 sem XML
5. Integrar com Spring Boot de forma transparente
6. Aplicar programação reativa com WebFlux
7. Trabalhar com mensagens ISO 8583 de forma type-safe

## 🔧 Extensões Possíveis

- Suporte a mais tipos de mensagem ISO 8583
- Validações customizadas via anotações
- Geração de documentação automática
- Métricas de performance integradas
- Suporte a diferentes versões do protocolo ISO
- Templates de configuração personalizados

## 🌟 Destaques da Implementação

- **Zero XML**: Nenhum arquivo de configuração XML
- **Zero Reflection**: Tudo processado em tempo de compilação
- **Type Safety**: Validação completa em compile-time
- **Performance**: Overhead mínimo em runtime
- **Manutenibilidade**: Código gerado automaticamente
- **Flexibilidade**: Fácil extensão e customização
- **Formato Simplificado**: Mensagens legíveis no formato `ISO8583:field=value|field=value`
- **Conversão Bidirecional**: Suporte completo DTO ↔ ISO Message
- **Integração Spring**: Componentes gerados automaticamente registrados no contexto
- **Testes Automatizados**: Pipeline completo validado com testes unitários

## 🧪 Resultados dos Testes

O projeto inclui testes automatizados que validam:

1. **Health Check**: Aplicação funcionando corretamente
2. **Stats Endpoint**: Message Type 2048 (0x0800) configurado corretamente  
3. **Sample Generation**: Geração de DTOs de exemplo
4. **DTO → ISO Conversion**: Conversão de objeto para mensagem ISO
5. **ISO → DTO Parsing**: Parsing de mensagem ISO para objeto
6. **Round-trip Validation**: Integridade dos dados em conversão completa

### Exemplo de Execução dos Testes:

```bash
mvn test
```

**Resultado:**
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

### Demonstração Completa:

```bash
./demo.sh
```

**Saída esperada:**
- Health check: `{"status":"UP","message":"ISO 8583 Annotation Processor funcionando"}`
- Stats: `{"messageType":2048,"description":"Purchase Request"}`
- Conversão DTO→ISO: `ISO8583:2=4111111111111111|3=000000|4=100.50|...`
- Parsing ISO→DTO: Objeto JSON com PAN field corretamente populado
- Simulação completa: Round-trip bem-sucedido

---

**Nota**: Este é um exemplo didático que demonstra os conceitos fundamentais da JSR 269 aplicados ao contexto de mensagens ISO 8583 com configuração totalmente programática. Em um ambiente de produção, considerações adicionais de segurança, performance e conformidade regulatória devem ser implementadas.
