package com.example.poc.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.poc.domain.event.CustomerCreatedEvent;

/**
 * Customer Event Handler
 * 
 * Listens to customer-related domain events and performs
 * side effects like:
 * - Sending welcome emails
 * - Creating audit logs
 * - Triggering workflows
 * - Updating read models
 * 
 * This is an example handler. In production, you might have:
 * - EmailNotificationHandler
 * - AuditLogHandler
 * - AnalyticsHandler
 * 
 * References:
 * - architecture.md: Event-Driven Architecture
 * - code-examples.md: Section 17 - Domain Events
 */
@Component
public class CustomerEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventHandler.class);

    /**
     * Handle CustomerCreatedEvent
     * 
     * This is called automatically by Spring when CustomerCreatedEvent
     * is published by DomainEventPublisher.
     * 
     * @param event the customer created event
     */
    @EventListener
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        log.info("Customer created event received: {}", event);

        // In production, this might:
        // 1. Send welcome email via EmailService
        // 2. Create audit log entry
        // 3. Trigger onboarding workflow
        // 4. Update analytics/reporting database
        // 5. Send notification to CRM system

        log.info("Welcome new customer: {} ({})", event.getName(), event.getEmail());

        // Example: You could inject services here
        // emailService.sendWelcomeEmail(event.getEmail(), event.getName());
        // auditLogService.logCustomerCreation(event);
    }
}
