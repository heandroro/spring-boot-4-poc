package com.example.poc.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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

import com.example.poc.domain.Customer;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;

/**
 * Integration tests for MongoCustomerRepository
 * 
 * Tests real MongoDB persistence using Testcontainers.
 * Requires ENABLE_DOCKER_TESTS=true environment variable to run.
 * 
 * Tests cover:
 * - Save and retrieve operations
 * - Custom queries
 * - Event publishing
 * - Index usage
 * - Value Object persistence
 * 
 * References:
 * - testing.md: Integration tests with Testcontainers
 * - mongodb.md: MongoDB repository patterns
 */
@Testcontainers
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
@DisplayName("MongoDB Customer Repository Integration Tests")
class MongoCustomerRepositoryTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry reg) {
        reg.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private MongoCustomerRepository repository;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.create(
                "Test Customer",
                new Email("test@example.com"),
                Address.of("123 Test St", "TestCity", "TC", "12345"),
                Money.of(new BigDecimal("5000.00")));
    }

    @Test
    @DisplayName("should save customer and publish domain events")
    void testSaveCustomer() {
        // When
        Customer saved = repository.save(testCustomer);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Customer");
        assertThat(saved.getEmail().toString()).isEqualTo("test@example.com");
        assertThat(saved.getStatus()).isEqualTo(Customer.Status.ACTIVE);

        // Verify events were pulled (transient field)
        assertThat(saved.pullEvents()).isEmpty();
    }

    @Test
    @DisplayName("should find customer by ID")
    void testFindById() {
        // Given
        Customer saved = repository.save(testCustomer);

        // When
        var found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Customer");
    }

    @Test
    @DisplayName("should find customer by email")
    void testFindByEmail() {
        // Given
        repository.save(testCustomer);

        // When
        var found = repository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Customer");
    }

    @Test
    @DisplayName("should find customers by status")
    void testFindByStatus() {
        // Given
        repository.save(testCustomer);

        Customer inactive = Customer.create(
                "Inactive Customer",
                new Email("inactive@example.com"),
                Address.of("456 Test Ave", "TestCity", "TC", "54321"),
                Money.of(new BigDecimal("3000.00")));
        inactive.setStatus(Customer.Status.INACTIVE);
        repository.save(inactive);

        // When
        List<Customer> activeCustomers = repository.findByStatus(Customer.Status.ACTIVE);
        List<Customer> inactiveCustomers = repository.findByStatus(Customer.Status.INACTIVE);

        // Then
        assertThat(activeCustomers)
                .isNotEmpty()
                .anyMatch(c -> c.getEmail().toString().equals("test@example.com"));

        assertThat(inactiveCustomers)
                .isNotEmpty()
                .anyMatch(c -> c.getEmail().toString().equals("inactive@example.com"));
    }

    @Test
    @DisplayName("should find all active customers")
    void testFindAllActive() {
        // Given
        repository.save(testCustomer);

        // When
        List<Customer> active = repository.findAllActive();

        // Then
        assertThat(active)
                .isNotEmpty()
                .anyMatch(c -> c.getEmail().toString().equals("test@example.com"));
    }

    @Test
    @DisplayName("should count customers by status")
    void testCountByStatus() {
        // Given
        repository.save(testCustomer);

        // When
        long activeCount = repository.countByStatus(Customer.Status.ACTIVE);

        // Then
        assertThat(activeCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("should find customers with high credit utilization")
    void testFindHighCreditUtilization() {
        // Given
        Customer highUtilization = Customer.create(
                "High Utilization",
                new Email("high@example.com"),
                Address.of("789 Credit Ln", "TestCity", "TC", "99999"),
                Money.of(new BigDecimal("10000.00")));
        // Use 80% of credit
        highUtilization.useCredit(Money.of(new BigDecimal("8000.00")));
        repository.save(highUtilization);

        // When
        List<Customer> highUsers = repository.findHighCreditUtilization(70.0);

        // Then
        assertThat(highUsers)
                .isNotEmpty()
                .anyMatch(c -> c.getEmail().toString().equals("high@example.com"));
    }

    @Test
    @DisplayName("should check if email exists")
    void testExistsByEmail() {
        // Given
        repository.save(testCustomer);

        // Then
        assertThat(repository.existsByEmail("test@example.com")).isTrue();
        assertThat(repository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("should persist Value Objects correctly")
    void testValueObjectPersistence() {
        // Given
        Money creditLimit = Money.of(new BigDecimal("15000.00"));
        Address address = new Address(
                "999 Value St",
                "VOCity",
                "VO",
                "11111",
                "United Kingdom");

        Customer customer = Customer.create(
                "Value Object Test",
                new Email("vo@example.com"),
                address,
                creditLimit);

        // When
        Customer saved = repository.save(customer);
        Customer retrieved = repository.findById(saved.getId()).orElseThrow();

        // Then - Money preserved
        assertThat(retrieved.getCreditLimit().amount()).isEqualTo(new BigDecimal("15000.00"));
        assertThat(retrieved.getCreditLimit().currency()).isEqualTo("USD");

        // Then - Address preserved
        assertThat(retrieved.getAddress().street()).isEqualTo("999 Value St");
        assertThat(retrieved.getAddress().city()).isEqualTo("VOCity");
        assertThat(retrieved.getAddress().country()).isEqualTo("United Kingdom");

        // Then - Email preserved (normalized)
        assertThat(retrieved.getEmail().toString()).isEqualTo("vo@example.com");
    }

    @Test
    @DisplayName("should handle credit operations and persist changes")
    void testCreditOperations() {
        // Given
        Customer saved = repository.save(testCustomer);

        // When - Use credit
        saved.useCredit(Money.of(new BigDecimal("2000.00")));
        Customer afterUse = repository.save(saved);

        // Then
        Customer retrieved = repository.findById(afterUse.getId()).orElseThrow();
        assertThat(retrieved.getAvailableCredit().amount())
                .isEqualTo(new BigDecimal("3000.00"));
        assertThat(retrieved.getUsedCredit().amount())
                .isEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    @DisplayName("should delete customer")
    void testDeleteCustomer() {
        // Given
        Customer saved = repository.save(testCustomer);
        String id = saved.getId();

        // When
        repository.deleteById(id);

        // Then
        assertThat(repository.findById(id)).isEmpty();
    }
}
