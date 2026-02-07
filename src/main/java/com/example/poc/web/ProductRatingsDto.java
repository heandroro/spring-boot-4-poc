package com.example.poc.web;

import jakarta.validation.constraints.NotNull;

public record ProductRatingsDto(
        @NotNull(message = "Average must not be null") Double average,
        @NotNull(message = "Count must not be null") Integer count) {
}

