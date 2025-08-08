#!/bin/bash

# Script de Compilação Otimizada para ISO 8583 com JSR 269
# Baseado na estratégia que funcionou anteriormente

set -e

echo "🚀 Compilação ISO 8583 com JSR 269 - Estratégia Otimizada"
echo "=========================================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para log colorido
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Verificar pré-requisitos
log_info "Verificando pré-requisitos..."
if ! command -v java &> /dev/null; then
    log_error "Java não encontrado"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    log_error "Maven não encontrado"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    log_error "Java 21+ necessário. Versão atual: $JAVA_VERSION"
    exit 1
fi

log_success "Java $JAVA_VERSION detectado"
log_success "Maven $(mvn -version | head -n 1 | cut -d' ' -f3) detectado"

# Etapa 1: Limpeza
log_info "Etapa 1: Limpeza do projeto"
mvn clean -q
log_success "Projeto limpo"

# Etapa 2: Gerar classpath
log_info "Etapa 2: Gerando classpath"
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt -q
log_success "Classpath gerado"

# Etapa 3: Criar diretórios necessários
log_info "Etapa 3: Criando estrutura de diretórios"
mkdir -p target/classes
mkdir -p target/generated-sources/annotations
log_success "Diretórios criados"

# Etapa 4: Compilar annotation processor manualmente
log_info "Etapa 4: Compilando annotation processor"
javac -cp $(cat cp.txt) -d target/classes \
    src/main/java/com/example/iso8583/annotation/*.java \
    src/main/java/com/example/iso8583/processor/Iso8583AnnotationProcessor.java 2>/dev/null
log_success "Annotation processor compilado"

# Etapa 5: Processar anotações no DTO
log_info "Etapa 5: Processando anotações no DTO"
javac -cp $(cat cp.txt):target/classes \
    -processor com.example.iso8583.processor.Iso8583AnnotationProcessor \
    -s target/generated-sources/annotations \
    -d target/classes \
    src/main/java/com/example/iso8583/dto/PurchaseRequestDto.java 2>/dev/null

# Verificar se a classe foi gerada
GENERATED_CLASS="target/generated-sources/annotations/com/example/iso8583/dto/generated/PurchaseRequestDtoProcessor.java"
if [ -f "$GENERATED_CLASS" ]; then
    log_success "PurchaseRequestDtoProcessor gerado com sucesso"
else
    log_error "Falha ao gerar PurchaseRequestDtoProcessor"
    exit 1
fi

# Etapa 6: Compilar projeto completo
log_info "Etapa 6: Compilando projeto completo"
mvn compile -Dmaven.compiler.proc=none -q
log_success "Projeto compilado com sucesso"

# Etapa 7: Executar testes (opcional)
if [ "$1" = "--with-tests" ]; then
    log_info "Etapa 7: Executando testes"
    mvn test -q
    log_success "Testes executados com sucesso"
fi

echo ""
log_success "🎉 Compilação concluída com sucesso!"
echo ""
log_info "Classes geradas disponíveis em:"
echo "  - target/generated-sources/annotations/com/example/iso8583/dto/generated/"
echo ""
log_info "Para executar a aplicação:"
echo "  mvn spring-boot:run"
echo ""
log_info "Para executar testes:"
echo "  mvn test"
echo ""
log_info "Para recompilar rapidamente:"
echo "  ./compile-iso8583.sh"
