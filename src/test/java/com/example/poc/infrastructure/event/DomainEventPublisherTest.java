package com.example.poc.infrastructure.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.poc.domain.Customer;
import com.example.poc.domain.event.DomainEvent;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.example.poc.domain.vo.Money;

/**
 * Integration tests for Domain Event Publishing
 * 
 * Tests that events can be published without errors.
 * Event listeners are tested separately in CustomerEventHandlerTest.
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

    @Test
    @DisplayName("should publish single domain event without errors")
    void testPublishSingleEvent() {
        // Given
        Customer customer = Customer.create(
                "Event Test Customer",
                new Email("event@example.com"),
                Address.of("123 Event St", "EventCity", "EC", "12345"),
                Money.of(new BigDecimal("1000.00"))
        );
        
        List<DomainEvent> events = customer.pullEvents();
        DomainEvent event = events.get(0);
        
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> eventPublisher.publish(event));
    }

    @Test
    @DisplayName("should publish multiple domain events without errors")
    void testPublishMultipleEvents() {
        // Given
        Customer customer1 = Customer.create(
                "Customer 1",
                new Email("customer1@example.com"),
                Address.of("111 Test St", "City1", "ST1", "11111"),
                Money.of(new BigDecimal("1000.00"))
        );
        
        Customer customer2 = Customer.create(
                "Customer 2",
                new Email("customer2@example.com"),
                Address.of("222 Test Ave", "City2", "ST2", "22222"),
                Money.of(new BigDecimal("2000.00"))
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
