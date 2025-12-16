package com.example.poc.domain.vo;

import org.apache.commons.lang3.StringUtils;

import jakarta.validation.constraints.NotBlank;

public record Address(
        @NotBlank(message = "Street must not be blank") String street,

        @NotBlank(message = "City must not be blank") String city,

        String state,

        @NotBlank(message = "Postal code must not be blank") String postalCode,

        String country) {

    public static final String DEFAULT_COUNTRY = "United States";

    public Address {
        // Normalize: trim whitespace and apply defaults
        street = street != null ? street.trim() : null;
        city = city != null ? city.trim() : null;
        state = StringUtils.isBlank(state) ? null : state.trim();
        postalCode = postalCode != null ? postalCode.trim() : null;
        country = StringUtils.isBlank(country) ? DEFAULT_COUNTRY : country.trim();
    }

    public static Address of(String street, String city, String state, String postalCode) {
        return new Address(street, city, state, postalCode, DEFAULT_COUNTRY);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(street).append(", ").append(city);

        if (!StringUtils.isBlank(state)) {
            sb.append(", ").append(state);
        }

        sb.append(" ").append(postalCode).append(", ").append(country);

        return sb.toString();
    }

    public boolean isInCountry(String targetCountry) {
        return this.country.equalsIgnoreCase(targetCountry);
    }
}
