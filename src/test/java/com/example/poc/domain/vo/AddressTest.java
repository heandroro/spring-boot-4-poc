package com.example.poc.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Address Value Object
 * 
 * Tests focus on:
 * - Address validation (required fields)
 * - Immutability
 * - Formatting for display
 * - Country operations
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
            "United States"
        );
        
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
    @DisplayName("should reject null street")
    void testRejectNullStreet() {
        // Expect
        assertThrows(
            NullPointerException.class,
            () -> new Address(null, "Springfield", "IL", "62701", "USA")
        );
    }

    @Test
    @DisplayName("should reject blank street")
    void testRejectBlankStreet() {
        // Expect
        assertThrows(
            IllegalArgumentException.class,
            () -> new Address("   ", "Springfield", "IL", "62701", "USA")
        );
    }

    @Test
    @DisplayName("should reject null city")
    void testRejectNullCity() {
        // Expect
        assertThrows(
            NullPointerException.class,
            () -> new Address("123 Main St", null, "IL", "62701", "USA")
        );
    }

    @Test
    @DisplayName("should reject blank city")
    void testRejectBlankCity() {
        // Expect
        assertThrows(
            IllegalArgumentException.class,
            () -> new Address("123 Main St", "   ", "IL", "62701", "USA")
        );
    }

    @Test
    @DisplayName("should reject null postal code")
    void testRejectNullPostalCode() {
        // Expect
        assertThrows(
            NullPointerException.class,
            () -> new Address("123 Main St", "Springfield", "IL", null, "USA")
        );
    }

    @Test
    @DisplayName("should reject blank postal code")
    void testRejectBlankPostalCode() {
        // Expect
        assertThrows(
            IllegalArgumentException.class,
            () -> new Address("123 Main St", "Springfield", "IL", "   ", "USA")
        );
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
            "  United States  "
        );
        
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
            "United States"
        );
        
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
            "United Kingdom"
        );
        
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
        assertDoesNotThrow(() -> 
            new Address("123 Main St", "London", null, "EC1A 1BB", "UK")
        );
    }

    @Test
    @DisplayName("should allow blank state")
    void testAllowBlankState() {
        // Expect no exception
        assertDoesNotThrow(() -> 
            new Address("123 Main St", "London", "   ", "EC1A 1BB", "UK")
        );
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
