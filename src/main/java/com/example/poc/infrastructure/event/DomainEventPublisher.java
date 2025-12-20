package com.example.poc.infrastructure.event;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.poc.domain.event.DomainEvent;

@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {}", event);
        applicationEventPublisher.publishEvent(event);
        log.info("Domain event published: {} at {}", event.getEventType(), event.getOccurredAt());
    }

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
