package com.example.poc.domain.event;

import java.io.Serializable;
import java.time.LocalDateTime;

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
