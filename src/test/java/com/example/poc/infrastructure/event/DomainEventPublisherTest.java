package com.example.poc.infrastructure.event;

import static org.instancio.Select.all;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.poc.domain.Customer;
import com.example.poc.domain.event.DomainEvent;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.github.javafaker.Faker;

/**
 * Integration tests for Domain Event Publishing
 * 
 * Tests that events can be published without errors.
 * Event listeners are tested separately in CustomerEventHandlerTest.
 * 
 * Uses JavaFaker with Instancio for realistic test data generation:
 * - JavaFaker generates realistic names, addresses, emails
 * - Instancio handles object creation and field constraints
 * 
 * References:
 * - architecture.md: Event-Driven Architecture
 * - code-examples.md: Section 17 - Domain Events
 */
@SpringBootTest
@DisplayName("Domain Event Publisher Integration Tests")
class DomainEventPublisherTest {

    @Autowired
    private DomainEventPublisher eventPublisher;
    
    private Faker faker;
    
    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    @DisplayName("should publish single domain event without errors")
    void testPublishSingleEvent() {
        // Given - Using JavaFaker with Instancio to create realistic test data
        String name = faker.name().fullName();
        Email email = new Email(faker.internet().emailAddress());
        Address address = Address.of(
                faker.address().streetAddress(),
                faker.address().city(),
                faker.address().stateAbbr(),
                faker.address().zipCode()
        );
        Money creditLimit = Money.of(Instancio.of(BigDecimal.class)
                .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal().min(BigDecimal.ZERO).max(new BigDecimal("10000.00")))
                .create());
        
        Customer customer = Customer.create(name, email, address, creditLimit);
        
        List<DomainEvent> events = customer.pullEvents();
        DomainEvent event = events.get(0);
        
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> eventPublisher.publish(event));
    }

    @Test
    @DisplayName("should publish multiple domain events without errors")
    void testPublishMultipleEvents() {
        // Given - Using JavaFaker with Instancio to create realistic test data
        Customer customer1 = Customer.create(
                faker.name().fullName(),
                new Email(faker.internet().emailAddress()),
                Address.of(
                        faker.address().streetAddress(),
                        faker.address().city(),
                        faker.address().stateAbbr(),
                        faker.address().zipCode()
                ),
                Money.of(Instancio.of(BigDecimal.class)
                        .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal().min(BigDecimal.ZERO).max(new BigDecimal("10000.00")))
                        .create())
        );
        
        Customer customer2 = Customer.create(
                faker.name().fullName(),
                new Email(faker.internet().emailAddress()),
                Address.of(
                        faker.address().streetAddress(),
                        faker.address().city(),
                        faker.address().stateAbbr(),
                        faker.address().zipCode()
                ),
                Money.of(Instancio.of(BigDecimal.class)
                        .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal().min(BigDecimal.ZERO).max(new BigDecimal("10000.00")))
                        .create())
        );
        
        List<DomainEvent> events = new ArrayList<>();
        events.addAll(customer1.pullEvents());
        events.addAll(customer2.pullEvents());
        
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> eventPublisher.publishAll(events));
    }

    @Test
    @DisplayName("should handle empty event list")
    void testPublishEmptyList() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> eventPublisher.publishAll(new ArrayList<>()));
    }

    @Test
    @DisplayName("should handle null event list gracefully")
    void testPublishNullList() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> eventPublisher.publishAll(null));
    }
}
