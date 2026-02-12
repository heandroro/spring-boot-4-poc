package com.example.poc.web;

import java.util.List;
import java.util.Map;

import com.example.poc.domain.Product.Status;
import com.example.poc.domain.vo.ProductImage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductDto(
        @NotBlank(message = "ID must not be blank") String id,

        @NotBlank(message = "SKU must not be blank") String sku,

        @NotBlank(message = "Name must not be blank") String name,

        String description,

        @NotBlank(message = "Category must not be blank") String category,

        @NotNull(message = "Price must not be null") String price,

        String currency,

        @NotNull(message = "Stock must not be null") StockDto stock,

        Map<String, Object> specifications,

        List<ProductImage> images,

        @NotNull(message = "Ratings must not be null") ProductRatingsDto ratings,

        @NotNull(message = "Status must not be null") Status status,

        @NotNull(message = "Created at must not be null") String createdAt,

        @NotNull(message = "Updated at must not be null") String updatedAt) {

    public ProductDto {
        specifications = specifications == null ? Map.of() : Map.copyOf(specifications);
        images = images == null ? List.of() : List.copyOf(images);
    }
}
