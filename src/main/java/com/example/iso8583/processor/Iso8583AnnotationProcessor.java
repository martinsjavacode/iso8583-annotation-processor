package com.example.iso8583.processor;

import com.example.iso8583.annotation.Iso8583Field;
import com.example.iso8583.annotation.Iso8583Message;
import com.example.iso8583.processor.meta.FieldMeta;
import com.example.iso8583.processor.meta.MessageMeta;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

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

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.example.iso8583.annotation.Iso8583Message","com.example.iso8583.annotation.Iso8583Field"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class Iso8583AnnotationProcessor extends AbstractProcessor {
	private Elements elementUtils;
	private Types typeUtils;
	private Filer filer;
	private Messager messager;
	private final List<MessageMeta> collectedMessages = new ArrayList<>();

	private static String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.elementUtils = processingEnv.getElementUtils();
		this.typeUtils = processingEnv.getTypeUtils();
		this.filer = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();

		this.messager.printMessage(Kind.NOTE,
				"ISO 8583 Annotation Processor started...");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			if (!collectedMessages.isEmpty()) {
				generateRegister();
			}
			return true;
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Iso8583Message.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				messager.printMessage(Kind.ERROR, "Annotation @Iso8583Message can only be applied to a class", element);
				continue;
			}

			TypeElement dtoType = (TypeElement) element;
			final Iso8583Message iso8583Message = element.getAnnotation(Iso8583Message.class);
			final int mti = iso8583Message.mti();

			List<FieldMeta> fields = new ArrayList<>();
			for (Element enclosed : dtoType.getEnclosedElements()) {
				if (enclosed.getKind() == ElementKind.FIELD) {
					VariableElement varElement = (VariableElement) enclosed;
					Iso8583Field iso8583Field = varElement.getAnnotation(Iso8583Field.class);
					if (iso8583Field != null) {
						final String propName = varElement.getSimpleName().toString();
						fields.add(
								new FieldMeta(
										varElement,
										iso8583Field.number(),
										iso8583Field.type(),
										iso8583Field.length(),
										iso8583Field.required(),
										propName
								)
						);
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
			collectedMessages.add(messageMeta);
		}

		return true;
	}

	/*
	Validações mínimas:
	- Duplicidade de DE: dois campos com number igual → erro.
	- Tamanho x tipo: para NUMERIC/ALPHA exigir length>0; para LLVAR/LLLVAR permitir length=0.
	 */
	private void validate(MessageMeta meta) {
		Set<Integer> seen = new HashSet<>();
		for (FieldMeta f : meta.fields()) {
			if (f.number() <= 1 || f.number() >= 128) {
				messager.printMessage(
						Kind.ERROR,
						"Field ISO should between 1-128",
						f.element()
				);
			}

			if (!seen.add(f.number())) {
				messager.printMessage(
						Kind.ERROR,
						"Field ISO " + f.number() + " duplicated.",
						f.element()
				);
			}

			if (f.type().needsLength() && f.length() <= 0) {
				messager.printMessage(
						Kind.ERROR,
						"Type " + f.type().name() + " needs length > 0",
						f.element()
				);
			}
		}
	}

	private void generateEncoder(MessageMeta meta) {
		ClassName isoMessage = ClassName.get("com.solab.iso8583", "IsoMessage");
		ClassName messageFactory = ClassName.get("com.solab.iso8583", "MessageFactory");
		ClassName isoType = ClassName.get("com.solab.iso8583", "IsoType");
		ClassName isoMessageEncoder = ClassName.get("com.example.iso8583.api", "IsoMessageEncoder");
		TypeName factory = ParameterizedTypeName.get(messageFactory, isoType);

		ClassName dto = ClassName.get(meta.packageName(), meta.simpleName());
		String generatedPkg = meta.packageName() + ".generated";
		String encoderName = meta.simpleName() + "Encoder";

		// Method encode(dto, factory)
		MethodSpec.Builder encode = MethodSpec.methodBuilder("encode")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(isoMessage)
				.addParameter(dto, "dto")
				.addParameter(factory, "factory")
				.addJavadoc("Generate a IsoMessage from the $L.\n", meta.simpleName());

		// Create message
		encode.addStatement("$T msg = (factory != null) ? factory.newMessage($L) : new $T()", isoMessage, meta.mti(), isoMessage);

		// Validation null for requirements fields
		for (FieldMeta f : meta.fields()) {
			if (f.required()) {
				encode.beginControlFlow("if (dto.get$L() == null)", capitalize(f.propertyName()))
						.addStatement("throw new IllegalArgumentException(\"Field %s (DE %d) is required\")", f.propertyName(), f.number())
						.endControlFlow();
			}
		}

		// Set of each number
		for (FieldMeta f : meta.fields()) {
			String getter = "get" + capitalize(f.propertyName()) + "()";
			if (f.type().needsLength()) {
				encode.addStatement("msg.setValue($L, dto.$L, $T.$L, $L)", f.number(), getter, isoType, f.type().name(), f.length());
			} else {
				encode.addStatement("msg.setValue($L, dto.$L, $T.$L)", f.number(), getter, isoType, f.type().name());
			}
		}

		encode.addStatement("return msg");

		TypeSpec encoder = TypeSpec.classBuilder(encoderName)
				.addJavadoc("Generated by Iso8583AnnotationProcessor. Do not edit it")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addSuperinterface(ParameterizedTypeName.get(isoMessageEncoder, dto))
				.addMethod(encode.build())
				.build();

		try {
			JavaFile.builder(generatedPkg, encoder)
					.skipJavaLangImports(true)
					.build()
					.writeTo(filer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void generateRegister() {
		ClassName mapClass = ClassName.get("java.util", "Map");
		ClassName hashMapClass = ClassName.get("java.util", "HashMap");
		ClassName encoderRegistry = ClassName.get("com.example.iso8583.api", "EncoderRegistry");
		ClassName isoMessageEncoder = ClassName.get("com.example.iso8583.api", "IsoMessageEncoder");

		TypeName mapType = ParameterizedTypeName.get(mapClass, ClassName.get(Class.class), isoMessageEncoder);

		// Private number
		FieldSpec mapField = FieldSpec.builder(mapType, "map", Modifier.PRIVATE, Modifier.FINAL)
				.initializer("new $T()", hashMapClass)
				.build();

		// Constructor that register all encoders
		MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC);

		for (MessageMeta messageMeta : collectedMessages) {
			ClassName dto = ClassName.get(messageMeta.packageName(), messageMeta.simpleName());
			ClassName encoder = ClassName.get(messageMeta.packageName() + ".generated", messageMeta.simpleName() + "Encoder");
			ctor.addStatement("map.put($T.class, new $T())", dto, encoder);
		}

		// getEncoder method
		MethodSpec getEncoder = MethodSpec.methodBuilder("getEncoder")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addTypeVariable(TypeVariableName.get("T"))
				.returns(ParameterizedTypeName.get(isoMessageEncoder, TypeVariableName.get("T")))
				.addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("T")), "dtoType")
				.addStatement("$T encoder = map.get(dtoType)", isoMessageEncoder)
				.beginControlFlow("if (encoder == null)")
				.addStatement("throw new IllegalStateException(\"Without encoder for \" + dtoType.getName())")
				.endControlFlow()
				.addStatement("return ($T) encoder", ParameterizedTypeName.get(isoMessageEncoder, TypeVariableName.get("T")))
				.build();

		TypeSpec registry = TypeSpec.classBuilder("GeneratedEncoderRegistry")
				.addJavadoc("Registry automatic generated by Iso8583AnnotationProcessor. Do not edit it.")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addSuperinterface(encoderRegistry)
				.addField(mapField)
				.addMethod(ctor.build())
				.addMethod(getEncoder)
				.build();

		try {
			JavaFile.builder("com.example.iso8583.generated", registry)
					.skipJavaLangImports(true)
					.build()
					.writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
