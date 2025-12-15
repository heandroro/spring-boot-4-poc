package com.example.poc.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainEventTest {

    private static class TestEvent extends DomainEvent {
        TestEvent(String aggregateId) {
            super(aggregateId);
        }
    }

    @Test
    @DisplayName("should expose aggregateId, occurredAt and eventType")
    void shouldExposeCoreFields() {
        TestEvent event = new TestEvent("agg-123");

        assertEquals("agg-123", event.getAggregateId());
        assertNotNull(event.getOccurredAt());
        assertEquals("TestEvent", event.getEventType());
        assertTrue(event.toString().contains("agg-123"));
        assertTrue(event.toString().contains("occurredAt"));
    }
}
