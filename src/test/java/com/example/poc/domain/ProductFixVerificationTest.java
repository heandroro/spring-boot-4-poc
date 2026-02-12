package com.example.poc.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.poc.domain.vo.Money;
import com.example.poc.domain.vo.ProductImage;

@DisplayName("Verificação das Correções do Product")
class ProductFixVerificationTest {

    @Test
    @DisplayName("TESTE 1: Product.create deve gerar ID automaticamente")
    void testProductCreateGeneratesId() {
        var product = Product.create(
            "TEST-001",
            "Test Product",
            "Description",
            "Category",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        assertNotNull(product.getId(), "ID deve ser gerado automaticamente");
        assertFalse(product.getId().isEmpty(), "ID não deve estar vazio");
        System.out.println("✅ TESTE 1 PASSOU: ID gerado = " + product.getId());
    }

    @Test
    @DisplayName("TESTE 2: updatePrice deve garantir que updatedAt seja sempre posterior")
    void testUpdatePriceIncrementsUpdatedAt() throws InterruptedException {
        var product = Product.create(
            "TEST-002",
            "Test Product 2",
            "Description",
            "Category",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        var oldUpdatedAt = product.getUpdatedAt();

        // Aguarda 1ms para garantir que haveria diferença
        Thread.sleep(1);

        product.updatePrice(new Money(new BigDecimal("200.00"), "USD"));

        var newUpdatedAt = product.getUpdatedAt();

        assertTrue(newUpdatedAt.isAfter(oldUpdatedAt),
            "updatedAt (" + newUpdatedAt + ") deve ser posterior a oldUpdatedAt (" + oldUpdatedAt + ")");
        System.out.println("✅ TESTE 2 PASSOU: oldUpdatedAt = " + oldUpdatedAt + ", newUpdatedAt = " + newUpdatedAt);
    }

    @Test
    @DisplayName("TESTE 3: updatePrice em cenário de execução rápida")
    void testUpdatePriceInFastExecution() {
        var product = Product.create(
            "TEST-003",
            "Test Product 3",
            "Description",
            "Category",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        var oldUpdatedAt = product.getUpdatedAt();

        // Execução imediata (sem sleep) - caso crítico
        product.updatePrice(new Money(new BigDecimal("200.00"), "USD"));

        var newUpdatedAt = product.getUpdatedAt();

        assertTrue(newUpdatedAt.isAfter(oldUpdatedAt),
            "updatedAt deve ser posterior mesmo em execução imediata. " +
            "oldUpdatedAt = " + oldUpdatedAt + ", newUpdatedAt = " + newUpdatedAt);
        System.out.println("✅ TESTE 3 PASSOU: Incremento garantido mesmo sem delay");
    }
}

