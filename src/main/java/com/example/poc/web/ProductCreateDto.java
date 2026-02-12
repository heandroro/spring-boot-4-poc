package com.example.poc.web;

import java.util.List;
import java.util.Map;

import com.example.poc.domain.vo.ProductImage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductCreateDto(
        @NotBlank(message = "SKU must not be blank") String sku,

        @NotBlank(message = "Name must not be blank") String name,

        String description,

        @NotBlank(message = "Category must not be blank") String category,

        @NotNull(message = "Price must not be null") String price,

        String currency,

        @NotNull(message = "Initial stock must not be null") @Positive(message = "Initial stock must be positive") Integer initialStock,

        Map<String, Object> specifications,

        List<ProductImage> images) {

    public ProductCreateDto {
        specifications = specifications == null ? Map.of() : Map.copyOf(specifications);
        images = images == null ? List.of() : List.copyOf(images);
    }
}
