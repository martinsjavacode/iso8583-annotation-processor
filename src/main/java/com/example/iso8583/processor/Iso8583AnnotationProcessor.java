package com.example.iso8583.processor;

import com.example.iso8583.annotation.Iso8583Field;
import com.example.iso8583.annotation.Iso8583Message;
import com.solab.iso8583.IsoType;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Annotation Processor que processa as anotações @Iso8583Message e @Iso8583Field
 * em tempo de compilação, gerando automaticamente classes de parser e builder.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "com.example.iso8583.annotation.Iso8583Message",
    "com.example.iso8583.annotation.Iso8583Field"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class Iso8583AnnotationProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Iso8583AnnotationProcessor iniciado");
        
        // Processa todas as classes anotadas com @Iso8583Message
        Set<? extends Element> messageElements = roundEnv.getElementsAnnotatedWith(Iso8583Message.class);
        
        messager.printMessage(Diagnostic.Kind.NOTE, 
            "Encontradas " + messageElements.size() + " classes anotadas com @Iso8583Message");
        
        for (Element element : messageElements) {
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "Processando elemento: " + element.getSimpleName());
                
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, 
                    "@Iso8583Message só pode ser aplicada a classes", element);
                continue;
            }
            
            TypeElement typeElement = (TypeElement) element;
            try {
                generateMessageProcessor(typeElement);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, 
                    "Erro ao gerar código para " + typeElement.getSimpleName() + ": " + e.getMessage());
            }
        }
        
        return true;
    }

    /**
     * Gera uma classe de processamento para cada mensagem ISO 8583 anotada
     */
    private void generateMessageProcessor(TypeElement messageClass) throws IOException {
        Iso8583Message messageAnnotation = messageClass.getAnnotation(Iso8583Message.class);
        String packageName = processingEnv.getElementUtils().getPackageOf(messageClass).getQualifiedName().toString();
        String className = messageClass.getSimpleName() + "Processor";
        
        // Coleta todos os campos anotados
        List<FieldInfo> fields = collectAnnotatedFields(messageClass);
        
        // Gera a classe do processor
        TypeSpec processorClass = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Component")).build())
            .addType(createFieldConfigRecord()) // Record FieldConfig
            .addField(createFieldConfigsField()) // Campo para armazenar configurações dos campos
            .addField(createMessageAnnotationFields(messageAnnotation)) // Campos para armazenar valores da anotação da mensagem
            .addMethod(createConstructor(fields, messageAnnotation)) // Construtor que configura os campos
            .addMethod(createDecodeMethod(messageClass, fields)) // Método de parsing da mensagem ISO 8583
            .addMethod(createEncodeMethod(messageClass, fields)) // Método de construção da mensagem ISO 8583
            .addMethod(createGetMessageTypeMethod(messageAnnotation)) // Método que retorna o tipo da mensagem
            .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".generated", processorClass)
            .addFileComment("Gerado automaticamente pelo Iso8583AnnotationProcessor")
            .build();

        javaFile.writeTo(filer);
        
        messager.printMessage(Diagnostic.Kind.NOTE, 
            "Gerada classe " + className + " para " + messageClass.getSimpleName());
    }

    /**
     * Coleta informações de todos os campos anotados com @Iso8583Field
     */
    private List<FieldInfo> collectAnnotatedFields(TypeElement messageClass) {
        return messageClass.getEnclosedElements().stream()
            .filter(element -> element.getKind() == ElementKind.FIELD)
            .filter(element -> element.getAnnotation(Iso8583Field.class) != null)
            .map(element -> {
                VariableElement field = (VariableElement) element;
                Iso8583Field annotation = field.getAnnotation(Iso8583Field.class);
                return new FieldInfo(
                    field.getSimpleName().toString(),
                    field.asType(),
                    annotation.field(),
                    annotation.length(),
                    annotation.type(),
                    annotation.required(),
                    annotation.description()
                );
            })
            .sorted(Comparator.comparingInt(FieldInfo::fieldNumber))
            .collect(Collectors.toList());
    }

    /**
     * Cria a classe FieldConfig
     */
    private TypeSpec createFieldConfigRecord() {
        return TypeSpec.classBuilder("FieldConfig")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addField(FieldSpec.builder(String.class, "fieldName", Modifier.PUBLIC, Modifier.FINAL).build())
            .addField(FieldSpec.builder(ClassName.get("com.solab.iso8583", "IsoType"), "isoType", Modifier.PUBLIC, Modifier.FINAL).build())
            .addField(FieldSpec.builder(int.class, "length", Modifier.PUBLIC, Modifier.FINAL).build())
            .addField(FieldSpec.builder(boolean.class, "required", Modifier.PUBLIC, Modifier.FINAL).build())
            .addField(FieldSpec.builder(String.class, "description", Modifier.PUBLIC, Modifier.FINAL).build())
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "fieldName")
                .addParameter(ClassName.get("com.solab.iso8583", "IsoType"), "isoType")
                .addParameter(int.class, "length")
                .addParameter(boolean.class, "required")
                .addParameter(String.class, "description")
                .addStatement("this.fieldName = fieldName")
                .addStatement("this.isoType = isoType")
                .addStatement("this.length = length")
                .addStatement("this.required = required")
                .addStatement("this.description = description")
                .build())
            .build();
    }

    /**
     * Cria o campo para configurações dos campos
     */
    private FieldSpec createFieldConfigsField() {
        return FieldSpec.builder(
            ParameterizedTypeName.get(
                ClassName.get("java.util", "List"),
                ClassName.get("", "FieldConfig")
            ),
            "fieldConfigs",
            Modifier.PRIVATE, Modifier.FINAL
        ).build();
    }

    /**
     * Cria campos para armazenar valores da anotação da mensagem
     */
    private FieldSpec createMessageAnnotationFields(Iso8583Message messageAnnotation) {
        // Compor MTI: version=0, clazz=2, function=0, source=0 → MTI = 0200
        // Formato ISO 8583: VCCF onde V=version, CC=class, F=function (source é implícito)
        int mti = (messageAnnotation.version() * 1000) + 
                  (messageAnnotation.clazz() * 100) + 
                  (messageAnnotation.function() * 10) + 
                  messageAnnotation.source();
        
        return FieldSpec.builder(int.class, "messageType", Modifier.PRIVATE, Modifier.FINAL)
            .initializer("$L", mti)
            .build();
    }

    /**
     * Cria o construtor que configura os campos
     */
    private MethodSpec createConstructor(List<FieldInfo> fields, Iso8583Message messageAnnotation) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC);

        // Configuração dos campos baseada nas anotações
        constructor.addComment("Configuração dos campos baseada nas anotações @Iso8583Field");
        constructor.addStatement("this.fieldConfigs = new $T<>()", ClassName.get("java.util", "ArrayList"));
        
        for (FieldInfo field : fields) {
            constructor.addStatement("fieldConfigs.add(new FieldConfig($S, $T.$L, $L, $L, $S))",
                field.fieldName(),
                ClassName.get("com.solab.iso8583", "IsoType"),
                field.isoType().name(),
                field.length(),
                field.required(),
                field.description()
            );
        }
        
        return constructor.build();
    }

    /**
     * Cria o método de decode da mensagem ISO 8583 no formato binário real
     */
    private MethodSpec createDecodeMethod(TypeElement messageClass, List<FieldInfo> fields) {
        ClassName messageClassName = ClassName.get(messageClass);
        
        MethodSpec.Builder parseMethod = MethodSpec.methodBuilder("decode")
            .addModifiers(Modifier.PUBLIC)
            .returns(messageClassName)
            .addParameter(String.class, "isoMessage")
            .addException(Exception.class);

        parseMethod.addComment("Faz o decode de uma mensagem ISO 8583 no formato binário real");
        parseMethod.addStatement("$T dto = new $T()", messageClassName, messageClassName);
        
        // Validar tamanho mínimo
        parseMethod.beginControlFlow("if (isoMessage.length() < 20)");
        parseMethod.addStatement("throw new Exception(\"Mensagem ISO 8583 inválida: muito curta\")");
        parseMethod.endControlFlow();
        
        // Extrair e validar MTI
        parseMethod.addStatement("String mtiStr = isoMessage.substring(0, 4)");
        parseMethod.addStatement("int receivedMti = Integer.parseInt(mtiStr)");
        parseMethod.beginControlFlow("if (receivedMti != this.messageType)");
        parseMethod.addStatement("throw new Exception(String.format(\"MTI inválido. Esperado: %04d, Recebido: %04d\", this.messageType, receivedMti))");
        parseMethod.endControlFlow();
        
        // Extrair bitmap (16 caracteres hex = 64 bits)
        parseMethod.addStatement("String bitmapHex = isoMessage.substring(4, 20)");
        parseMethod.addStatement("boolean[] bitmap = new boolean[64]");
        
        // Decodificar bitmap
        parseMethod.beginControlFlow("for (int i = 0; i < 16; i++)");
        parseMethod.addStatement("int nibble = Integer.parseInt(bitmapHex.substring(i, i + 1), 16)");
        parseMethod.beginControlFlow("for (int j = 0; j < 4; j++)");
        parseMethod.addStatement("bitmap[i * 4 + j] = (nibble & (1 << (3 - j))) != 0");
        parseMethod.endControlFlow();
        parseMethod.endControlFlow();
        
        // Processar campos
        parseMethod.addStatement("int pos = 20");
        
        // Gera código para mapear cada campo
        for (FieldInfo field : fields) {
            String setterName = generateSetterName(field.fieldName());
            int fieldIndex = field.fieldNumber() - 1;
            
            parseMethod.beginControlFlow("if (bitmap[$L])", fieldIndex);
            
            // Determina o tipo de conversão necessária baseado no tipo do campo
            String fieldTypeName = field.fieldType().toString();
            if (fieldTypeName.contains("BigDecimal")) {
                parseMethod.addStatement("String field$LStr = isoMessage.substring(pos, pos + $L)", 
                    field.fieldNumber(), field.length());
                parseMethod.addStatement("dto.$L(new $T(Long.parseLong(field$LStr) / 100.0))", 
                    setterName, ClassName.get("java.math", "BigDecimal"), field.fieldNumber());
            } else if (fieldTypeName.contains("LocalDateTime")) {
                parseMethod.addStatement("String field$LStr = isoMessage.substring(pos, pos + $L)", 
                    field.fieldNumber(), field.length());
                parseMethod.addStatement("dto.$L($T.now())", 
                    setterName, ClassName.get("java.time", "LocalDateTime"));
            } else {
                // String
                parseMethod.addStatement("String field$LStr = isoMessage.substring(pos, pos + $L).trim()", 
                    field.fieldNumber(), field.length());
                parseMethod.addStatement("dto.$L(field$LStr)", setterName, field.fieldNumber());
            }
            
            parseMethod.addStatement("pos += $L", field.length());
            parseMethod.endControlFlow();
        }

        parseMethod.addStatement("return dto");
        return parseMethod.build();
    }

    /**
     * Cria o método de encode da mensagem ISO 8583 no formato binário real
     */
    private MethodSpec createEncodeMethod(TypeElement messageClass, List<FieldInfo> fields) {
        ClassName messageClassName = ClassName.get(messageClass);
        
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("encode")
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class)
            .addParameter(messageClassName, "dto")
            .addException(Exception.class);

        buildMethod.addComment("Constrói uma mensagem ISO 8583 no formato binário padrão");
        
        // Inicializar StringBuilder para construir a mensagem
        buildMethod.addStatement("$T message = new $T()", StringBuilder.class, StringBuilder.class);
        
        // Adicionar MTI (4 dígitos)
        buildMethod.addStatement("message.append(String.format(\"%04d\", this.messageType))");
        
        // Criar bitmap para indicar quais campos estão presentes
        buildMethod.addStatement("boolean[] bitmap = new boolean[128]");
        buildMethod.addStatement("$T<String> fieldData = new $T<>()", List.class, ArrayList.class);

        // Gera código para cada campo
        for (FieldInfo field : fields) {
            buildMethod.beginControlFlow("if (dto.$L() != null)", 
                generateGetterName(field.fieldName()));
            
            buildMethod.addStatement("bitmap[$L] = true", field.fieldNumber() - 1);
            
            // Formatar campo baseado no tipo ISO
            String fieldTypeName = field.fieldType().toString();
            if (fieldTypeName.contains("BigDecimal")) {
                // Para amounts, formatar como 12 dígitos sem ponto decimal
                buildMethod.addStatement("String field$L = String.format(\"%012d\", (long)(dto.$L().doubleValue() * 100))", 
                    field.fieldNumber(), generateGetterName(field.fieldName()));
            } else if (fieldTypeName.contains("LocalDateTime")) {
                // Para datas, usar formato MMddHHmmss
                buildMethod.addStatement("String field$L = dto.$L().format($T.ofPattern(\"MMddHHmmss\"))", 
                    field.fieldNumber(), generateGetterName(field.fieldName()), 
                    ClassName.get("java.time.format", "DateTimeFormatter"));
            } else {
                // Para strings, aplicar padding baseado no tipo ISO
                if (field.isoType().name().equals("NUMERIC")) {
                    // Campos numéricos: pad com zeros à esquerda
                    buildMethod.addStatement("String fieldValue = dto.$L()", generateGetterName(field.fieldName()));
                    buildMethod.addStatement("String field$L = (\"0\".repeat($L) + fieldValue).substring(fieldValue.length())", 
                        field.fieldNumber(), field.length());
                } else if (field.isoType().name().equals("LLVAR")) {
                    // Campos LLVAR: prefixo com 2 dígitos do comprimento + dados
                    buildMethod.addStatement("String fieldValue = dto.$L()", generateGetterName(field.fieldName()));
                    buildMethod.addStatement("String field$L = String.format(\"%02d\", fieldValue.length()) + fieldValue", 
                        field.fieldNumber());
                } else {
                    // Campos alfanuméricos: pad com espaços à direita
                    buildMethod.addStatement("String fieldValue = dto.$L()", generateGetterName(field.fieldName()));
                    buildMethod.addStatement("String field$L = fieldValue.length() > $L ? fieldValue.substring(0, $L) : fieldValue", 
                        field.fieldNumber(), field.length(), field.length());
                    buildMethod.addStatement("field$L = (field$L + \" \".repeat($L)).substring(0, $L)", 
                        field.fieldNumber(), field.fieldNumber(), field.length(), field.length());
                }
            }
            
            buildMethod.addStatement("fieldData.add(field$L)", field.fieldNumber());
            buildMethod.endControlFlow();
        }
        
        // Gerar bitmap (primeiros 64 bits em hex)
        buildMethod.addStatement("$T bitmapHex = new $T()", StringBuilder.class, StringBuilder.class);
        buildMethod.beginControlFlow("for (int i = 0; i < 64; i += 4)");
        buildMethod.addStatement("int nibble = 0");
        buildMethod.beginControlFlow("for (int j = 0; j < 4 && (i + j) < 64; j++)");
        buildMethod.beginControlFlow("if (bitmap[i + j])");
        buildMethod.addStatement("nibble |= (1 << (3 - j))");
        buildMethod.endControlFlow();
        buildMethod.endControlFlow();
        buildMethod.addStatement("bitmapHex.append(String.format(\"%X\", nibble))");
        buildMethod.endControlFlow();
        
        // Garantir que o bitmap tenha exatamente 16 caracteres (64 bits)
        buildMethod.addStatement("while (bitmapHex.length() < 16) bitmapHex.append(\"0\")");
        
        // Montar mensagem final
        buildMethod.addStatement("message.append(bitmapHex.toString())");
        buildMethod.beginControlFlow("for (String field : fieldData)");
        buildMethod.addStatement("message.append(field)");
        buildMethod.endControlFlow();
        
        buildMethod.addStatement("return message.toString()");
        return buildMethod.build();
    }

    /**
     * Cria método que retorna o tipo da mensagem (MTI) composto pelos campos da anotação
     */
    private MethodSpec createGetMessageTypeMethod(Iso8583Message messageAnnotation) {
        // Compor MTI: version=0, clazz=2, function=0, source=0 → MTI = 0200
        int mti = (messageAnnotation.version() * 1000) + 
                  (messageAnnotation.clazz() * 100) + 
                  (messageAnnotation.function() * 10) + 
                  messageAnnotation.source();
        
        return MethodSpec.methodBuilder("getMessageType")
            .addModifiers(Modifier.PUBLIC)
            .returns(int.class)
            .addStatement("return $L", mti)
            .build();
    }

    // Métodos auxiliares para geração de código
    private String generateGetterName(String fieldName) {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private String generateSetterName(String fieldName) {
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * Record para armazenar informações do campo
     */
    private record FieldInfo(
        String fieldName,
        TypeMirror fieldType,
        int fieldNumber,
        int length,
        IsoType isoType,
        boolean required,
        String description
    ) {}
}
