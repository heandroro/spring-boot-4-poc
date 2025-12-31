package com.example.poc.support;

import static org.instancio.Select.all;
import static org.instancio.Select.field;

import java.math.BigDecimal;

import org.instancio.Instancio;

import com.example.poc.domain.Customer;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.example.poc.web.CustomerDto;
import com.github.javafaker.Faker;

public final class CustomerTestFixtures {

    private static final Faker faker = new Faker();

    private CustomerTestFixtures() {
    }

    public static BigDecimal generatePositiveMoney() {
        return Instancio.of(BigDecimal.class)
                .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
                        .min(BigDecimal.ONE)
                        .max(new BigDecimal("10000.00")))
                .create();
    }

    public static BigDecimal generatePositiveMoneyInRange(BigDecimal min, BigDecimal max) {
        return Instancio.of(BigDecimal.class)
                .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
                        .min(min)
                        .max(max))
                .create();
    }

    public static CustomerDto createValidCustomerDto() {
        BigDecimal creditLimit = generatePositiveMoney();
        return Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::name), faker.name().fullName())
                .set(field(CustomerDto::email), faker.internet().emailAddress())
                .set(field(CustomerDto::street), faker.address().streetAddress())
                .set(field(CustomerDto::city), faker.address().city())
                .set(field(CustomerDto::state), faker.address().stateAbbr())
                .set(field(CustomerDto::postalCode), faker.address().zipCode())
                .set(field(CustomerDto::country), "BR")
                .set(field(CustomerDto::creditLimit), creditLimit)
                .set(field(CustomerDto::availableCredit), creditLimit)
                .set(field(CustomerDto::status), "ACTIVE")
                .create();
    }

    public static Address createValidAddress() {
        return Address.of(
                faker.address().streetAddress(),
                faker.address().city(),
                faker.address().stateAbbr(),
                faker.address().zipCode());
    }

    public static Customer createValidCustomer() {
        return Customer.create(
                faker.name().fullName(),
                new Email(faker.internet().emailAddress()),
                createValidAddress(),
                Money.of(generatePositiveMoney()));
    }

    public static Customer createCustomerWithLimit(BigDecimal limit) {
        return Customer.create(
                faker.name().fullName(),
                new Email(faker.internet().emailAddress()),
                createValidAddress(),
                Money.of(limit));
    }
}
