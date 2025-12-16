package com.example.poc.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.example.poc.domain.Customer;
import com.example.poc.domain.CustomerRepository;
import com.example.poc.domain.event.DomainEvent;
import com.example.poc.infrastructure.event.DomainEventPublisher;

@Repository
public class MongoCustomerRepository {

    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    private final MongoTemplate mongoTemplate;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "MongoTemplate is injected and thread-safe; storing the reference is intentional and safe")
    public MongoCustomerRepository(
            CustomerRepository customerRepository,
            DomainEventPublisher eventPublisher,
            MongoTemplate mongoTemplate) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
        this.mongoTemplate = mongoTemplate;
    }

    public Customer save(Customer customer) {
        // Pull events before save (they're transient and won't be persisted)
        List<DomainEvent> events = customer.pullEvents();

        // Save to MongoDB
        Customer saved = customerRepository.save(customer);

        // Publish events after successful save
        eventPublisher.publishAll(events);

        return saved;
    }

    public Optional<Customer> findById(String id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public List<Customer> findByStatus(Customer.Status status) {
        return customerRepository.findByStatus(status);
    }

    public List<Customer> findAllActive() {
        return customerRepository.findAllActive();
    }

    public long countByStatus(Customer.Status status) {
        return customerRepository.countByStatus(status);
    }

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

    public boolean existsByEmail(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    public void deleteById(String id) {
        customerRepository.deleteById(id);
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
