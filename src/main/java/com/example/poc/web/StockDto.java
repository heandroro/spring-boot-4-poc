package com.example.poc.web;

import jakarta.validation.constraints.NotNull;

public record StockDto(
        @NotNull(message = "Available must not be null") Integer available,
        @NotNull(message = "Reserved must not be null") Integer reserved,
        @NotNull(message = "Total must not be null") Integer total) {
}

