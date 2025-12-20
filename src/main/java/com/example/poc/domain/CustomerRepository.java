package com.example.poc.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CustomerRepository extends MongoRepository<Customer, String> {

    /**
     * Find customer by email
     * 
     * Emails must be unique across the system
     */
    @Query("{ 'email.value': ?0 }")
    Optional<Customer> findByEmail(String email);

    List<Customer> findByStatus(Customer.Status status);

    long countByStatus(Customer.Status status);

    @Query("{ 'status': 'ACTIVE', 'creditLimit.amount': { $gt: 0 } }")
    List<Customer> findAllActive();
}
