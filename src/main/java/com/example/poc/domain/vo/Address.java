package com.example.poc.domain.vo;

import java.util.Objects;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;

/**
 * Address Value Object
 * 
 * Immutable representation of a physical address.
 * Encapsulates address validation and formatting logic.
 * 
 * Domain Rules:
 * - Street, city, and postal code are required
 * - Country defaults to "United States"
 * 
 * Bean Validation annotations (@NotBlank) provide declarative validation rules
 * that work with Spring's validation framework at API boundaries, simplifying validation.
 * 
 * References:
 * - architecture.md: Value Objects pattern
 * - code-examples.md: Section 22 - Rich Classes
 */
public record Address(
    @NotBlank(message = "Street must not be blank")
    String street,
    
    @NotBlank(message = "City must not be blank")
    String city,
    
    String state,
    
    @NotBlank(message = "Postal code must not be blank")
    String postalCode,
    
    String country
) {

    public static final String DEFAULT_COUNTRY = "United States";

    /**
     * Constructor with validation and normalization
     * Bean Validation annotations provide declarative rules,
     * constructor ensures fail-fast behavior for direct instantiation
     */
    public Address {
        // Validate required fields (null checks using Objects for consistency)
        Objects.requireNonNull(street, "Street must not be null");
        Objects.requireNonNull(city, "City must not be null");
        Objects.requireNonNull(postalCode, "Postal code must not be null");
        
        // Trim whitespace
        street = street.trim();
        city = city.trim();
        state = StringUtils.isBlank(state) ? null : state.trim();
        postalCode = postalCode.trim();
        country = StringUtils.isBlank(country) ? DEFAULT_COUNTRY : country.trim();
        
        // Validate not blank (simplified with Bean Validation annotations)
        if (street.isBlank()) {
            throw new IllegalArgumentException("Street must not be blank");
        }
        if (city.isBlank()) {
            throw new IllegalArgumentException("City must not be blank");
        }
        if (postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code must not be blank");
        }
    }

    /**
     * Create Address with default country
     */
    public static Address of(String street, String city, String state, String postalCode) {
        return new Address(street, city, state, postalCode, DEFAULT_COUNTRY);
    }

    /**
     * Format for display
     * Example: "123 Main St, Springfield, IL 62701, United States"
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(street).append(", ").append(city);
        
        if (!StringUtils.isBlank(state)) {
            sb.append(", ").append(state);
        }
        
        sb.append(" ").append(postalCode).append(", ").append(country);
        
        return sb.toString();
    }

    /**
     * Check if address is in a specific country
     */
    public boolean isInCountry(String targetCountry) {
        return this.country.equalsIgnoreCase(targetCountry);
    }
}
