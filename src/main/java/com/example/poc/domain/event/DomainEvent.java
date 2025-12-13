package com.example.poc.domain.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for all domain events
 * 
 * Domain events represent something that happened in the business domain.
 * They are immutable and serve as a historical record of important changes.
 * 
 * References:
 * - architecture.md: Domain Events pattern
 * - code-examples.md: Section 17 - Domain Events
 */
public abstract class DomainEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final LocalDateTime occurredAt;
    private final String aggregateId;

    protected DomainEvent(String aggregateId) {
        this.aggregateId = aggregateId;
        this.occurredAt = LocalDateTime.now();
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Get event type name for tracking
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getEventType() + "{" +
                "aggregateId='" + aggregateId + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
