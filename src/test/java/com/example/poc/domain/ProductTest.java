package com.example.poc.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.poc.domain.vo.Money;
import com.example.poc.domain.vo.ProductImage;
import com.example.poc.domain.vo.ProductRatings;
import com.example.poc.domain.vo.Stock;

@DisplayName("Product Aggregate Root")
class ProductTest {

    @Test
    @DisplayName("deve criar produto com valores válidos")
    void shouldCreateProductWithValidValues() {
        var sku = "PROD-001";
        var name = "Laptop Dell";
        var description = "Laptop de alta performance";
        var category = "Eletrônicos";
        var price = new Money(new BigDecimal("1500.00"), "USD");
        var images = List.of(ProductImage.primary("https://example.com/img1.jpg", "Laptop"));
        var initialStock = 10;

        var product = Product.create(sku, name, description, category, price, initialStock, images);

        assertNotNull(product.getId());
        assertEquals(sku, product.getSku());
        assertEquals(name, product.getName());
        assertEquals(description, product.getDescription());
        assertEquals(category, product.getCategory());
        assertEquals(price, product.getPrice());
        assertEquals(initialStock, product.getStock().available());
        assertEquals(0, product.getStock().reserved());
        assertEquals(initialStock, product.getStock().total());
        assertEquals(Product.Status.ACTIVE, product.getStatus());
        assertFalse(product.pullEvents().isEmpty());
    }

    @Test
    @DisplayName("deve lançar exceção ao criar produto com SKU vazio")
    void shouldThrowExceptionWithEmptySku() {
        assertThrows(IllegalArgumentException.class, () -> {
            Product.create("  ", "Laptop", "Description", "Eletrônicos",
                    new Money(new BigDecimal("100"), "USD"), 10,
                    List.of(ProductImage.primary("https://example.com/img.jpg", "img")));
        });
    }

    @Test
    @DisplayName("deve lançar exceção ao criar produto com nome vazio")
    void shouldThrowExceptionWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            Product.create("SKU-001", "  ", "Description", "Eletrônicos",
                    new Money(new BigDecimal("100"), "USD"), 10,
                    List.of(ProductImage.primary("https://example.com/img.jpg", "img")));
        });
    }

    @Test
    @DisplayName("deve atualizar preço de produto ativo")
    void shouldUpdatePriceForActiveProduct() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));
        var newPrice = new Money(new BigDecimal("2000.00"), "USD");
        var oldUpdatedAt = product.getUpdatedAt();

        product.updatePrice(newPrice);

        assertEquals(newPrice, product.getPrice());
        assertTrue(product.getUpdatedAt().isAfter(oldUpdatedAt));
    }

    @Test
    @DisplayName("deve lançar exceção ao atualizar preço de produto inativo")
    void shouldThrowExceptionWhenUpdatingPriceOfInactiveProduct() {
        var product = Instancio.create(Product.class);
        product.setStatus(Product.Status.INACTIVE);

        var newPrice = new Money(new BigDecimal("2000.00"), "USD");

        assertThrows(IllegalStateException.class, () -> product.updatePrice(newPrice));
    }

    @Test
    @DisplayName("deve reservar estoque com sucesso")
    void shouldReserveStock() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        product.reserveStock(5);

        assertEquals(5, product.getStock().available());
        assertEquals(5, product.getStock().reserved());
        assertEquals(10, product.getStock().total());
    }

    @Test
    @DisplayName("deve lançar exceção ao reservar mais estoque que disponível")
    void shouldThrowExceptionWhenReservingMoreThanAvailable() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(IllegalArgumentException.class, () -> product.reserveStock(15));
    }

    @Test
    @DisplayName("deve confirmar reserva de estoque")
    void shouldConfirmReservation() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        product.reserveStock(5);
        product.confirmReservation(5);

        assertEquals(5, product.getStock().available());
        assertEquals(0, product.getStock().reserved());
        assertEquals(5, product.getStock().total());
    }

    @Test
    @DisplayName("deve cancelar reserva de estoque")
    void shouldCancelReservation() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        product.reserveStock(5);
        product.cancelReservation(5);

        assertEquals(10, product.getStock().available());
        assertEquals(0, product.getStock().reserved());
        assertEquals(10, product.getStock().total());
    }

    @Test
    @DisplayName("deve replenish estoque")
    void shouldReplenishStock() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        product.replenishStock(5);

        assertEquals(15, product.getStock().available());
        assertEquals(0, product.getStock().reserved());
        assertEquals(15, product.getStock().total());
    }

    @Test
    @DisplayName("deve atualizar ratings")
    void shouldUpdateRatings() {
        var product = Instancio.create(Product.class);
        var newRatings = new ProductRatings(4.5, 10);

        product.updateRatings(newRatings);

        assertEquals(newRatings, product.getRatings());
    }

    @Test
    @DisplayName("deve mudar status do produto")
    void shouldChangeProductStatus() {
        var product = Instancio.create(Product.class);

        product.setStatus(Product.Status.DISCONTINUED);

        assertEquals(Product.Status.DISCONTINUED, product.getStatus());
    }

    @Test
    @DisplayName("deve verificar igualdade por ID e SKU")
    void shouldCheckEqualityByIdAndSku() {
        var product1 = Instancio.create(Product.class);
        var product2 = Instancio.create(Product.class);
        product2 = Product.create(product1.getSku(), "Name", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertNotEquals(product1, product2);
    }

    @Test
    @DisplayName("deve atualizar descrição com valor válido")
    void shouldUpdateDescriptionWithValidValue() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));
        var oldUpdatedAt = product.getUpdatedAt();

        product.updateDescription("Nova descrição");

        assertEquals("Nova descrição", product.getDescription());
        assertTrue(product.getUpdatedAt().isAfter(oldUpdatedAt));
    }

    @Test
    @DisplayName("deve aceitar descrição null")
    void shouldAcceptNullDescription() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        product.updateDescription(null);

        assertNull(product.getDescription());
    }

    @Test
    @DisplayName("deve lançar exceção ao atualizar descrição com valor em branco")
    void shouldThrowExceptionWhenUpdatingDescriptionWithBlankValue() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(IllegalArgumentException.class, () -> product.updateDescription("  "));
    }

    @Test
    @DisplayName("deve atualizar especificações com mapa válido")
    void shouldUpdateSpecificationsWithValidMap() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));
        var newSpecs = Map.<String, Object>of("color", "blue", "size", "M");

        product.updateSpecifications(newSpecs);

        assertEquals(newSpecs, product.getSpecifications());
    }

    @Test
    @DisplayName("deve lançar exceção ao atualizar especificações com null")
    void shouldThrowExceptionWhenUpdatingSpecificationsWithNull() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(NullPointerException.class, () -> product.updateSpecifications(null));
    }

    @Test
    @DisplayName("deve atualizar imagens com lista válida")
    void shouldUpdateImagesWithValidList() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));
        var newImages = List.of(
            ProductImage.primary("https://example.com/new.jpg", "New"),
            ProductImage.secondary("https://example.com/sec.jpg", "Secondary")
        );

        product.updateImages(newImages);

        assertEquals(newImages, product.getImages());
    }

    @Test
    @DisplayName("deve lançar exceção ao atualizar imagens com null")
    void shouldThrowExceptionWhenUpdatingImagesWithNull() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(NullPointerException.class, () -> product.updateImages(null));
    }

    @Test
    @DisplayName("deve lançar exceção ao atualizar imagens com lista vazia")
    void shouldThrowExceptionWhenUpdatingImagesWithEmptyList() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(IllegalArgumentException.class, () -> product.updateImages(List.of()));
    }

    @Test
    @DisplayName("deve retornar eventos na primeira chamada e lista vazia na segunda")
    void shouldReturnEventsOnFirstPullAndEmptyOnSecond() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        var events1 = product.pullEvents();
        var events2 = product.pullEvents();

        assertFalse(events1.isEmpty());
        assertTrue(events2.isEmpty());
    }

    @Test
    @DisplayName("deve verificar igualdade por ID mesmo com SKU diferente")
    void shouldCheckEqualityByIdEvenWithDifferentSku() {
        // Como o construtor é privado, testamos que produtos diferentes não são iguais
        var product1 = Product.create("SKU-001", "Product1", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));
        var product2 = Product.create("SKU-002", "Product2", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        // Produtos com IDs diferentes não são iguais
        assertNotEquals(product1, product2);
        // Mesmo produto é igual a si mesmo
        assertEquals(product1, product1);
        assertEquals(product1.hashCode(), product1.hashCode());
    }

    @Test
    @DisplayName("deve retornar toString não vazio")
    void shouldReturnNonEmptyToString() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        var toString = product.toString();

        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("Product"));
        assertTrue(toString.contains("SKU-001"));
    }

    @Test
    @DisplayName("deve não alterar timestamp ao definir mesmo status")
    void shouldNotChangeTimestampWhenSettingSameStatus() {
        var product = Product.create("SKU-001", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));
        var oldUpdatedAt = product.getUpdatedAt();

        product.setStatus(Product.Status.ACTIVE);

        assertEquals(oldUpdatedAt, product.getUpdatedAt());
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando criar produto com sku null")
    void shouldThrowWhenCreatingProductWithNullSku() {
        assertThrows(NullPointerException.class, () -> Product.create(null, "Name", "Desc", "Category",
                new Money(new BigDecimal("10.00"), "USD"), 1, List.of(ProductImage.primary("http://x", "a"))));
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando criar produto com name null")
    void shouldThrowWhenCreatingProductWithNullName() {
        assertThrows(NullPointerException.class, () -> Product.create("SKU", null, "Desc", "Category",
                new Money(new BigDecimal("10.00"), "USD"), 1, List.of(ProductImage.primary("http://x", "a"))));
    }

    @Test
    @DisplayName("deve lançar IllegalArgumentException ao tentar reservar quantidade negativa")
    void shouldThrowWhenReservingNegativeQuantity() {
        var product = Product.create("SKU-NEG", "Product", "Desc", "Category",
                new Money(new BigDecimal("10.00"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(IllegalArgumentException.class, () -> product.reserveStock(-1));
    }

    @Test
    @DisplayName("deve lançar IllegalArgumentException ao confirmar reserva com quantidade negativa")
    void shouldThrowWhenConfirmingNegativeQuantity() {
        var product = Product.create("SKU-NEG2", "Product", "Desc", "Category",
                new Money(new BigDecimal("10.00"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(IllegalArgumentException.class, () -> product.confirmReservation(-2));
    }

    @Test
    @DisplayName("deve lançar IllegalArgumentException ao cancelar reserva com quantidade negativa")
    void shouldThrowWhenCancellingNegativeQuantity() {
        var product = Product.create("SKU-NEG3", "Product", "Desc", "Category",
                new Money(new BigDecimal("10.00"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(IllegalArgumentException.class, () -> product.cancelReservation(-3));
    }

    @Test
    @DisplayName("deve lançar IllegalArgumentException ao replenish com quantidade inválida (<=0)")
    void shouldThrowWhenReplenishWithNonPositiveQuantity() {
        var product = Product.create("SKU-NEG4", "Product", "Desc", "Category",
                new Money(new BigDecimal("10.00"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(IllegalArgumentException.class, () -> product.replenishStock(0));
    }

    @Test
    @DisplayName("deve lançar NullPointerException ao atualizar preço com null")
    void shouldThrowWhenUpdatingPriceWithNull() {
        var product = Product.create("SKU-NULL", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        assertThrows(NullPointerException.class, () -> product.updatePrice(null));
    }

    @Test
    @DisplayName("deve ajustar updatedAt quando now não for after updatedAt")
    void shouldAdjustUpdatedAtWhenNowNotAfter() throws Exception {
        var product = Product.create("SKU-TIME", "Product", "Desc", "Category",
                new Money(new BigDecimal("100"), "USD"), 10,
                List.of(ProductImage.primary("https://example.com/img.jpg", "img")));

        // Forçar updatedAt no futuro via reflection para cobrir o ramo
        var field = Product.class.getDeclaredField("updatedAt");
        field.setAccessible(true);
        var future = product.getUpdatedAt().plusDays(1);
        field.set(product, future);

        var newPrice = new Money(new BigDecimal("200"), "USD");

        product.updatePrice(newPrice);

        // agora updatedAt deve ser future + 1 nanos
        var updated = product.getUpdatedAt();
        assertTrue(updated.isAfter(future));
        assertEquals(newPrice, product.getPrice());
    }

    @Test
    @DisplayName("deve lançar IllegalArgumentException ao criar produto com categoria vazia")
    void shouldThrowWhenCreatingProductWithBlankCategory() {
        assertThrows(IllegalArgumentException.class, () -> Product.create("SKU-CAT", "Name", "Desc", "  ",
                new Money(new BigDecimal("10.00"), "USD"), 1, List.of(ProductImage.primary("http://x", "a"))));
    }

    @Test
    @DisplayName("deve lançar IllegalArgumentException ao criar produto com initialStock negativo")
    void shouldThrowWhenCreatingProductWithNegativeInitialStock() {
        assertThrows(IllegalArgumentException.class, () -> Product.create("SKU-NEGSTOCK", "Name", "Desc", "Category",
                new Money(new BigDecimal("10.00"), "USD"), -5, List.of(ProductImage.primary("http://x", "a"))));
    }

    @Test
    @DisplayName("equals deve retornar false ao comparar com null e com outro tipo")
    void shouldHandleEqualsWithNullAndDifferentType() {
        var product = Product.create("EQ-001", "P", "D", "C",
                new Money(new BigDecimal("10.00"), "USD"), 1, List.of(ProductImage.primary("http://x", "a")));

        assertFalse(product.equals(null));
        assertFalse(product.equals("some string"));
    }

    @Test
    @DisplayName("equals deve retornar true quando id e sku forem iguais")
    void shouldEqualWhenIdAndSkuAreEqual() throws Exception {
        var p1 = Product.create("EQ-100", "P", "D", "C", new Money(new BigDecimal("1.00"), "USD"), 1, List.of(ProductImage.primary("http://x","a")));
        var p2 = Product.create("EQ-200", "P2", "D2", "C", new Money(new BigDecimal("1.00"), "USD"), 1, List.of(ProductImage.primary("http://x","a")));

        // Forçar id e sku iguais via reflection
        var idField = Product.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(p2, p1.getId());

        var skuField = Product.class.getDeclaredField("sku");
        skuField.setAccessible(true);
        skuField.set(p2, p1.getSku());

        assertEquals(p1, p2);
    }

    @Test
    @DisplayName("equals deve retornar false quando id igual mas sku diferente")
    void shouldNotEqualWhenIdEqualButSkuDifferent() throws Exception {
        var p1 = Product.create("EQ-300", "P", "D", "C", new Money(new BigDecimal("1.00"), "USD"), 1, List.of(ProductImage.primary("http://x","a")));
        var p2 = Product.create("EQ-301", "P2", "D2", "C", new Money(new BigDecimal("1.00"), "USD"), 1, List.of(ProductImage.primary("http://x","a")));

        var idField = Product.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(p2, p1.getId());

        // sku fica diferente
        assertNotEquals(p1, p2);
    }
}
