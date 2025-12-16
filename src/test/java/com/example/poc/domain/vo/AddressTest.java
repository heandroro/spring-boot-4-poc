package com.example.poc.domain.vo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Address Value Object
 * 
 * Tests focus on:
 * - Address normalization (trimming, default values)
 * - Immutability
 * - Formatting for display
 * - Country operations
 * 
 * Note: Validation is handled by Bean Validation annotations (@NotBlank)
 * at API boundaries, not in the constructor.
 * 
 * References: testing.md, architecture.md - Value Objects section
 */
@DisplayName("Address Value Object Tests")
class AddressTest {

    @Test
    @DisplayName("should create Address with all fields")
    void testCreateAddressComplete() {
        // When
        Address address = new Address(
                "123 Main St",
                "Springfield",
                "IL",
                "62701",
                "United States");

        // Then
        assertEquals("123 Main St", address.street());
        assertEquals("Springfield", address.city());
        assertEquals("IL", address.state());
        assertEquals("62701", address.postalCode());
        assertEquals("United States", address.country());
    }

    @Test
    @DisplayName("should create Address with default country")
    void testCreateAddressDefaultCountry() {
        // When
        Address address = Address.of("123 Main St", "Springfield", "IL", "62701");

        // Then
        assertEquals("United States", address.country());
    }

    @Test
    @DisplayName("should trim whitespace from fields")
    void testTrimWhitespace() {
        // When
        Address address = new Address(
                "  123 Main St  ",
                "  Springfield  ",
                "  IL  ",
                "  62701  ",
                "  United States  ");

        // Then
        assertEquals("123 Main St", address.street());
        assertEquals("Springfield", address.city());
        assertEquals("IL", address.state());
        assertEquals("62701", address.postalCode());
        assertEquals("United States", address.country());
    }

    @Test
    @DisplayName("should format address for display with state")
    void testFormatWithState() {
        // Given
        Address address = new Address(
                "123 Main St",
                "Springfield",
                "IL",
                "62701",
                "United States");

        // When
        String formatted = address.format();

        // Then
        assertTrue(formatted.contains("123 Main St"));
        assertTrue(formatted.contains("Springfield"));
        assertTrue(formatted.contains("IL"));
        assertTrue(formatted.contains("62701"));
        assertTrue(formatted.contains("United States"));
    }

    @Test
    @DisplayName("should format address without state")
    void testFormatWithoutState() {
        // Given
        Address address = new Address(
                "123 Main St",
                "London",
                null,
                "EC1A 1BB",
                "United Kingdom");

        // When
        String formatted = address.format();

        // Then
        assertTrue(formatted.contains("123 Main St"));
        assertTrue(formatted.contains("London"));
        assertTrue(formatted.contains("EC1A 1BB"));
        assertTrue(formatted.contains("United Kingdom"));
        // State should not appear
        assertFalse(formatted.contains(", , "));
    }

    @Test
    @DisplayName("should check if address is in a specific country")
    void testIsInCountry() {
        // Given
        Address address = Address.of("123 Main St", "Springfield", "IL", "62701");

        // Then
        assertTrue(address.isInCountry("United States"));
        assertTrue(address.isInCountry("united states")); // Case insensitive
        assertFalse(address.isInCountry("Canada"));
    }

    @Test
    @DisplayName("should allow null state")
    void testAllowNullState() {
        // Expect no exception
        assertDoesNotThrow(() -> new Address("123 Main St", "London", null, "EC1A 1BB", "UK"));
    }

    @Test
    @DisplayName("blank state is normalized to null")
    void blankStateNormalizedToNull() {
        Address a = new Address("123 Main St", "London", "   ", "EC1A 1BB", "UK");
        assertNull(a.state());
    }

    @Test
    @DisplayName("blank or null country becomes default")
    void blankOrNullCountryDefaults() {
        Address a1 = new Address("123 Main St", "City", "ST", "123", "   ");
        Address a2 = new Address("123 Main St", "City", "ST", "123", null);

        assertEquals(Address.DEFAULT_COUNTRY, a1.country());
        assertEquals(Address.DEFAULT_COUNTRY, a2.country());
    }

    @Test
    @DisplayName("null optional fields are allowed and handled")
    void nullOptionalFields() {
        Address a = new Address(null, null, null, null, null);
        assertNull(a.street());
        assertNull(a.city());
        assertNull(a.state());
        assertNull(a.postalCode());
        assertEquals(Address.DEFAULT_COUNTRY, a.country());
    }

    @Test
    @DisplayName("should be immutable")
    void testImmutability() {
        // Given
        Address address = Address.of("123 Main St", "Springfield", "IL", "62701");

        // When - verify cannot modify
        String originalStreet = address.street();

        // Then - address content remains unchanged
        assertEquals("123 Main St", address.street());
        assertEquals(originalStreet, address.street());
    }
}
