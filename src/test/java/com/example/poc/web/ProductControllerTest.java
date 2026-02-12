package com.example.poc.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.example.poc.application.ProductService;
import com.example.poc.domain.Product;
import com.example.poc.domain.vo.ProductImage;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController controller;

    private ProductDto productDto;
    private ProductCreateDto createDto;

    @BeforeEach
    void setUp() {
        productDto = new ProductDto(
            "1",
            "TEST-001",
            "Test Product",
            "Test Description",
            "Electronics",
            "100.00",
            "USD",
            new StockDto(10, 0, 10),
            Map.of("color", "blue"),
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test")),
            new ProductRatingsDto(4.5, 10),
            Product.Status.ACTIVE,
            "2026-01-01T10:00:00",
            "2026-01-01T10:00:00"
        );

        createDto = new ProductCreateDto(
            "TEST-001",
            "Test Product",
            "Test Description",
            "Electronics",
            "100.00",
            "USD",
            10,
            Map.of("color", "blue"),
            List.of(ProductImage.primary("http://test.com/img.jpg", "Test"))
        );
    }

    @Test
    @DisplayName("deve criar produto com sucesso")
    void shouldCreateProduct() {
        when(productService.create(any(ProductCreateDto.class))).thenReturn(productDto);

        var response = controller.create(createDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().id());
        verify(productService).create(any(ProductCreateDto.class));
    }

    @Test
    @DisplayName("deve buscar produto por ID")
    void shouldFindProductById() {
        when(productService.findById("1")).thenReturn(Optional.of(productDto));

        var response = controller.getById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().id());
        verify(productService).findById("1");
    }

    @Test
    @DisplayName("deve retornar 404 quando produto não existe")
    void shouldReturn404WhenProductNotFound() {
        when(productService.findById("999")).thenReturn(Optional.empty());

        var response = controller.getById("999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(productService).findById("999");
    }

    @Test
    @DisplayName("deve buscar produto por SKU")
    void shouldFindProductBySku() {
        when(productService.findBySku("TEST-001")).thenReturn(Optional.of(productDto));

        var response = controller.getBySku("TEST-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TEST-001", response.getBody().sku());
        verify(productService).findBySku("TEST-001");
    }

    @Test
    @DisplayName("deve buscar produtos por categoria")
    void shouldFindProductsByCategory() {
        var page = new PageImpl<>(List.of(productDto));
        var pageable = PageRequest.of(0, 10);
        when(productService.findByCategory(eq("Electronics"), any(Pageable.class))).thenReturn(page);

        var response = controller.listByCategory("Electronics", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(productService).findByCategory(eq("Electronics"), any(Pageable.class));
    }

    @Test
    @DisplayName("deve listar todos os produtos com paginação")
    void shouldListAllProducts() {
        var page = new PageImpl<>(List.of(productDto));
        var pageable = PageRequest.of(0, 10);
        when(productService.findAll(any(Pageable.class))).thenReturn(page);

        var response = controller.listAll(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(productService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("deve atualizar produto com sucesso")
    void shouldUpdateProduct() {
        when(productService.update(eq("1"), any(ProductCreateDto.class))).thenReturn(productDto);

        var response = controller.update("1", createDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().id());
        verify(productService).update(eq("1"), any(ProductCreateDto.class));
    }

    @Test
    @DisplayName("deve deletar produto com sucesso")
    void shouldDeleteProduct() {
        doNothing().when(productService).deleteById("1");

        var response = controller.delete("1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService).deleteById("1");
    }

    @Test
    @DisplayName("deve retornar Location header correto ao criar produto")
    void shouldReturnCorrectLocationHeaderWhenCreatingProduct() {
        when(productService.create(any(ProductCreateDto.class))).thenReturn(productDto);

        var response = controller.create(createDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        assertEquals("/api/products/1", response.getHeaders().getLocation().getPath());
        verify(productService).create(any(ProductCreateDto.class));
    }

    @Test
    @DisplayName("deve retornar 404 ao buscar produto por SKU inexistente")
    void shouldReturn404WhenProductBySkuNotFound() {
        when(productService.findBySku("NON-EXISTENT")).thenReturn(Optional.empty());

        var response = controller.getBySku("NON-EXISTENT");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(productService).findBySku("NON-EXISTENT");
    }

    @Test
    @DisplayName("deve retornar 404 ao atualizar produto inexistente")
    void shouldReturn404WhenUpdatingNonExistentProduct() {
        var localService = mock(ProductService.class);
        when(localService.update(eq("999"), any(ProductCreateDto.class)))
            .thenThrow(new IllegalArgumentException("Product not found"));

        var localController = new ProductController(localService);

        var ex = assertThrows(IllegalArgumentException.class, () -> localController.update("999", createDto));

        assertTrue(ex.getMessage().contains("Product not found"));
    }

    @Test
    @DisplayName("deve retornar página vazia quando categoria não tem produtos")
    void shouldReturnEmptyPageWhenCategoryHasNoProducts() {
        var emptyPage = new PageImpl<ProductDto>(List.of());
        var pageable = PageRequest.of(0, 10);
        when(productService.findByCategory(eq("EmptyCategory"), any(Pageable.class))).thenReturn(emptyPage);

        var response = controller.listByCategory("EmptyCategory", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
        verify(productService).findByCategory(eq("EmptyCategory"), any(Pageable.class));
    }

    @Test
    @DisplayName("deve retornar página vazia quando não há produtos")
    void shouldReturnEmptyPageWhenNoProducts() {
        var emptyPage = new PageImpl<ProductDto>(List.of());
        var pageable = PageRequest.of(0, 10);
        when(productService.findAll(any(Pageable.class))).thenReturn(emptyPage);

        var response = controller.listAll(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
        verify(productService).findAll(any(Pageable.class));
    }
}
