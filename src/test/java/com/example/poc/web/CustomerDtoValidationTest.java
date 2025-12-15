package com.example.poc.web;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.github.javafaker.Faker;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@DisplayName("CustomerDto Validation Tests")
class CustomerDtoValidationTest {

    private Validator validator;
    private Faker faker;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.afterPropertiesSet();
        validator = factoryBean.getValidator();
        faker = new Faker();
    }

    @Test
    @DisplayName("should create DTO via factory with defaults")
    void shouldCreateDtoViaFactoryWithDefaults() {
        BigDecimal limit = new BigDecimal("500.00");

        CustomerDto dto = CustomerDto.create(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                faker.address().streetAddress(),
                faker.address().city(),
                faker.address().stateAbbr(),
                faker.address().zipCode(),
                "BR",
                limit);

        assertEquals(limit, dto.creditLimit());
        assertEquals(limit, dto.availableCredit());
        assertEquals("ACTIVE", dto.status());
    }

    @Test
    @DisplayName("should fail validation when mandatory fields are invalid")
    void shouldFailValidationWhenMandatoryFieldsInvalid() {
        CustomerDto invalid = Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::name), " ")
                .set(field(CustomerDto::email), "not-an-email")
                .set(field(CustomerDto::street), " ")
                .set(field(CustomerDto::city), " ")
                .set(field(CustomerDto::postalCode), " ")
                .set(field(CustomerDto::creditLimit), null)
                .create();

        Set<ConstraintViolation<CustomerDto>> violations = validator.validate(invalid);

        // Assert exact number of violations (6 fields invalid)
        assertEquals(6, violations.size(), "Should have exactly 6 validation violations");
        
        // Verify specific field violations exist
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")), 
                "Should have violation for name field");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")), 
                "Should have violation for email field");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("street")), 
                "Should have violation for street field");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("city")), 
                "Should have violation for city field");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("postalCode")), 
                "Should have violation for postalCode field");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("creditLimit")), 
                "Should have violation for creditLimit field");
        
        // Verify specific error messages
        ConstraintViolation<CustomerDto> nameViolation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("name"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected name violation not found"));
        assertEquals("Name must not be blank", nameViolation.getMessage(), 
                "Name violation should have correct message");
        
        ConstraintViolation<CustomerDto> emailViolation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("email"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected email violation not found"));
        assertEquals("Email should be valid", emailViolation.getMessage(), 
                "Email violation should have correct message");
        
        ConstraintViolation<CustomerDto> creditLimitViolation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("creditLimit"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected creditLimit violation not found"));
        assertEquals("Credit limit must not be null", creditLimitViolation.getMessage(), 
                "CreditLimit violation should have correct message");
    }

    @Test
    @DisplayName("should pass validation for valid payload")
    void shouldPassValidationForValidPayload() {
        BigDecimal limit = generatePositiveMoney();
        CustomerDto valid = Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::name), faker.name().fullName())
                .set(field(CustomerDto::email), faker.internet().emailAddress())
                .set(field(CustomerDto::street), faker.address().streetAddress())
                .set(field(CustomerDto::city), faker.address().city())
                .set(field(CustomerDto::postalCode), faker.address().zipCode())
                .set(field(CustomerDto::country), "BR")
                .set(field(CustomerDto::creditLimit), limit)
                .set(field(CustomerDto::availableCredit), limit)
                .set(field(CustomerDto::status), "ACTIVE")
                .create();

        Set<ConstraintViolation<CustomerDto>> violations = validator.validate(valid);

        assertTrue(violations.isEmpty());
    }

    private BigDecimal generatePositiveMoney() {
        return Instancio.of(BigDecimal.class)
                .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
                        .min(BigDecimal.ONE)
                        .max(new BigDecimal("10000.00")))
                .create();
    }
}
