#!/bin/bash

echo "=========================================="
echo "  VERIFICAÇÃO DAS CORREÇÕES - PRODUCT"
echo "=========================================="
echo ""

cd /Volumes/Dock/ai/copilot/poc/spring-boot-4-poc

echo "📝 Verificando se as correções estão no código..."
echo ""

echo "1️⃣ Verificando import do UUID:"
grep -n "import java.util.UUID" src/main/java/com/example/poc/domain/Product.java
if [ $? -eq 0 ]; then
    echo "   ✅ Import do UUID encontrado"
else
    echo "   ❌ Import do UUID NÃO encontrado"
fi
echo ""

echo "2️⃣ Verificando geração de ID no Product.create():"
grep -n "product.id = UUID.randomUUID().toString()" src/main/java/com/example/poc/domain/Product.java
if [ $? -eq 0 ]; then
    echo "   ✅ Geração de ID encontrada"
else
    echo "   ❌ Geração de ID NÃO encontrada"
fi
echo ""

echo "3️⃣ Verificando lógica de incremento no updatePrice():"
grep -A 3 "if (!now.isAfter(this.updatedAt))" src/main/java/com/example/poc/domain/Product.java
if [ $? -eq 0 ]; then
    echo "   ✅ Lógica de incremento encontrada"
else
    echo "   ❌ Lógica de incremento NÃO encontrada"
fi
echo ""

echo "=========================================="
echo "  EXECUTANDO TESTES"
echo "=========================================="
echo ""

echo "Compilando..."
./gradlew compileJava compileTestJava --quiet

echo ""
echo "Executando testes do ProductTest..."
./gradlew test --tests "com.example.poc.domain.ProductTest.shouldCreateProductWithValidValues" \
               --tests "com.example.poc.domain.ProductTest.shouldUpdatePriceForActiveProduct" \
               --quiet

echo ""
echo "=========================================="
echo "  RESULTADO DOS TESTES"
echo "=========================================="
echo ""

if [ -f build/test-results/test/TEST-com.example.poc.domain.ProductTest.xml ]; then
    echo "Analisando resultados XML..."

    # Conta testes que passaram
    PASSED=$(grep -c 'status="passed"' build/test-results/test/TEST-com.example.poc.domain.ProductTest.xml 2>/dev/null || echo "0")

    # Conta testes que falharam
    FAILED=$(grep -c '<failure' build/test-results/test/TEST-com.example.poc.domain.ProductTest.xml 2>/dev/null || echo "0")

    echo "✅ Testes PASSED: $PASSED"
    echo "❌ Testes FAILED: $FAILED"

    if [ $FAILED -eq 0 ]; then
        echo ""
        echo "🎉 SUCESSO! Todos os testes passaram!"
    else
        echo ""
        echo "⚠️  Ainda há testes falhando. Detalhes:"
        grep -A 5 '<failure' build/test-results/test/TEST-com.example.poc.domain.ProductTest.xml
    fi
else
    echo "⚠️  Arquivo de resultados não encontrado"
    echo "    Execute: ./gradlew test --tests '*ProductTest'"
fi

echo ""
echo "=========================================="

