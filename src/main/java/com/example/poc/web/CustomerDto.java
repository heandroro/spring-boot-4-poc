package com.example.poc.web;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CustomerDto(
        @JsonProperty("id") String id,

        @NotBlank(message = "Name must not be blank") @JsonProperty("name") String name,

        @NotBlank(message = "Email must not be blank") @Email(message = "Email should be valid") @JsonProperty("email") String email,

        @NotBlank(message = "Street must not be blank") @JsonProperty("street") String street,

        @NotBlank(message = "City must not be blank") @JsonProperty("city") String city,

        @JsonProperty("state") String state,

        @NotBlank(message = "Postal code must not be blank") @JsonProperty("postalCode") String postalCode,

        @JsonProperty("country") String country,

        @NotNull(message = "Credit limit must not be null") @Positive(message = "Credit limit must be positive") @JsonProperty("creditLimit") BigDecimal creditLimit,

        @JsonProperty("availableCredit") BigDecimal availableCredit,

        @JsonProperty("status") String status) {
    public static CustomerDto create(String name, String email, String street,
            String city, String state, String postalCode,
            String country, BigDecimal creditLimit) {
        return new CustomerDto(
                null, // ID will be generated
                name,
                email,
                street,
                city,
                state,
                postalCode,
                country,
                creditLimit,
                creditLimit, // Initially available credit = limit
                "ACTIVE");
    }
}
