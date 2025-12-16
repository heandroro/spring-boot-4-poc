package com.example.poc.domain.vo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for Email Value Object
 * 
 * Tests focus on:
 * - Email format validation
 * - Immutability
 * - Case normalization (lowercase)
 * - Domain extraction
 * 
 * References: testing.md, security.md - Input Validation
 */
@DisplayName("Email Value Object Tests")
class EmailTest {

    @Test
    @DisplayName("should create Email with valid format")
    void testCreateValidEmail() {
        // When
        Email email = new Email("user@example.com");

        // Then
        assertEquals("user@example.com", email.value());
    }

    @Test
    @DisplayName("should normalize email to lowercase")
    void testEmailNormalizedToLowercase() {
        // When
        Email email = new Email("User@Example.COM");

        // Then
        assertEquals("user@example.com", email.value());
    }

    @Test
    @DisplayName("should trim whitespace from email")
    void testEmailTrimmed() {
        // When
        Email email = new Email("  user@example.com  ");

        // Then
        assertEquals("user@example.com", email.value());
    }

    @Test
    @DisplayName("should reject null email")
    void testRejectNullEmail() {
        // Expect
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    @DisplayName("should reject blank email")
    void testRejectBlankEmail() {
        // Expect
        assertThrows(IllegalArgumentException.class, () -> new Email("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "invalid@",
            "@example.com",
            "user@.com",
            "user@example",
            "user name@example.com",
            "user@exam ple.com"
    })
    @DisplayName("should reject invalid email formats")
    void testRejectInvalidFormats(String invalidEmail) {
        // Expect
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.co.uk",
            "user+tag@example.org",
            "123@example.com",
            "user_name@example.com"
    })
    @DisplayName("should accept valid email formats")
    void testAcceptValidFormats(String validEmail) {
        // Expect no exception
        assertDoesNotThrow(() -> new Email(validEmail));
    }

    @Test
    @DisplayName("should extract domain from email")
    void testGetDomain() {
        // Given
        Email email = new Email("user@example.com");

        // When
        String domain = email.getDomain();

        // Then
        assertEquals("example.com", domain);
    }

    @Test
    @DisplayName("should extract local part from email")
    void testGetLocalPart() {
        // Given
        Email email = new Email("user.name@example.com");

        // When
        String localPart = email.getLocalPart();

        // Then
        assertEquals("user.name", localPart);
    }

    @Test
    @DisplayName("should check if email belongs to domain")
    void testBelongsToDomain() {
        // Given
        Email email = new Email("user@example.com");

        // Then
        assertTrue(email.belongsToDomain("example.com"));
        assertTrue(email.belongsToDomain("EXAMPLE.COM")); // Case insensitive
        assertFalse(email.belongsToDomain("other.com"));
    }

    @Test
    @DisplayName("of factory creates Email and normalizes")
    void testOfFactory() {
        Email email = Email.of("User@Example.COM");
        assertEquals("user@example.com", email.value());
    }

    @Test
    @DisplayName("should return email value as string")
    void testToString() {
        // Given
        Email email = new Email("user@example.com");

        // When
        String emailStr = email.toString();

        // Then
        assertEquals("user@example.com", emailStr);
    }

    @Test
    @DisplayName("should be immutable")
    void testImmutability() {
        // Given
        Email email = new Email("user@example.com");

        // When - try to get value (should be final/immutable)
        String value = email.value();

        // Then - verify email content hasn't changed
        assertEquals("user@example.com", value);
        assertEquals("user@example.com", email.toString());
    }
}
