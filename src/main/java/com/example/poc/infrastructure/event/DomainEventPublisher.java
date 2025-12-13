package com.example.poc.infrastructure.event;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.poc.domain.event.DomainEvent;

/**
 * Domain Event Publisher
 * 
 * Publishes domain events to Spring's ApplicationEventPublisher,
 * which can be consumed by event listeners across the application.
 * 
 * This decouples domain logic from infrastructure concerns and
 * enables reactive event-driven architecture.
 * 
 * Usage:
 * 1. Aggregate raises events: customer.pullEvents()
 * 2. Repository publishes after save: eventPublisher.publish(events)
 * 3. Event handlers react: @EventListener methods
 * 
 * References:
 * - architecture.md: Event-Driven Architecture, Domain Events
 * - code-examples.md: Section 17 - Domain Events
 */
@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Publish a single domain event
     * 
     * @param event the domain event to publish
     */
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {}", event);
        applicationEventPublisher.publishEvent(event);
        log.info("Domain event published: {} at {}", event.getEventType(), event.getOccurredAt());
    }

    /**
     * Publish multiple domain events
     * 
     * @param events list of domain events to publish
     */
    public void publishAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            log.debug("No domain events to publish");
            return;
        }

        log.debug("Publishing {} domain events", events.size());
        events.forEach(this::publish);
        log.info("Published {} domain events", events.size());
    }
}
