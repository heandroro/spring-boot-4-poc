package com.example.poc.domain;

import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CustomerRepository
 * 
 * Tests real MongoDB persistence using Testcontainers.
 * Requires ENABLE_DOCKER_TESTS=true environment variable to run.
 * 
 * References:
 * - testing.md: Integration tests with Testcontainers
 * - mongodb.md: MongoDB repository patterns
 */
@Testcontainers
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
class CustomerRepositoryTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.8");

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry reg) {
        reg.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    CustomerRepository repository;

    @Test
    @DisplayName("should save and retrieve customer from MongoDB")
    void testSaveAndFind() {
        // Given
        Customer customer = Customer.create(
            "Alice Johnson",
            new Email("alice@example.com"),
            Address.of("456 Oak Ave", "Portland", "OR", "97201"),
            Money.of(new BigDecimal("2500.00"))
        );
        
        // When
        Customer saved = repository.save(customer);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("should find customer by email")
    void testFindByEmail() {
        // Given
        Email email = new Email("bob@example.com");
        Customer customer = Customer.create(
            "Bob Smith",
            email,
            Address.of("789 Pine Ln", "Seattle", "WA", "98101"),
            Money.of(new BigDecimal("5000.00"))
        );
        repository.save(customer);
        
        // When
        var found = repository.findByEmail(email.toString());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bob Smith");
    }

    @Test
    @DisplayName("should find active customers")
    void testFindAllActive() {
        // Given
        Customer active = Customer.create(
            "Charlie Brown",
            new Email("charlie@example.com"),
            Address.of("321 Elm St", "Boston", "MA", "02101"),
            Money.of(new BigDecimal("3000.00"))
        );
        repository.save(active);
        
        // When
        List<Customer> activeCustomers = repository.findAllActive();
        
        // Then
        assertThat(activeCustomers)
            .isNotEmpty()
            .anyMatch(c -> c.getEmail().toString().equals("charlie@example.com"));
    }

    @Test
    @DisplayName("should count customers by status")
    void testCountByStatus() {
        // Given
        Customer active = Customer.create(
            "Diana Prince",
            new Email("diana@example.com"),
            Address.of("555 Maple Dr", "Denver", "CO", "80201"),
            Money.of(new BigDecimal("7500.00"))
        );
        repository.save(active);
        
        // When
        long activeCount = repository.countByStatus(Customer.Status.ACTIVE);
        
        // Then
        assertThat(activeCount).isGreaterThan(0);
    }
}
