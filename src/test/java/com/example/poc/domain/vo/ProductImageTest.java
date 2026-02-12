package com.example.poc.domain.vo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductImage Value Object")
class ProductImageTest {

    @Test
    @DisplayName("deve criar imagem primária")
    void shouldCreatePrimaryImage() {
        var image = ProductImage.primary("http://test.com/img.jpg", "Test Image");

        assertEquals("http://test.com/img.jpg", image.url());
        assertEquals("Test Image", image.alt());
        assertTrue(image.isPrimary());
    }

    @Test
    @DisplayName("deve criar imagem secundária")
    void shouldCreateSecondaryImage() {
        var image = ProductImage.secondary("http://test.com/img2.jpg", "Secondary Image");

        assertEquals("http://test.com/img2.jpg", image.url());
        assertEquals("Secondary Image", image.alt());
        assertFalse(image.isPrimary());
    }

    @Test
    @DisplayName("deve lançar exceção ao criar imagem com URL null")
    void shouldThrowExceptionWithNullUrl() {
        assertThrows(NullPointerException.class, () -> ProductImage.primary(null, "Alt"));
    }

    @Test
    @DisplayName("deve lançar exceção ao criar imagem com URL vazia")
    void shouldThrowExceptionWithBlankUrl() {
        assertThrows(IllegalArgumentException.class, () -> ProductImage.primary("  ", "Alt"));
    }

    @Test
    @DisplayName("deve lançar exceção ao criar imagem com altText vazio")
    void shouldThrowExceptionWithBlankAltText() {
        assertThrows(IllegalArgumentException.class, () -> ProductImage.primary("http://test.com", "  "));
    }

    @Test
    @DisplayName("deve validar igualdade de imagens")
    void shouldValidateImageEquality() {
        var image1 = ProductImage.primary("http://test.com/img.jpg", "Test");
        var image2 = ProductImage.primary("http://test.com/img.jpg", "Test");
        var image3 = ProductImage.secondary("http://test.com/img.jpg", "Test");

        assertEquals(image1, image2);
        assertNotEquals(image1, image3);
    }

    @Test
    @DisplayName("deve validar hashCode de imagens")
    void shouldValidateImageHashCode() {
        var image1 = ProductImage.primary("http://test.com/img.jpg", "Test");
        var image2 = ProductImage.primary("http://test.com/img.jpg", "Test");

        assertEquals(image1.hashCode(), image2.hashCode());
    }

    @Test
    @DisplayName("deve retornar string representation")
    void shouldReturnStringRepresentation() {
        var image = ProductImage.primary("http://test.com/img.jpg", "Test");
        var str = image.toString();

        assertNotNull(str);
        assertTrue(str.contains("http://test.com/img.jpg"));
    }
}
