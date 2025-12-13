package com.example.poc.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing a monetary amount with currency.
 * Immutable and provides comparison operations.
 */
public record Money(BigDecimal amount, Currency currency) {
    
    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
    
    /**
     * Checks if this money amount is greater than another.
     * Both amounts must be in the same currency.
     *
     * @param other the money to compare with
     * @return true if this amount is greater than the other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Adds another money amount to this one.
     * Both amounts must be in the same currency.
     *
     * @param other the money to add
     * @return a new Money instance with the sum
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtracts another money amount from this one.
     * Both amounts must be in the same currency.
     * The result must not be negative.
     *
     * @param other the money to subtract
     * @return a new Money instance with the difference
     * @throws IllegalArgumentException if currencies don't match or result would be negative
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                String.format("Subtraction result cannot be negative: %s - %s",
                    this.amount, other.amount)
            );
        }
        return new Money(result, this.currency);
    }
    
    /**
     * Multiplies this money amount by a factor.
     * The factor must be non-negative to ensure the result is non-negative.
     *
     * @param factor the multiplication factor (must be non-negative)
     * @return a new Money instance with the product
     * @throws IllegalArgumentException if factor is negative
     */
    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "Factor cannot be null");
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Multiplication factor cannot be negative");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }
    
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Cannot operate on different currencies: %s and %s",
                    this.currency.getCurrencyCode(),
                    other.currency.getCurrencyCode())
            );
        }
    }
    
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
}
