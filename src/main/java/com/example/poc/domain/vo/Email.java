package com.example.poc.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    // Simplified email regex (not full RFC 5321 but covers 99% of cases)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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

    private static boolean isValidFormat(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static Email of(String email) {
        return new Email(email);
    }

    public String getDomain() {
        int atIndex = value.indexOf('@');
        return value.substring(atIndex + 1);
    }

    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return value.substring(0, atIndex);
    }

    public boolean belongsToDomain(String domain) {
        return this.getDomain().equalsIgnoreCase(domain);
    }

    @Override
    public String toString() {
        return value;
    }
}
