package com.example.poc.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.poc.domain.event.CustomerCreatedEvent;

@Component
public class CustomerEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventHandler.class);

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
