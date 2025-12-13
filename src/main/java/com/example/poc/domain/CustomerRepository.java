package com.example.poc.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer aggregate root
 * 
 * This interface defines the persistence contract for customers.
 * Spring Data MongoDB provides the implementation.
 * 
 * Database Design:
 * - Collection: "customers"
 * - Indexes: email (unique), status, createdAt
 * - References: MongoDB collection design in docs/mongodb.md
 */
public interface CustomerRepository extends MongoRepository<Customer, String> {

    /**
     * Find customer by email
     * 
     * Emails must be unique across the system
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find all active customers
     */
    List<Customer> findByStatus(Customer.Status status);

    /**
     * Count customers by status
     */
    long countByStatus(Customer.Status status);

    /**
     * Custom query: Find customers with high credit utilization
     * 
     * @param threshold utilization percentage (0-100)
     */
    @Query("{ 'status': 'ACTIVE', 'creditLimit': { $gt: 0 } }")
    List<Customer> findAllActive();
}
