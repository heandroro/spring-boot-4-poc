#!/bin/bash
cd /Volumes/Dock/ai/copilot/poc/spring-boot-4-poc
echo "=== Compilando o projeto ==="
./gradlew compileJava compileTestJava

echo ""
echo "=== Executando testes do ProductTest ==="
./gradlew test --tests "com.example.poc.domain.ProductTest.shouldCreateProductWithValidValues" --tests "com.example.poc.domain.ProductTest.shouldUpdatePriceForActiveProduct"

echo ""
echo "=== Verificando resultados ==="
if [ -f build/test-results/test/TEST-com.example.poc.domain.ProductTest.xml ]; then
    cat build/test-results/test/TEST-com.example.poc.domain.ProductTest.xml | grep -E "testcase|failure"
else
    echo "Arquivo de resultados não encontrado"
fi

