package com.example.poc.domain.vo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for Money Value Object
 * 
 * Tests focus on:
 * - Immutability
 * - Arithmetic operations with validation
 * - Business rules enforcement
 * - Edge cases (zero, negative values)
 * 
 * References: testing.md, architecture.md - Value Objects section
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("should create Money with valid amount and currency")
    void testCreateMoney() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";

        // When
        Money money = new Money(amount, currency);

        // Then
        assertEquals(amount, money.amount());
        assertEquals(currency, money.currency());
    }

    @Test
    @DisplayName("should create Money with USD default currency")
    void testCreateMoneyDefaultCurrency() {
        // When
        Money money = Money.of(new BigDecimal("50.00"));

        // Then
        assertEquals("USD", money.currency());
    }

    @Test
    @DisplayName("should create Money from String")
    void testCreateMoneyFromString() {
        // When
        Money money = Money.of("123.45");

        // Then
        assertEquals(new BigDecimal("123.45"), money.amount());
        assertEquals("USD", money.currency());
    }

    @Test
    @DisplayName("should reject null amount")
    void testRejectNullAmount() {
        // Expect
        assertThrows(NullPointerException.class, () -> new Money(null, "USD"));
    }

    @Test
    @DisplayName("should reject null currency")
    void testRejectNullCurrency() {
        // Expect
        assertThrows(NullPointerException.class, () -> new Money(BigDecimal.TEN, null));
    }

    @Test
    @DisplayName("should reject negative amount")
    void testRejectNegativeAmount() {
        // Expect
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Money(new BigDecimal("-10.00"), "USD"));
        assertTrue(exception.getMessage().contains("non-negative"));
    }

    @Test
    @DisplayName("should reject blank currency")
    void testRejectBlankCurrency() {
        // Expect
        assertThrows(
                IllegalArgumentException.class,
                () -> new Money(BigDecimal.TEN, "   "));
    }

    @Test
    @DisplayName("should add two Money values with same currency")
    void testAddMoney() {
        // Given
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("50.00"), "USD");

        // When
        Money result = money1.add(money2);

        // Then
        assertEquals(new BigDecimal("150.00"), result.amount());
        assertEquals("USD", result.currency());
    }

    @Test
    @DisplayName("should reject adding Money with different currencies")
    void testAddMoneyDifferentCurrencies() {
        // Given
        Money usd = new Money(new BigDecimal("100.00"), "USD");
        Money eur = new Money(new BigDecimal("100.00"), "EUR");

        // Expect
        assertThrows(IllegalArgumentException.class, () -> usd.add(eur));
    }

    @Test
    @DisplayName("should subtract two Money values with same currency")
    void testSubtractMoney() {
        // Given
        Money money1 = new Money(new BigDecimal("100.00"), "USD");
        Money money2 = new Money(new BigDecimal("30.00"), "USD");

        // When
        Money result = money1.subtract(money2);

        // Then
        assertEquals(new BigDecimal("70.00"), result.amount());
    }

    @Test
    @DisplayName("should reject subtraction resulting in negative amount")
    void testSubtractMoneyResultsInNegative() {
        // Given
        Money money1 = new Money(new BigDecimal("30.00"), "USD");
        Money money2 = new Money(new BigDecimal("50.00"), "USD");

        // Expect
        assertThrows(IllegalArgumentException.class, () -> money1.subtract(money2));
    }

    @Test
    @DisplayName("should multiply Money by positive quantity")
    void testMultiplyMoney() {
        // Given
        Money money = new Money(new BigDecimal("10.00"), "USD");

        // When
        Money result = money.multiply(5);

        // Then
        assertEquals(new BigDecimal("50.00"), result.amount());
    }

    @Test
    @DisplayName("should multiply Money by zero")
    void testMultiplyMoneyByZero() {
        // Given
        Money money = new Money(new BigDecimal("10.00"), "USD");

        // When
        Money result = money.multiply(0);

        // Then
        assertTrue(result.isZero());
    }

    @Test
    @DisplayName("should reject multiplication by negative quantity")
    void testMultiplyMoneyByNegativeQuantity() {
        // Given
        Money money = new Money(new BigDecimal("10.00"), "USD");

        // Expect
        assertThrows(IllegalArgumentException.class, () -> money.multiply(-5));
    }

    @Test
    @DisplayName("should identify zero amount")
    void testIsZero() {
        // Given
        Money zero = new Money(BigDecimal.ZERO, "USD");
        Money nonZero = new Money(new BigDecimal("0.01"), "USD");

        // Then
        assertTrue(zero.isZero());
        assertFalse(nonZero.isZero());
    }

    @Test
    @DisplayName("should compare Money values")
    void testCompareGreaterThan() {
        // Given
        Money greater = new Money(new BigDecimal("100.00"), "USD");
        Money lesser = new Money(new BigDecimal("50.00"), "USD");

        // Then
        assertTrue(greater.isGreaterThan(lesser));
        assertFalse(lesser.isGreaterThan(greater));
    }

    @Test
    @DisplayName("should reject comparison of different currencies")
    void testCompareGreaterThanDifferentCurrencies() {
        // Given
        Money usd = new Money(new BigDecimal("100.00"), "USD");
        Money eur = new Money(new BigDecimal("50.00"), "EUR");

        // Expect
        assertThrows(IllegalArgumentException.class, () -> usd.isGreaterThan(eur));
    }

    @Test
    @DisplayName("should format Money for display")
    void testFormat() {
        // Given
        Money money = new Money(new BigDecimal("100.50"), "EUR");

        // When
        String formatted = money.format();

        // Then
        assertTrue(formatted.startsWith("EUR"));
        assertTrue(formatted.contains("100.5"));
    }

    @Test
    @DisplayName("should reject subtraction with different currencies")
    void testSubtractDifferentCurrencies() {
        // Given
        Money usd = new Money(new BigDecimal("100.00"), "USD");
        Money eur = new Money(new BigDecimal("50.00"), "EUR");

        // Expect
        assertThrows(IllegalArgumentException.class, () -> usd.subtract(eur));
    }

    @Test
    @DisplayName("should evaluate isGreaterThanOrEqual correctly and reject different currencies")
    void testIsGreaterThanOrEqual() {
        // Given
        Money a = new Money(new BigDecimal("100.00"), "USD");
        Money b = new Money(new BigDecimal("100.00"), "USD");
        Money c = new Money(new BigDecimal("50.00"), "USD");

        // Then
        assertTrue(a.isGreaterThanOrEqual(b));
        assertTrue(a.isGreaterThanOrEqual(c));

        Money eur = new Money(new BigDecimal("100.00"), "EUR");
        assertThrows(IllegalArgumentException.class, () -> a.isGreaterThanOrEqual(eur));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0.00", "0.01", "1000000.00", "0.001" })
    @DisplayName("should accept various valid amounts")
    void testAcceptValidAmounts(String amountStr) {
        // Expect no exception
        assertDoesNotThrow(() -> Money.of(amountStr));
    }
}
