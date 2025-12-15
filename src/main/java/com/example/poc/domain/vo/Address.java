package com.example.poc.domain.vo;

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
     * Constructor with normalization
     * Bean Validation annotations (@NotBlank) handle validation at API boundaries.
     * Constructor normalizes data (trim whitespace, apply defaults).
     */
    public Address {
        // Normalize: trim whitespace and apply defaults
        street = street != null ? street.trim() : null;
        city = city != null ? city.trim() : null;
        state = StringUtils.isBlank(state) ? null : state.trim();
        postalCode = postalCode != null ? postalCode.trim() : null;
        country = StringUtils.isBlank(country) ? DEFAULT_COUNTRY : country.trim();
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
