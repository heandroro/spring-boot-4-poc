package com.example.poc.infrastructure.mapping;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.example.poc.domain.Product;
import com.example.poc.domain.vo.Money;
import com.example.poc.domain.vo.ProductImage;
import com.example.poc.domain.vo.ProductRatings;
import com.example.poc.web.ProductCreateDto;

@DisplayName("ProductMapper")
class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ProductMapper.class);
    }

    @Test
    @DisplayName("deve mapear Product para ProductDto")
    void shouldMapProductToDto() {
        var product = Product.create(
            "TEST-001",
            "Test Product",
            "Test Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        var dto = mapper.toDto(product);

        assertNotNull(dto);
        assertEquals(product.getId(), dto.id());
        assertEquals(product.getSku(), dto.sku());
        assertEquals(product.getName(), dto.name());
        assertEquals(product.getDescription(), dto.description());
        assertEquals(product.getCategory(), dto.category());
        assertEquals("100.00", dto.price());
        assertEquals("USD", dto.currency());
        assertNotNull(dto.stock());
        assertEquals(10, dto.stock().available());
        assertEquals(0, dto.stock().reserved());
        assertEquals(10, dto.stock().total());
        assertNotNull(dto.ratings());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.updatedAt());
    }

    @Test
    @DisplayName("deve mapear ProductCreateDto para Product")
    void shouldMapCreateDtoToDomain() {
        var createDto = new ProductCreateDto(
            "TEST-002",
            "New Product",
            "New Description",
            "Electronics",
            "200.00",
            "EUR",
            20,
            Map.of("color", "blue"),
            List.of(ProductImage.primary("http://test.com/img2.jpg", "Test 2"))
        );

        var product = mapper.toDomain(createDto);

        assertNotNull(product);
        assertNotNull(product.getId());
        assertEquals(createDto.sku(), product.getSku());
        assertEquals(createDto.name(), product.getName());
        assertEquals(createDto.description(), product.getDescription());
        assertEquals(createDto.category(), product.getCategory());
        assertEquals(new BigDecimal("200.00"), product.getPrice().amount());
        assertEquals("EUR", product.getPrice().currency());
        assertEquals(20, product.getStock().available());
    }

    @Test
    @DisplayName("deve mapear ProductCreateDto com moeda padrão quando currency é null")
    void shouldMapCreateDtoWithDefaultCurrency() {
        var createDto = new ProductCreateDto(
            "TEST-003",
            "Product without currency",
            "Description",
            "Electronics",
            "150.00",
            null,
            15,
            null,
            List.of()
        );

        var product = mapper.toDomain(createDto);

        assertNotNull(product);
        assertEquals("USD", product.getPrice().currency());
    }

    @Test
    @DisplayName("deve mapear ProductCreateDto com moeda padrão quando currency está vazia")
    void shouldMapCreateDtoWithDefaultCurrencyWhenBlank() {
        var createDto = new ProductCreateDto(
            "TEST-004",
            "Product with blank currency",
            "Description",
            "Electronics",
            "175.00",
            "  ",
            18,
            null,
            List.of()
        );

        var product = mapper.toDomain(createDto);

        assertNotNull(product);
        assertEquals("USD", product.getPrice().currency());
    }

    @Test
    @DisplayName("deve retornar null ao mapear Product null")
    void shouldReturnNullWhenMappingNullProduct() {
        var dto = mapper.toDto(null);
        assertNull(dto);
    }

    @Test
    @DisplayName("deve mapear Product com specifications para ProductDto")
    void shouldMapProductWithSpecificationsToDto() {
        var product = Product.create(
            "TEST-005",
            "Product with specs",
            "Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );
        product.updateSpecifications(Map.of("color", "red", "size", "L"));

        var dto = mapper.toDto(product);

        assertNotNull(dto);
        assertEquals(Map.of("color", "red", "size", "L"), dto.specifications());
    }

    @Test
    @DisplayName("deve mapear Product com múltiplas imagens para ProductDto")
    void shouldMapProductWithMultipleImagesToDto() {
        var images = List.of(
            ProductImage.primary("http://test.com/primary.jpg", "Primary"),
            ProductImage.secondary("http://test.com/secondary.jpg", "Secondary")
        );
        var product = Product.create(
            "TEST-006",
            "Product with images",
            "Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            images
        );

        var dto = mapper.toDto(product);

        assertNotNull(dto);
        assertEquals(2, dto.images().size());
        assertTrue(dto.images().get(0).isPrimary());
        assertFalse(dto.images().get(1).isPrimary());
    }

    @Test
    @DisplayName("deve mapear Product com status INACTIVE para ProductDto")
    void shouldMapProductWithInactiveStatusToDto() {
        var product = Product.create(
            "TEST-007",
            "Inactive Product",
            "Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );
        product.setStatus(Product.Status.INACTIVE);

        var dto = mapper.toDto(product);

        assertNotNull(dto);
        assertEquals(Product.Status.INACTIVE, dto.status());
    }

    @Test
    @DisplayName("deve mapear ProductCreateDto com specifications null")
    void shouldMapCreateDtoWithNullSpecifications() {
        var createDto = new ProductCreateDto(
            "TEST-008",
            "Product with null specs",
            "Description",
            "Electronics",
            "100.00",
            "USD",
            10,
            null, // specifications null
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        var product = mapper.toDomain(createDto);

        assertNotNull(product);
        assertNotNull(product.getSpecifications());
        assertTrue(product.getSpecifications().isEmpty());
    }

    @Test
    @DisplayName("deve mapear ProductCreateDto com specifications preenchidas")
    void shouldMapCreateDtoWithSpecifications() {
        var specs = Map.<String, Object>of("color", "blue", "weight", "1.5kg");
        var createDto = new ProductCreateDto(
            "TEST-009",
            "Product with specs",
            "Description",
            "Electronics",
            "100.00",
            "USD",
            10,
            specs,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        var product = mapper.toDomain(createDto);

        assertNotNull(product);
        assertEquals(specs, product.getSpecifications());
    }

    @Test
    @DisplayName("deve mapear Product com stock reservado para ProductDto")
    void shouldMapProductWithReservedStockToDto() {
        var product = Product.create(
            "TEST-010",
            "Product with reserved stock",
            "Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );
        product.reserveStock(3);

        var dto = mapper.toDto(product);

        assertNotNull(dto);
        assertEquals(7, dto.stock().available());
        assertEquals(3, dto.stock().reserved());
        assertEquals(10, dto.stock().total());
    }

    @Test
    @DisplayName("deve mapear Product com ratings atualizados para ProductDto")
    void shouldMapProductWithUpdatedRatingsToDto() {
        var product = Product.create(
            "TEST-011",
            "Product with ratings",
            "Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );
        product.updateRatings(new ProductRatings(4.2, 25));

        var dto = mapper.toDto(product);

        assertNotNull(dto);
        assertEquals(4.2, dto.ratings().average());
        assertEquals(25, dto.ratings().count());
    }

    @Test
    @DisplayName("deve mapear ProductCreateDto com price null para não lançar e usar default currency se necessário")
    void shouldMapCreateDtoWithNullPrice() {
        var createDto = new ProductCreateDto(
            "TEST-020",
            "Product with null price",
            "Description",
            "Electronics",
            null,
            null,
            5,
            null,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        var product = mapper.toDomain(createDto);

        assertNotNull(product);
        assertNotNull(product.getPrice());
        assertEquals("USD", product.getPrice().currency());
    }

    @Test
    @DisplayName("deve mapear Product com campos nulos para DTO com campos null")
    void shouldMapProductWithNullFieldsToDto() throws Exception {
        var product = Product.create(
            "TEST-030",
            "Null Fields Product",
            "Desc",
            "Electronics",
            new Money(new BigDecimal("10.00"), "USD"),
            5,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        // Forçar stock, ratings e datas para null via reflection
        var stockField = Product.class.getDeclaredField("stock");
        stockField.setAccessible(true);
        stockField.set(product, null);

        var ratingsField = Product.class.getDeclaredField("ratings");
        ratingsField.setAccessible(true);
        ratingsField.set(product, null);

        var createdField = Product.class.getDeclaredField("createdAt");
        createdField.setAccessible(true);
        createdField.set(product, null);

        var updatedField = Product.class.getDeclaredField("updatedAt");
        updatedField.setAccessible(true);
        updatedField.set(product, null);

        var dto = mapper.toDto(product);

        assertNotNull(dto);
        assertNull(dto.stock());
        assertNull(dto.ratings());
        assertNull(dto.createdAt());
        assertNull(dto.updatedAt());
    }
}
