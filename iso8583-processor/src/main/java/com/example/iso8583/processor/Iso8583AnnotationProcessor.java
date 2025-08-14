package com.example.iso8583.processor;

import com.example.iso8583.annotation.Iso8583Field;
import com.example.iso8583.annotation.Iso8583Message;
import com.example.iso8583.processor.meta.FieldMeta;
import com.example.iso8583.processor.meta.MessageMeta;
import com.google.auto.service.AutoService;
import com.palantir.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.tools.Diagnostic.Kind;

/**
 * Processador de anotações que gera encoders e decoders para mensagens ISO 8583.
 * 
 * Para cada classe anotada com @Iso8583Message, gera:
 * 1. Um encoder que converte DTO -> IsoMessage/bytes
 * 2. Um decoder que converte IsoMessage/bytes -> DTO
 * 3. Um registry que permite localizar encoders/decoders por tipo
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "com.example.iso8583.annotation.Iso8583Message", 
    "com.example.iso8583.annotation.Iso8583Field"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class Iso8583AnnotationProcessor extends AbstractProcessor {
    
    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private Messager messager;
    private final List<MessageMeta> collectedMessages = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();

        this.messager.printMessage(Kind.NOTE, "ISO 8583 Annotation Processor started...");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            if (!collectedMessages.isEmpty()) {
                generateRegistry();
            }
            return true;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Iso8583Message.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Kind.ERROR, 
                    "Annotation @Iso8583Message can only be applied to a class", element);
                continue;
            }

            TypeElement dtoType = (TypeElement) element;
            processMessageClass(dtoType);
        }

        return true;
    }

    private void processMessageClass(TypeElement dtoType) {
        Iso8583Message iso8583Message = dtoType.getAnnotation(Iso8583Message.class);
        int mti = iso8583Message.mti();

        List<FieldMeta> fields = new ArrayList<>();
        for (Element enclosed : dtoType.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement varElement = (VariableElement) enclosed;
                Iso8583Field iso8583Field = varElement.getAnnotation(Iso8583Field.class);
                
                if (iso8583Field != null) {
                    String propName = varElement.getSimpleName().toString();
                    int length = iso8583Field.length();
                    
                    // Para tipos que têm comprimento fixo, usa o comprimento do tipo se não especificado
                    if (length == 0 && iso8583Field.type().getFixedLength() > 0) {
                        length = iso8583Field.type().getFixedLength();
                    }
                    
                    fields.add(new FieldMeta(
                        varElement,
                        iso8583Field.number(),
                        iso8583Field.type(),
                        length,
                        iso8583Field.required(),
                        propName
                    ));
                }
            }
        }

        MessageMeta messageMeta = new MessageMeta(
            dtoType,
            mti,
            fields,
            elementUtils.getPackageOf(dtoType).getQualifiedName().toString(),
            dtoType.getSimpleName().toString()
        );

        validate(messageMeta);
        generateEncoder(messageMeta);
        generateDecoder(messageMeta);
        collectedMessages.add(messageMeta);
    }

    private void validate(MessageMeta meta) {
        Set<Integer> seen = new HashSet<>();
        
        for (FieldMeta f : meta.fields()) {
            // Valida número do campo
            if (f.number() < 2 || f.number() > 128) {
                messager.printMessage(Kind.ERROR,
                    "Field number must be between 2-128, got: " + f.number(),
                    f.element());
            }

            // Valida duplicidade
            if (!seen.add(f.number())) {
                messager.printMessage(Kind.ERROR,
                    "Duplicate field number: " + f.number(),
                    f.element());
            }

            // Valida comprimento para tipos que precisam
            if (f.type().needsLength() && f.length() <= 0) {
                messager.printMessage(Kind.ERROR,
                    "Type " + f.type().name() + " requires length > 0",
                    f.element());
            }
        }
    }

    private void generateEncoder(MessageMeta meta) {
        ClassName dto = ClassName.get(meta.packageName(), meta.simpleName());
        String generatedPkg = meta.packageName() + ".generated";
        String encoderName = meta.simpleName() + "Encoder";

        // Imports necessários
        ClassName isoMessage = ClassName.get("com.example.iso8583.domain", "IsoMessage");
        ClassName isoValue = ClassName.get("com.example.iso8583.domain", "IsoValue");
        ClassName isoType = ClassName.get("com.example.iso8583.enums", "IsoType");
        ClassName isoEncoder = ClassName.get("com.example.iso8583.service", "IsoEncoder");
        ClassName isoMessageFactory = ClassName.get("com.example.iso8583.service", "IsoMessageFactory");
        ClassName isoMessageEncoder = ClassName.get("com.example.iso8583.contract", "IsoMessageEncoder");

        // Método de validação
        MethodSpec.Builder validateMethod = MethodSpec.methodBuilder("validateRequirements")
            .addModifiers(Modifier.PRIVATE)
            .returns(dto)
            .addParameter(dto, "dto")
            .addJavadoc("Valida campos obrigatórios");

        for (FieldMeta f : meta.fields()) {
            if (f.required()) {
                validateMethod.beginControlFlow("if (dto.get$L() == null)", capitalize(f.propertyName()))
                    .addStatement("throw new IllegalArgumentException(\"Field $L (DE $L) is required\")", 
                        f.propertyName(), f.number())
                    .endControlFlow();
            }
        }
        validateMethod.addStatement("return dto");

        // Método toIsoMessage
        MethodSpec.Builder toIsoMessageMethod = MethodSpec.methodBuilder("toIsoMessage")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(isoMessage)
            .addParameter(dto, "dto")
            .addJavadoc("Converte DTO em IsoMessage")
            .addStatement("validateRequirements(dto)")
            .addStatement("$T message = new $T($L)", isoMessage, isoMessage, meta.mti());

        // Adiciona cada campo à mensagem
        for (FieldMeta f : meta.fields()) {
            String getter = "get" + capitalize(f.propertyName()) + "()";
            toIsoMessageMethod.addStatement(
                "message.setField($L, dto.$L, $T.$L, $L)",
                f.number(), getter, isoType, f.type().name(), f.length()
            );
        }

        toIsoMessageMethod.addStatement("return message");

        // Método encode simples
        MethodSpec encodeSimple = MethodSpec.methodBuilder("encode")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(ArrayTypeName.of(TypeName.BYTE))
            .addParameter(dto, "dto")
            .addJavadoc("Codifica DTO em bytes ISO 8583")
            .addStatement("$T message = toIsoMessage(dto)", isoMessage)
            .addStatement("$T encoder = new $T()", isoEncoder, isoEncoder)
            .addStatement("return encoder.encode(message)")
            .build();

        // Método encode com factory
        MethodSpec encodeWithFactory = MethodSpec.methodBuilder("encode")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(ArrayTypeName.of(TypeName.BYTE))
            .addParameter(dto, "dto")
            .addParameter(isoMessageFactory, "factory")
            .addJavadoc("Codifica DTO em bytes ISO 8583 usando factory específica")
            .addStatement("$T message = toIsoMessage(dto)", isoMessage)
            .addStatement("return factory.encode(message)")
            .build();

        // Classe do encoder
        TypeSpec encoder = TypeSpec.classBuilder(encoderName)
            .addJavadoc("Encoder gerado automaticamente para $L.\nNão edite este arquivo.", meta.simpleName())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ParameterizedTypeName.get(isoMessageEncoder, dto))
            .addMethod(validateMethod.build())
            .addMethod(toIsoMessageMethod.build())
            .addMethod(encodeSimple)
            .addMethod(encodeWithFactory)
            .build();

        writeJavaFile(generatedPkg, encoder);
    }

    private void generateDecoder(MessageMeta meta) {
        ClassName dto = ClassName.get(meta.packageName(), meta.simpleName());
        String generatedPkg = meta.packageName() + ".generated";
        String decoderName = meta.simpleName() + "Decoder";

        // Imports necessários
        ClassName isoMessage = ClassName.get("com.example.iso8583.domain", "IsoMessage");
        ClassName isoValue = ClassName.get("com.example.iso8583.domain", "IsoValue");
        ClassName isoDecoder = ClassName.get("com.example.iso8583.service", "IsoDecoder");
        ClassName isoMessageFactory = ClassName.get("com.example.iso8583.service", "IsoMessageFactory");
        ClassName isoMessageDecoder = ClassName.get("com.example.iso8583.contract", "IsoMessageDecoder");
        ClassName fieldTemplate = ClassName.get("com.example.iso8583.service", "IsoDecoder", "FieldTemplate");
        ClassName isoType = ClassName.get("com.example.iso8583.enums", "IsoType");
        ClassName map = ClassName.get("java.util", "Map");
        ClassName hashMap = ClassName.get("java.util", "HashMap");

        // Método para criar template de campos
        MethodSpec.Builder createTemplateMethod = MethodSpec.methodBuilder("createFieldTemplate")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .returns(ParameterizedTypeName.get(map, TypeName.get(Integer.class), fieldTemplate))
            .addJavadoc("Cria template de campos para decodificação");

        createTemplateMethod.addStatement("$T<Integer, $T> template = new $T<>()", 
            map, fieldTemplate, hashMap);

        for (FieldMeta f : meta.fields()) {
            createTemplateMethod.addStatement("template.put($L, new $T($T.$L, $L))",
                f.number(), fieldTemplate, isoType, f.type().name(), f.length());
        }

        createTemplateMethod.addStatement("return template");

        // Método fromIsoMessage
        MethodSpec.Builder fromIsoMessageMethod = MethodSpec.methodBuilder("fromIsoMessage")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(dto)
            .addParameter(isoMessage, "isoMessage")
            .addJavadoc("Converte IsoMessage em DTO")
            .addStatement("$T result = new $T()", dto, dto);

        // Extrai cada campo da mensagem
        for (FieldMeta f : meta.fields()) {
            String setter = "set" + capitalize(f.propertyName());
            fromIsoMessageMethod.addStatement("$T<?> field$L = isoMessage.getField($L)", 
                isoValue, f.number(), f.number());
            fromIsoMessageMethod.beginControlFlow("if (field$L != null)", f.number());
            
            // Conversão baseada no tipo do campo no DTO
            String javaType = f.element().asType().toString();
            if (javaType.contains("String")) {
                fromIsoMessageMethod.addStatement("result.$L(field$L.value().toString())", 
                    setter, f.number());
            } else if (javaType.contains("BigDecimal")) {
                fromIsoMessageMethod.addStatement("result.$L(new java.math.BigDecimal(field$L.value().toString()))", 
                    setter, f.number());
            } else if (javaType.contains("LocalDateTime")) {
                fromIsoMessageMethod.addStatement("result.$L(java.time.LocalDateTime.parse(field$L.value().toString()))", 
                    setter, f.number());
            } else {
                fromIsoMessageMethod.addStatement("result.$L(($L) field$L.value())", 
                    setter, javaType, f.number());
            }
            
            fromIsoMessageMethod.endControlFlow();
        }

        fromIsoMessageMethod.addStatement("return result");

        // Método decode simples
        MethodSpec decodeSimple = MethodSpec.methodBuilder("decode")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(dto)
            .addParameter(ArrayTypeName.of(TypeName.BYTE), "data")
            .addJavadoc("Decodifica bytes ISO 8583 em DTO")
            .addStatement("$T decoder = new $T()", isoDecoder, isoDecoder)
            .addStatement("$T<Integer, $T> template = createFieldTemplate()", 
                map, fieldTemplate)
            .addStatement("$T message = decoder.decodeWithTemplate(data, template)", isoMessage)
            .addStatement("return fromIsoMessage(message)")
            .build();

        // Método decode com factory
        MethodSpec decodeWithFactory = MethodSpec.methodBuilder("decode")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(dto)
            .addParameter(ArrayTypeName.of(TypeName.BYTE), "data")
            .addParameter(isoMessageFactory, "factory")
            .addJavadoc("Decodifica bytes ISO 8583 em DTO usando factory específica")
            .addStatement("$T message = factory.decode(data)", isoMessage)
            .addStatement("return fromIsoMessage(message)")
            .build();

        // Classe do decoder
        TypeSpec decoder = TypeSpec.classBuilder(decoderName)
            .addJavadoc("Decoder gerado automaticamente para $L.\nNão edite este arquivo.", meta.simpleName())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ParameterizedTypeName.get(isoMessageDecoder, dto))
            .addMethod(createTemplateMethod.build())
            .addMethod(fromIsoMessageMethod.build())
            .addMethod(decodeSimple)
            .addMethod(decodeWithFactory)
            .build();

        writeJavaFile(generatedPkg, decoder);
    }

    private void generateRegistry() {
        ClassName mapClass = ClassName.get("java.util", "Map");
        ClassName hashMapClass = ClassName.get("java.util", "HashMap");
        ClassName encoderRegistry = ClassName.get("com.example.iso8583.contract", "EncoderRegistry");
        ClassName isoMessageEncoder = ClassName.get("com.example.iso8583.contract", "IsoMessageEncoder");
        ClassName isoMessageDecoder = ClassName.get("com.example.iso8583.contract", "IsoMessageDecoder");

        TypeName encoderMapType = ParameterizedTypeName.get(mapClass, 
            ClassName.get(Class.class), isoMessageEncoder);
        TypeName decoderMapType = ParameterizedTypeName.get(mapClass, 
            ClassName.get(Class.class), isoMessageDecoder);

        // Campos para mapas
        FieldSpec encoderMapField = FieldSpec.builder(encoderMapType, "encoders", 
            Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new $T()", hashMapClass)
            .build();

        FieldSpec decoderMapField = FieldSpec.builder(decoderMapType, "decoders", 
            Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new $T()", hashMapClass)
            .build();

        // Construtor que registra todos os encoders/decoders
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC);

        for (MessageMeta messageMeta : collectedMessages) {
            ClassName dto = ClassName.get(messageMeta.packageName(), messageMeta.simpleName());
            ClassName encoder = ClassName.get(messageMeta.packageName() + ".generated", 
                messageMeta.simpleName() + "Encoder");
            ClassName decoder = ClassName.get(messageMeta.packageName() + ".generated", 
                messageMeta.simpleName() + "Decoder");
            
            constructor.addStatement("encoders.put($T.class, new $T())", dto, encoder);
            constructor.addStatement("decoders.put($T.class, new $T())", dto, decoder);
        }

        // Método getEncoder
        MethodSpec getEncoder = MethodSpec.methodBuilder("getEncoder")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(TypeVariableName.get("T"))
            .returns(ParameterizedTypeName.get(isoMessageEncoder, TypeVariableName.get("T")))
            .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), 
                TypeVariableName.get("T")), "dtoType")
            .addStatement("$T encoder = encoders.get(dtoType)", isoMessageEncoder)
            .beginControlFlow("if (encoder == null)")
            .addStatement("throw new IllegalStateException(\"No encoder found for \" + dtoType.getName())")
            .endControlFlow()
            .addStatement("return ($T) encoder", 
                ParameterizedTypeName.get(isoMessageEncoder, TypeVariableName.get("T")))
            .build();

        // Método getDecoder
        MethodSpec getDecoder = MethodSpec.methodBuilder("getDecoder")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(TypeVariableName.get("T"))
            .returns(ParameterizedTypeName.get(isoMessageDecoder, TypeVariableName.get("T")))
            .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), 
                TypeVariableName.get("T")), "dtoType")
            .addStatement("$T decoder = decoders.get(dtoType)", isoMessageDecoder)
            .beginControlFlow("if (decoder == null)")
            .addStatement("throw new IllegalStateException(\"No decoder found for \" + dtoType.getName())")
            .endControlFlow()
            .addStatement("return ($T) decoder", 
                ParameterizedTypeName.get(isoMessageDecoder, TypeVariableName.get("T")))
            .build();

        // Classe do registry
        TypeSpec registry = TypeSpec.classBuilder("GeneratedIso8583Registry")
            .addJavadoc("Registry gerado automaticamente.\nNão edite este arquivo.")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(encoderRegistry)
            .addField(encoderMapField)
            .addField(decoderMapField)
            .addMethod(constructor.build())
            .addMethod(getEncoder)
            .addMethod(getDecoder)
            .build();

        writeJavaFile("com.example.iso8583.generated", registry);
    }

    private void writeJavaFile(String packageName, TypeSpec typeSpec) {
        try {
            JavaFile.builder(packageName, typeSpec)
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
        } catch (IOException e) {
            throw new RuntimeException("Error writing generated file", e);
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
