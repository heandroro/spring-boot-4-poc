package com.example.poc.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.poc.domain.Product;
import com.example.poc.domain.ProductRepository;
import com.example.poc.domain.vo.Money;
import com.example.poc.domain.vo.ProductImage;
import com.example.poc.infrastructure.mapping.ProductMapper;
import com.example.poc.web.ProductCreateDto;
import com.example.poc.web.ProductDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService service;

    private Product product;
    private ProductDto productDto;
    private ProductCreateDto createDto;

    @BeforeEach
    void setUp() {
        product = Product.create(
            "TEST-001",
            "Test Product",
            "Test Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        productDto = Instancio.create(ProductDto.class);

        createDto = new ProductCreateDto(
            "TEST-001",
            "Test Product",
            "Test Description",
            "Electronics",
            "100.00",
            "USD",
            10,
            null,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );
    }

    @Test
    @DisplayName("deve criar produto com sucesso")
    void shouldCreateProduct() {
        when(repository.existsBySku(createDto.sku())).thenReturn(false);
        when(mapper.toDomain(createDto)).thenReturn(product);
        when(repository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDto(product)).thenReturn(productDto);

        var result = service.create(createDto);

        assertNotNull(result);
        verify(repository).existsBySku(createDto.sku());
        verify(mapper).toDomain(createDto);
        verify(repository).save(any(Product.class));
        verify(mapper).toDto(product);
    }

    @Test
    @DisplayName("deve lançar exceção ao criar produto com SKU duplicado")
    void shouldThrowExceptionWhenCreatingProductWithDuplicateSku() {
        when(repository.existsBySku(createDto.sku())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(createDto));
        verify(repository).existsBySku(createDto.sku());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve buscar produto por ID")
    void shouldFindProductById() {
        when(repository.findById("1")).thenReturn(Optional.of(product));
        when(mapper.toDto(product)).thenReturn(productDto);

        var result = service.findById("1");

        assertTrue(result.isPresent());
        verify(repository).findById("1");
        verify(mapper).toDto(product);
    }

    @Test
    @DisplayName("deve retornar vazio quando produto não existe")
    void shouldReturnEmptyWhenProductNotFound() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        var result = service.findById("999");

        assertFalse(result.isPresent());
        verify(repository).findById("999");
        verify(mapper, never()).toDto(any());
    }

    @Test
    @DisplayName("deve buscar produto por SKU")
    void shouldFindProductBySku() {
        when(repository.findBySku("TEST-001")).thenReturn(Optional.of(product));
        when(mapper.toDto(product)).thenReturn(productDto);

        var result = service.findBySku("TEST-001");

        assertTrue(result.isPresent());
        verify(repository).findBySku("TEST-001");
        verify(mapper).toDto(product);
    }

    @Test
    @DisplayName("deve buscar produtos por categoria com paginação")
    void shouldFindProductsByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(repository.findByCategory("Electronics", pageable)).thenReturn(productPage);
        when(mapper.toDto(product)).thenReturn(productDto);

        var result = service.findByCategory("Electronics", pageable);

        assertEquals(1, result.getTotalElements());
        verify(repository).findByCategory("Electronics", pageable);
        verify(mapper).toDto(product);
    }

    @Test
    @DisplayName("deve buscar todos os produtos com paginação")
    void shouldFindAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(repository.findAll(pageable)).thenReturn(productPage);
        when(mapper.toDto(product)).thenReturn(productDto);

        var result = service.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(pageable);
        verify(mapper).toDto(product);
    }

    @Test
    @DisplayName("deve atualizar produto com sucesso")
    void shouldUpdateProduct() {
        when(repository.findById("1")).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);
        when(mapper.toDto(product)).thenReturn(productDto);

        var result = service.update("1", createDto);

        assertNotNull(result);
        verify(repository).findById("1");
        verify(repository).save(any(Product.class));
        verify(mapper).toDto(product);
    }

    @Test
    @DisplayName("deve lançar exceção ao atualizar produto inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.update("999", createDto));
        verify(repository).findById("999");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar exceção ao atualizar produto com SKU duplicado")
    void shouldThrowExceptionWhenUpdatingWithDuplicateSku() {
        var existingProduct = Product.create(
            "EXISTING-001",
            "Existing Product",
            "Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );

        when(repository.findById("1")).thenReturn(Optional.of(existingProduct));
        when(repository.existsBySku(createDto.sku())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.update("1", createDto));
        verify(repository).findById("1");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve atualizar produto quando o SKU mudar e não houver conflito")
    void shouldUpdateProductWhenSkuChangesToNewSku() {
        var productWithOldSku = Product.create(
            "OLD-001",
            "Old Product",
            "Description",
            "Electronics",
            new Money(new BigDecimal("100.00"), "USD"),
            10,
            List.of(ProductImage.primary("http://img", "alt"))
        );

        var dtoWithNewSku = new ProductCreateDto(
            "NEW-001",
            "Updated Product",
            "Updated Desc",
            "Electronics",
            "120.00",
            "USD",
            10,
            null,
            List.of(ProductImage.primary("http://img", "alt"))
        );

        when(repository.findById("1")).thenReturn(Optional.of(productWithOldSku));
        when(repository.existsBySku("NEW-001")).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(productWithOldSku);
        when(mapper.toDto(any(Product.class))).thenReturn(productDto);

        var result = service.update("1", dtoWithNewSku);

        assertNotNull(result);
        verify(repository).findById("1");
        verify(repository).existsBySku("NEW-001");
        verify(repository).save(any(Product.class));
        verify(mapper).toDto(any(Product.class));
    }

    @Test
    @DisplayName("deve deletar produto por ID")
    void shouldDeleteProductById() {
        service.deleteById("1");

        verify(repository).deleteById("1");
    }
}
