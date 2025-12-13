package com.example.poc.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.example.poc.domain.Customer;
import com.example.poc.domain.CustomerRepository;
import com.example.poc.domain.event.DomainEvent;
import com.example.poc.infrastructure.event.DomainEventPublisher;

/**
 * MongoDB Customer Repository Implementation
 * 
 * Extends Spring Data MongoDB repository with custom queries
 * and event publishing logic.
 * 
 * Key Features:
 * 1. Automatic event publishing after save/update
 * 2. Custom MongoDB queries using MongoTemplate
 * 3. Optimized queries with proper indexing
 * 4. Transaction support (when needed)
 * 
 * Architecture Pattern:
 * - CustomerRepository (domain interface)
 * - MongoCustomerRepository (infrastructure impl)
 * - Spring Data provides basic CRUD
 * - We add custom queries and event publishing
 * 
 * References:
 * - mongodb.md: Repository patterns, custom queries
 * - architecture.md: Repository abstraction layer
 * - code-examples.md: Section 15 - Repository pattern
 */
@Repository
public class MongoCustomerRepository {

    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    private final MongoTemplate mongoTemplate;

    public MongoCustomerRepository(
            CustomerRepository customerRepository,
            DomainEventPublisher eventPublisher,
            MongoTemplate mongoTemplate) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Save customer and publish domain events
     * 
     * This is the main save method that should be used by application layer.
     * It ensures domain events are published after successful persistence.
     * 
     * @param customer the customer to save
     * @return the saved customer
     */
    public Customer save(Customer customer) {
        // Pull events before save (they're transient and won't be persisted)
        List<DomainEvent> events = customer.pullEvents();

        // Save to MongoDB
        Customer saved = customerRepository.save(customer);

        // Publish events after successful save
        eventPublisher.publishAll(events);

        return saved;
    }

    /**
     * Find customer by ID
     */
    public Optional<Customer> findById(String id) {
        return customerRepository.findById(id);
    }

    /**
     * Find customer by email (must be unique)
     */
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    /**
     * Find all customers with a specific status
     * 
     * Uses index on 'status' field for performance
     */
    public List<Customer> findByStatus(Customer.Status status) {
        return customerRepository.findByStatus(status);
    }

    /**
     * Find all active customers
     * 
     * Custom query using @Query annotation in CustomerRepository
     */
    public List<Customer> findAllActive() {
        return customerRepository.findAllActive();
    }

    /**
     * Count customers by status
     */
    public long countByStatus(Customer.Status status) {
        return customerRepository.countByStatus(status);
    }

    /**
     * Custom query: Find customers with high credit utilization
     * 
     * This uses MongoTemplate for complex queries that can't be
     * expressed with Spring Data method naming conventions.
     * 
     * @param minUtilizationPercent minimum utilization percentage (0-100)
     * @return list of customers with high utilization
     */
    public List<Customer> findHighCreditUtilization(double minUtilizationPercent) {
        // MongoDB aggregation to calculate (creditLimit - availableCredit) /
        // creditLimit
        // This is just an example - in production you might want to use aggregation
        // pipeline

        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Customer.Status.ACTIVE.toString()));

        List<Customer> activeCustomers = mongoTemplate.find(query, Customer.class);

        // Filter in memory (or use MongoDB aggregation pipeline for better performance)
        return activeCustomers.stream()
                .filter(c -> c.getCreditUtilizationPercentage() >= minUtilizationPercent)
                .toList();
    }

    /**
     * Check if email already exists (for uniqueness validation)
     * 
     * @param email the email to check
     * @return true if email exists
     */
    public boolean existsByEmail(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    /**
     * Delete customer by ID
     * 
     * Note: In production, you might want soft deletes instead
     */
    public void deleteById(String id) {
        customerRepository.deleteById(id);
    }

    /**
     * Get all customers (use with caution in production)
     */
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
