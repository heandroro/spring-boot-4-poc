package com.example.poc.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money Value Object
 * 
 * Immutable representation of a monetary amount with currency.
 * Ensures type safety and prevents arithmetic errors by encapsulating
 * numeric operations with proper rounding rules.
 * 
 * Domain Rule: Amounts must be non-negative
 * 
 * References:
 * - architecture.md: Value Objects pattern
 * - code-examples.md: Section 22 - Rich Classes
 */
public record Money(BigDecimal amount, String currency) {

    public static final String DEFAULT_CURRENCY = "USD";

    /**
     * Constructor with validation
     */
    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be non-negative, got: " + amount);
        }
        
        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }
    }

    /**
     * Create Money with USD currency
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    /**
     * Create Money from String (useful for calculations)
     */
    public static Money of(String amount) {
        return new Money(new BigDecimal(amount), DEFAULT_CURRENCY);
    }

    /**
     * Add another Money value
     * 
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot add different currencies: " + this.currency + " + " + other.currency
            );
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtract another Money value
     * 
     * @throws IllegalArgumentException if currencies don't match or result is negative
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot subtract different currencies: " + this.currency + " - " + other.currency
            );
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.signum() < 0) {
            throw new IllegalArgumentException("Subtraction would result in negative amount");
        }
        return new Money(result, this.currency);
    }

    /**
     * Multiply by a factor (useful for quantity calculations)
     */
    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    /**
     * Check if amount is zero
     */
    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    /**
     * Check if amount is greater than other
     */
    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare different currencies");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Check if amount is greater than or equal to other
     */
    public boolean isGreaterThanOrEqual(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare different currencies");
        }
        return this.amount.compareTo(other.amount) >= 0;
    }

    /**
     * Format for display: "USD 100.00"
     */
    public String format() {
        return currency + " " + amount.setScale(2, RoundingMode.HALF_UP);
    }
}
