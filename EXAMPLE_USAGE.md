# Exemplos Práticos de Uso

## 1. Exemplo Básico: Mensagem de Compra

### Definindo o DTO

```java
@Iso8583Message(mti = 0x200) // MTI 0200 = Financial Transaction Request
public class PurchaseRequestDto {
    
    @Iso8583Field(number = 2, type = IsoType.LLVAR, required = true)
    private String primaryAccountNumber; // PAN
    
    @Iso8583Field(number = 3, type = IsoType.NUMERIC, length = 6, required = true)
    private String processingCode; // 000000 = Purchase
    
    @Iso8583Field(number = 4, type = IsoType.AMOUNT, required = true)
    private BigDecimal transactionAmount;
    
    @Iso8583Field(number = 11, type = IsoType.NUMERIC, length = 6, required = true)
    private String systemTraceAuditNumber; // STAN
    
    @Iso8583Field(number = 41, type = IsoType.ALPHA, length = 8, required = true)
    private String terminalId;
    
    @Iso8583Field(number = 42, type = IsoType.ALPHA, length = 15, required = true)
    private String merchantId;
    
    // Getters e setters...
}
```

### Usando o Código Gerado

```java
public class PurchaseExample {
    
    public void demonstratePurchase() {
        // 1. Criar o DTO
        PurchaseRequestDto purchase = new PurchaseRequestDto();
        purchase.setPrimaryAccountNumber("4111111111111111");
        purchase.setProcessingCode("000000");
        purchase.setTransactionAmount(new BigDecimal("150.75"));
        purchase.setSystemTraceAuditNumber("123456");
        purchase.setTerminalId("TERM0001");
        purchase.setMerchantId("MERCHANT000001");
        
        // 2. Obter o encoder gerado automaticamente
        GeneratedIso8583Registry registry = new GeneratedIso8583Registry();
        IsoMessageEncoder<PurchaseRequestDto> encoder = registry.getEncoder(PurchaseRequestDto.class);
        
        // 3. Codificar para bytes ISO 8583
        byte[] isoData = encoder.encode(purchase);
        
        System.out.println("Mensagem ISO 8583 codificada:");
        System.out.println(bytesToHex(isoData));
        
        // 4. Decodificar de volta
        IsoMessageDecoder<PurchaseRequestDto> decoder = registry.getDecoder(PurchaseRequestDto.class);
        PurchaseRequestDto decoded = decoder.decode(isoData);
        
        System.out.println("Mensagem decodificada:");
        System.out.println(decoded);
    }
}
```

## 2. Exemplo Avançado: Mensagem de Resposta

### DTO de Resposta

```java
@Iso8583Message(mti = 0x210) // MTI 0210 = Financial Transaction Response
public class PurchaseResponseDto {
    
    @Iso8583Field(number = 2, type = IsoType.LLVAR, required = true)
    private String primaryAccountNumber;
    
    @Iso8583Field(number = 3, type = IsoType.NUMERIC, length = 6, required = true)
    private String processingCode;
    
    @Iso8583Field(number = 4, type = IsoType.AMOUNT, required = true)
    private BigDecimal transactionAmount;
    
    @Iso8583Field(number = 11, type = IsoType.NUMERIC, length = 6, required = true)
    private String systemTraceAuditNumber;
    
    @Iso8583Field(number = 37, type = IsoType.ALPHA, length = 12, required = true)
    private String retrievalReferenceNumber; // RRN
    
    @Iso8583Field(number = 38, type = IsoType.ALPHA, length = 6, required = false)
    private String authorizationCode; // Código de autorização
    
    @Iso8583Field(number = 39, type = IsoType.ALPHA, length = 2, required = true)
    private String responseCode; // 00 = Approved
    
    @Iso8583Field(number = 41, type = IsoType.ALPHA, length = 8, required = true)
    private String terminalId;
    
    @Iso8583Field(number = 42, type = IsoType.ALPHA, length = 15, required = true)
    private String merchantId;
    
    // Getters e setters...
}
```

### Processamento Completo

```java
public class TransactionProcessor {
    
    private final GeneratedIso8583Registry registry = new GeneratedIso8583Registry();
    
    public byte[] processTransaction(byte[] requestData) {
        try {
            // 1. Decodificar requisição
            IsoMessageDecoder<PurchaseRequestDto> requestDecoder = 
                registry.getDecoder(PurchaseRequestDto.class);
            PurchaseRequestDto request = requestDecoder.decode(requestData);
            
            System.out.println("Processando transação:");
            System.out.println("PAN: " + maskPan(request.getPrimaryAccountNumber()));
            System.out.println("Valor: " + request.getTransactionAmount());
            System.out.println("Terminal: " + request.getTerminalId());
            
            // 2. Processar transação (lógica de negócio)
            PurchaseResponseDto response = processBusinessLogic(request);
            
            // 3. Codificar resposta
            IsoMessageEncoder<PurchaseResponseDto> responseEncoder = 
                registry.getEncoder(PurchaseResponseDto.class);
            return responseEncoder.encode(response);
            
        } catch (Exception e) {
            System.err.println("Erro ao processar transação: " + e.getMessage());
            return createErrorResponse();
        }
    }
    
    private PurchaseResponseDto processBusinessLogic(PurchaseRequestDto request) {
        PurchaseResponseDto response = new PurchaseResponseDto();
        
        // Copia campos da requisição
        response.setPrimaryAccountNumber(request.getPrimaryAccountNumber());
        response.setProcessingCode(request.getProcessingCode());
        response.setTransactionAmount(request.getTransactionAmount());
        response.setSystemTraceAuditNumber(request.getSystemTraceAuditNumber());
        response.setTerminalId(request.getTerminalId());
        response.setMerchantId(request.getMerchantId());
        
        // Adiciona campos específicos da resposta
        response.setRetrievalReferenceNumber(generateRRN());
        response.setAuthorizationCode(generateAuthCode());
        response.setResponseCode("00"); // Aprovado
        
        return response;
    }
    
    private String generateRRN() {
        return String.format("%012d", System.currentTimeMillis() % 1000000000000L);
    }
    
    private String generateAuthCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
    
    private String maskPan(String pan) {
        if (pan == null || pan.length() < 8) return "****";
        return pan.substring(0, 4) + "****" + pan.substring(pan.length() - 4);
    }
    
    private byte[] createErrorResponse() {
        // Implementar resposta de erro
        return new byte[0];
    }
}
```

## 3. Exemplo com Factory Customizada

### Configurando Templates

```java
public class CustomIsoFactory {
    
    public static IsoMessageFactory createFinancialFactory() {
        return IsoMessageFactory.createFinancialFactory()
            // Adicionar campos customizados
            .addFieldTemplate(60, IsoType.LLLVAR, 0)  // Additional Data
            .addFieldTemplate(61, IsoType.LLLVAR, 0)  // Reserved for Private Use
            .addFieldTemplate(63, IsoType.LLLVAR, 0); // Reserved for Private Use
    }
    
    public void demonstrateFactoryUsage() {
        IsoMessageFactory factory = createFinancialFactory();
        
        // Criar mensagem usando factory
        IsoMessage message = factory.newMessage(0x200);
        message.setField(2, "4111111111111111", IsoType.LLVAR, 0);
        message.setField(4, new BigDecimal("100.00"), IsoType.AMOUNT, 12);
        message.setField(60, "Additional transaction data", IsoType.LLLVAR, 0);
        
        // Codificar usando factory
        byte[] data = factory.encode(message);
        
        // Decodificar usando factory
        IsoMessage decoded = factory.decode(data);
        
        System.out.println("Mensagem processada com factory customizada:");
        System.out.println(decoded);
    }
}
```

## 4. Exemplo de Validação Customizada

### DTO com Validações

```java
@Iso8583Message(mti = 0x200)
public class ValidatedPurchaseDto {
    
    @Iso8583Field(number = 2, type = IsoType.LLVAR, required = true)
    private String primaryAccountNumber;
    
    @Iso8583Field(number = 4, type = IsoType.AMOUNT, required = true)
    private BigDecimal transactionAmount;
    
    // Validação customizada
    public void validate() {
        if (primaryAccountNumber == null || !isValidPan(primaryAccountNumber)) {
            throw new IllegalArgumentException("PAN inválido");
        }
        
        if (transactionAmount == null || transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }
        
        if (transactionAmount.compareTo(new BigDecimal("10000.00")) > 0) {
            throw new IllegalArgumentException("Valor excede limite máximo");
        }
    }
    
    private boolean isValidPan(String pan) {
        // Implementar algoritmo de Luhn
        return pan.matches("\\d{13,19}") && luhnCheck(pan);
    }
    
    private boolean luhnCheck(String pan) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = pan.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(pan.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
    
    // Getters e setters...
}
```

## 5. Exemplo de Teste Unitário

```java
@Test
class PurchaseTransactionTest {
    
    private GeneratedIso8583Registry registry;
    
    @BeforeEach
    void setUp() {
        registry = new GeneratedIso8583Registry();
    }
    
    @Test
    void shouldEncodeAndDecodeSuccessfully() {
        // Given
        PurchaseRequestDto original = createSamplePurchase();
        
        // When
        IsoMessageEncoder<PurchaseRequestDto> encoder = registry.getEncoder(PurchaseRequestDto.class);
        byte[] encoded = encoder.encode(original);
        
        IsoMessageDecoder<PurchaseRequestDto> decoder = registry.getDecoder(PurchaseRequestDto.class);
        PurchaseRequestDto decoded = decoder.decode(encoded);
        
        // Then
        assertThat(decoded.getPrimaryAccountNumber()).isEqualTo(original.getPrimaryAccountNumber());
        assertThat(decoded.getTransactionAmount()).isEqualByComparingTo(original.getTransactionAmount());
        assertThat(decoded.getSystemTraceAuditNumber()).isEqualTo(original.getSystemTraceAuditNumber());
    }
    
    @Test
    void shouldValidateRequiredFields() {
        // Given
        PurchaseRequestDto incomplete = new PurchaseRequestDto();
        incomplete.setPrimaryAccountNumber("4111111111111111");
        // processingCode não foi setado (obrigatório)
        
        // When/Then
        IsoMessageEncoder<PurchaseRequestDto> encoder = registry.getEncoder(PurchaseRequestDto.class);
        assertThatThrownBy(() -> encoder.encode(incomplete))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("required");
    }
    
    private PurchaseRequestDto createSamplePurchase() {
        PurchaseRequestDto dto = new PurchaseRequestDto();
        dto.setPrimaryAccountNumber("4111111111111111");
        dto.setProcessingCode("000000");
        dto.setTransactionAmount(new BigDecimal("99.99"));
        dto.setSystemTraceAuditNumber("123456");
        dto.setTerminalId("TERM0001");
        dto.setMerchantId("MERCHANT000001");
        return dto;
    }
}
```

## 6. Exemplo de Integração com Spring Boot

### Service Layer

```java
@Service
public class Iso8583TransactionService {
    
    private final GeneratedIso8583Registry registry;
    
    public Iso8583TransactionService() {
        this.registry = new GeneratedIso8583Registry();
    }
    
    @Transactional
    public PurchaseResponseDto processTransaction(PurchaseRequestDto request) {
        // Validar entrada
        validateTransaction(request);
        
        // Processar
        PurchaseResponseDto response = executeTransaction(request);
        
        // Auditar
        auditTransaction(request, response);
        
        return response;
    }
    
    private void validateTransaction(PurchaseRequestDto request) {
        // Implementar validações de negócio
    }
    
    private PurchaseResponseDto executeTransaction(PurchaseRequestDto request) {
        // Implementar lógica de processamento
        return new PurchaseResponseDto();
    }
    
    private void auditTransaction(PurchaseRequestDto request, PurchaseResponseDto response) {
        // Implementar auditoria
    }
}
```

### REST Controller

```java
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    private final Iso8583TransactionService transactionService;
    
    public TransactionController(Iso8583TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping("/purchase")
    public Mono<ResponseEntity<PurchaseResponseDto>> processPurchase(
            @RequestBody PurchaseRequestDto request) {
        
        return Mono.fromCallable(() -> transactionService.processTransaction(request))
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
```

## Conclusão

Estes exemplos demonstram como usar a implementação própria de ISO 8583 em diferentes cenários:

1. **Uso básico** com DTOs anotados
2. **Processamento completo** de transações
3. **Customização** com factories
4. **Validações** avançadas
5. **Testes** unitários
6. **Integração** com Spring Boot

A implementação oferece flexibilidade total mantendo type safety e performance otimizada.
