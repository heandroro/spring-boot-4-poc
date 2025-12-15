package com.example.poc.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CustomerCreatedEvent Unit Tests")
class CustomerCreatedEventTest {

    @Test
    @DisplayName("should expose name email and timestamp even without aggregate id")
    void shouldExposeFieldsWithoutAggregateId() {
        LocalDateTime now = LocalDateTime.now();
        CustomerCreatedEvent event = new CustomerCreatedEvent("user@example.com", "User Name", now);

        assertNull(event.getAggregateId());
        assertEquals("user@example.com", event.getEmail());
        assertEquals("User Name", event.getName());
        assertEquals(now, event.getCreatedAt());
        assertNotNull(event.getOccurredAt());
        assertEquals("CustomerCreatedEvent", event.getEventType());
    }
}
