package com.example.poc.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email Value Object
 * 
 * Immutable representation of an email address.
 * Handles email validation following RFC 5321 basic rules.
 * 
 * Domain Rules:
 * - Must be valid email format
 * - Must not be blank
 * 
 * References:
 * - security.md: Input Validation section
 * - code-examples.md: Section 22 - Rich Classes
 */
public record Email(String value) {

    // Simplified email regex (not full RFC 5321 but covers 99% of cases)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Constructor with validation
     */
    public Email {
        Objects.requireNonNull(value, "Email must not be null");

        String trimmed = value.trim();

        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        if (!isValidFormat(trimmed)) {
            throw new IllegalArgumentException("Invalid email format: " + trimmed);
        }

        value = trimmed.toLowerCase();
    }

    /**
     * Check if email format is valid
     */
    private static boolean isValidFormat(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Create Email instance
     */
    public static Email of(String email) {
        return new Email(email);
    }

    /**
     * Extract domain from email
     * Example: "user@example.com" -> "example.com"
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return value.substring(atIndex + 1);
    }

    /**
     * Extract local part (username) from email
     * Example: "user@example.com" -> "user"
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return value.substring(0, atIndex);
    }

    /**
     * Check if email belongs to a specific domain
     */
    public boolean belongsToDomain(String domain) {
        return this.getDomain().equalsIgnoreCase(domain);
    }

    /**
     * Return the email value
     */
    @Override
    public String toString() {
        return value;
    }
}
