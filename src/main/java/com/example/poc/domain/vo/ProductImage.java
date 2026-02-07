package com.example.poc.domain.vo;

import java.util.Objects;

public record ProductImage(
        String url,
        String alt,
        Boolean isPrimary) {

    public ProductImage {
        Objects.requireNonNull(url, "URL must not be null");
        if (url.isBlank()) {
            throw new IllegalArgumentException("URL must not be blank");
        }

        if (alt != null && alt.isBlank()) {
            throw new IllegalArgumentException("Alt text must not be blank if provided");
        }

        Objects.requireNonNull(isPrimary, "isPrimary must not be null");
    }

    public static ProductImage primary(String url, String alt) {
        return new ProductImage(url, alt, true);
    }

    public static ProductImage secondary(String url, String alt) {
        return new ProductImage(url, alt, false);
    }
}

