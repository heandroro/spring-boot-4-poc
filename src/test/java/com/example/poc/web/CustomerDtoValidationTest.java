package com.example.poc.web;

import static com.example.poc.support.CustomerTestFixtures.createValidCustomerDto;
import static com.example.poc.support.CustomerTestFixtures.generatePositiveMoney;
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

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("creditLimit")));
    }

    @Test
    @DisplayName("should pass validation for valid payload")
    void shouldPassValidationForValidPayload() {
        CustomerDto valid = createValidCustomerDto();

        Set<ConstraintViolation<CustomerDto>> violations = validator.validate(valid);

        assertTrue(violations.isEmpty());
    }
}
