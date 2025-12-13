package com.example.poc.domain.event;

import java.time.LocalDateTime;

/**
 * Event raised when a new customer is created
 * 
 * This event is published after the customer aggregate is persisted,
 * allowing other bounded contexts to react to customer creation.
 * 
 * References:
 * - architecture.md: Domain Events and bounded contexts
 * - code-examples.md: Section 17 - Domain Events
 */
public class CustomerCreatedEvent extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final String email;
    private final String name;
    private final LocalDateTime createdAt;

    public CustomerCreatedEvent(String email, String name, LocalDateTime createdAt) {
        super(null); // Customer ID not yet assigned at creation time
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "CustomerCreatedEvent{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
