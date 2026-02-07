package com.example.poc.domain.vo;

import java.util.Objects;

public record Stock(
        Integer available,
        Integer reserved,
        Integer total) {

    public Stock {
        Objects.requireNonNull(available, "Available must not be null");
        Objects.requireNonNull(reserved, "Reserved must not be null");
        Objects.requireNonNull(total, "Total must not be null");

        if (available < 0) {
            throw new IllegalArgumentException("Available must be non-negative, got: " + available);
        }

        if (reserved < 0) {
            throw new IllegalArgumentException("Reserved must be non-negative, got: " + reserved);
        }

        if (total < 0) {
            throw new IllegalArgumentException("Total must be non-negative, got: " + total);
        }

        if (available + reserved != total) {
            throw new IllegalArgumentException(
                    "Stock invariant violated: available (" + available + ") + reserved (" +
                            reserved + ") must equal total (" + total + ")");
        }
    }

    public static Stock initial(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        return new Stock(quantity, 0, quantity);
    }

    public static Stock empty() {
        return new Stock(0, 0, 0);
    }

    public Stock reserve(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity to reserve must be non-negative");
        }

        if (quantity > available) {
            throw new IllegalArgumentException(
                    "Cannot reserve " + quantity + " units. Available: " + available);
        }

        return new Stock(
                available - quantity,
                reserved + quantity,
                total);
    }

    public Stock confirmReservation(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        if (quantity > reserved) {
            throw new IllegalArgumentException(
                    "Cannot confirm " + quantity + " units. Reserved: " + reserved);
        }

        return new Stock(
                available,
                reserved - quantity,
                total - quantity);
    }

    public Stock cancelReservation(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        if (quantity > reserved) {
            throw new IllegalArgumentException(
                    "Cannot cancel " + quantity + " units. Reserved: " + reserved);
        }

        return new Stock(
                available + quantity,
                reserved - quantity,
                total);
    }

    public Stock replenish(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }

        return new Stock(
                available + quantity,
                reserved,
                total + quantity);
    }

    public boolean hasAvailable(Integer quantity) {
        return quantity >= 0 && quantity <= available;
    }

    public boolean isOutOfStock() {
        return available == 0 && reserved == 0;
    }

    public boolean isLowStock(Integer threshold) {
        return available <= threshold;
    }
}

