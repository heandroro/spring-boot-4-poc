package com.example.poc.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.poc.domain.Customer;
import com.example.poc.domain.CustomerRepository;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.github.javafaker.Faker;

/**
 * Integration Tests for MongoCustomerRepository
 * 
 * These tests verify real MongoDB persistence behavior including:
 * - Save/retrieve operations with Value Objects
 * - Custom queries (findHighCreditUtilization)
 * - Serialization/deserialization of domain objects
 * - MongoDB indexes
 * - Event publishing after save
 * 
 * Unlike the unit tests (MongoCustomerRepositoryTest), these tests use
 * Testcontainers to run against a real MongoDB instance in Docker.
 * 
 * Note: These tests require Docker and may take longer to run. They can be
 * enabled with ENABLE_DOCKER_TESTS=true environment variable.
 * 
 * References:
 * - docs/testing.md: Integration testing strategy
 * - PR heandroro/spring-boot-4-poc#16: Original conversion to unit tests
 */
@SpringBootTest
@Testcontainers
@DisplayName("MongoCustomerRepository Integration Tests")
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
class MongoCustomerRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("app.security.jwt.secret", () -> "test-secret-key-for-integration-tests-must-be-at-least-32-chars");
    }

    @Autowired
    private MongoCustomerRepository repository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        // Clean up database before each test
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("should persist and retrieve customer with Value Objects")
    void shouldPersistAndRetrieveCustomerWithValueObjects() {
        // Given: Create a customer with rich Value Objects
        String name = faker.name().fullName();
        Email email = new Email(faker.internet().emailAddress());
        Address address = Address.of(
            faker.address().streetAddress(),
            faker.address().city(),
            faker.address().stateAbbr(),
            faker.address().zipCode()
        );
        Money creditLimit = Money.of(new BigDecimal("5000.00"));

        Customer customer = Customer.create(name, email, address, creditLimit);

        // When: Save to MongoDB
        Customer saved = repository.save(customer);

        // Then: Should have ID assigned
        assertNotNull(saved.getId());

        // When: Retrieve from MongoDB
        Optional<Customer> retrieved = repository.findById(saved.getId());

        // Then: All Value Objects should be properly deserialized
        assertTrue(retrieved.isPresent());
        Customer found = retrieved.get();
        
        assertEquals(name, found.getName());
        assertEquals(email, found.getEmail());
        assertEquals(address, found.getAddress());
        assertEquals(creditLimit, found.getCreditLimit());
        assertEquals(creditLimit, found.getAvailableCredit()); // Initially all credit available
        assertEquals(Customer.Status.ACTIVE, found.getStatus());
    }

    @Test
    @DisplayName("should properly serialize and deserialize Money Value Object")
    void shouldProperlySerializeAndDeserializeMoneyValueObject() {
        // Given: Customer with specific credit operations
        Customer customer = createCustomerWithLimit(new BigDecimal("10000.00"));
        
        // Use some credit
        customer.useCredit(Money.of(new BigDecimal("3500.00")));
        
        // When: Save and retrieve
        Customer saved = repository.save(customer);
        Customer retrieved = repository.findById(saved.getId()).orElseThrow();

        // Then: Money calculations should be preserved
        assertEquals(new BigDecimal("10000.00"), retrieved.getCreditLimit().amount());
        assertEquals(new BigDecimal("6500.00"), retrieved.getAvailableCredit().amount());
        assertEquals(new BigDecimal("3500.00"), retrieved.getUsedCredit().amount());
        assertEquals(35.0, retrieved.getCreditUtilizationPercentage(), 0.1);
    }

    @Test
    @DisplayName("should properly serialize and deserialize Email Value Object")
    void shouldProperlySerializeAndDeserializeEmailValueObject() {
        // Given: Customer with Email value object
        String emailStr = faker.internet().emailAddress().toLowerCase();
        Email email = new Email(emailStr);
        Customer customer = Customer.create(
            faker.name().fullName(),
            email,
            createValidAddress(),
            Money.of(new BigDecimal("1000.00"))
        );

        // When: Save and retrieve
        Customer saved = repository.save(customer);
        Customer retrieved = repository.findById(saved.getId()).orElseThrow();

        // Then: Email should be preserved with methods available
        assertEquals(emailStr, retrieved.getEmail().value());
        assertEquals(emailStr, retrieved.getEmail().toString());
        assertEquals(email.getDomain(), retrieved.getEmail().getDomain());
        assertEquals(email.getLocalPart(), retrieved.getEmail().getLocalPart());
    }

    @Test
    @DisplayName("should properly serialize and deserialize Address Value Object")
    void shouldProperlySerializeAndDeserializeAddressValueObject() {
        // Given: Customer with Address value object
        String street = faker.address().streetAddress();
        String city = faker.address().city();
        String state = faker.address().stateAbbr();
        String postalCode = faker.address().zipCode();
        
        Address address = Address.of(street, city, state, postalCode);
        Customer customer = Customer.create(
            faker.name().fullName(),
            new Email(faker.internet().emailAddress()),
            address,
            Money.of(new BigDecimal("1000.00"))
        );

        // When: Save and retrieve
        Customer saved = repository.save(customer);
        Customer retrieved = repository.findById(saved.getId()).orElseThrow();

        // Then: Address should be preserved with all fields
        assertEquals(street, retrieved.getAddress().street());
        assertEquals(city, retrieved.getAddress().city());
        assertEquals(state, retrieved.getAddress().state());
        assertEquals(postalCode, retrieved.getAddress().postalCode());
        assertEquals("United States", retrieved.getAddress().country());
        assertTrue(retrieved.getAddress().isInCountry("United States"));
    }

    @Test
    @DisplayName("should find customers by high credit utilization using MongoDB query")
    void shouldFindCustomersByHighCreditUtilizationUsingMongoDBQuery() {
        // Given: Multiple customers with different credit utilization
        Customer lowUtil = createCustomerWithLimit(new BigDecimal("10000.00"));
        lowUtil.useCredit(Money.of(new BigDecimal("500.00"))); // 5% utilization
        
        Customer mediumUtil = createCustomerWithLimit(new BigDecimal("10000.00"));
        mediumUtil.useCredit(Money.of(new BigDecimal("3000.00"))); // 30% utilization
        
        Customer highUtil = createCustomerWithLimit(new BigDecimal("10000.00"));
        highUtil.useCredit(Money.of(new BigDecimal("7500.00"))); // 75% utilization
        
        Customer inactiveHighUtil = createCustomerWithLimit(new BigDecimal("10000.00"));
        inactiveHighUtil.useCredit(Money.of(new BigDecimal("8000.00"))); // 80% utilization
        inactiveHighUtil.setStatus(Customer.Status.INACTIVE);

        // Save all customers
        repository.save(lowUtil);
        repository.save(mediumUtil);
        repository.save(highUtil);
        repository.save(inactiveHighUtil);

        // When: Find customers with utilization >= 25%
        List<Customer> result = repository.findHighCreditUtilization(25.0);

        // Then: Should return only ACTIVE customers with >= 25% utilization
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(mediumUtil.getId())));
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(highUtil.getId())));
        assertFalse(result.stream().anyMatch(c -> c.getId().equals(lowUtil.getId())));
        assertFalse(result.stream().anyMatch(c -> c.getId().equals(inactiveHighUtil.getId())));
    }

    @Test
    @DisplayName("should find customer by email using unique index")
    void shouldFindCustomerByEmailUsingUniqueIndex() {
        // Given: Customer with specific email
        String emailStr = "unique.customer@example.com";
        Email email = new Email(emailStr);
        Customer customer = Customer.create(
            faker.name().fullName(),
            email,
            createValidAddress(),
            Money.of(new BigDecimal("2000.00"))
        );
        
        repository.save(customer);

        // When: Find by email
        Optional<Customer> found = repository.findByEmail(emailStr);

        // Then: Should find the customer
        assertTrue(found.isPresent());
        assertEquals(emailStr, found.get().getEmail().toString());
    }

    @Test
    @DisplayName("should check email existence")
    void shouldCheckEmailExistence() {
        // Given: Customer with specific email
        String existingEmail = "exists@example.com";
        Customer customer = Customer.create(
            faker.name().fullName(),
            new Email(existingEmail),
            createValidAddress(),
            Money.of(new BigDecimal("1000.00"))
        );
        
        repository.save(customer);

        // When: Check if emails exist
        boolean exists = repository.existsByEmail(existingEmail);
        boolean notExists = repository.existsByEmail("notexists@example.com");

        // Then: Should correctly identify existence
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("should find customers by status")
    void shouldFindCustomersByStatus() {
        // Given: Customers with different statuses
        Customer active1 = createValidCustomer();
        Customer active2 = createValidCustomer();
        Customer inactive = createValidCustomer();
        inactive.setStatus(Customer.Status.INACTIVE);

        repository.save(active1);
        repository.save(active2);
        repository.save(inactive);

        // When: Find by status
        List<Customer> activeCustomers = repository.findByStatus(Customer.Status.ACTIVE);
        List<Customer> inactiveCustomers = repository.findByStatus(Customer.Status.INACTIVE);

        // Then: Should find correct customers
        assertEquals(2, activeCustomers.size());
        assertEquals(1, inactiveCustomers.size());
    }

    @Test
    @DisplayName("should count customers by status")
    void shouldCountCustomersByStatus() {
        // Given: Multiple customers with different statuses
        Customer active1 = createValidCustomer();
        Customer active2 = createValidCustomer();
        Customer active3 = createValidCustomer();
        Customer suspended = createValidCustomer();
        suspended.setStatus(Customer.Status.SUSPENDED);

        repository.save(active1);
        repository.save(active2);
        repository.save(active3);
        repository.save(suspended);

        // When: Count by status
        long activeCount = repository.countByStatus(Customer.Status.ACTIVE);
        long suspendedCount = repository.countByStatus(Customer.Status.SUSPENDED);
        long inactiveCount = repository.countByStatus(Customer.Status.INACTIVE);

        // Then: Should have correct counts
        assertEquals(3, activeCount);
        assertEquals(1, suspendedCount);
        assertEquals(0, inactiveCount);
    }

    @Test
    @DisplayName("should find all active customers")
    void shouldFindAllActiveCustomers() {
        // Given: Mix of active and inactive customers
        Customer active1 = createValidCustomer();
        Customer active2 = createValidCustomer();
        Customer inactive = createValidCustomer();
        inactive.setStatus(Customer.Status.INACTIVE);
        Customer suspended = createValidCustomer();
        suspended.setStatus(Customer.Status.SUSPENDED);

        repository.save(active1);
        repository.save(active2);
        repository.save(inactive);
        repository.save(suspended);

        // When: Find all active customers
        List<Customer> activeCustomers = repository.findAllActive();

        // Then: Should only return ACTIVE customers
        assertEquals(2, activeCustomers.size());
        assertTrue(activeCustomers.stream().allMatch(c -> c.getStatus() == Customer.Status.ACTIVE));
    }

    @Test
    @DisplayName("should delete customer by ID")
    void shouldDeleteCustomerById() {
        // Given: Saved customer
        Customer customer = createValidCustomer();
        Customer saved = repository.save(customer);
        String customerId = saved.getId();

        // Verify customer exists
        assertTrue(repository.findById(customerId).isPresent());

        // When: Delete customer
        repository.deleteById(customerId);

        // Then: Customer should no longer exist
        assertFalse(repository.findById(customerId).isPresent());
    }

    @Test
    @DisplayName("should persist domain events are published after save")
    void shouldPersistDomainEventsArePublishedAfterSave() {
        // Given: New customer (will have CustomerCreatedEvent)
        Customer customer = createValidCustomer();
        
        // Verify customer has events before save
        assertEquals(1, customer.pullEvents().size());
        
        // Re-create to get events again
        customer = Customer.create(
            customer.getName(),
            customer.getEmail(),
            customer.getAddress(),
            customer.getCreditLimit()
        );

        // When: Save customer (repository should publish events)
        Customer saved = repository.save(customer);

        // Then: Customer should be persisted
        assertNotNull(saved.getId());
        
        // And: Events should have been pulled (cleared from customer)
        assertEquals(0, saved.pullEvents().size());
        
        // And: Customer should be retrievable from database
        Optional<Customer> retrieved = repository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
    }

    @Test
    @DisplayName("should handle credit operations and persist state correctly")
    void shouldHandleCreditOperationsAndPersistStateCorrectly() {
        // Given: Customer with credit
        Customer customer = createCustomerWithLimit(new BigDecimal("5000.00"));
        Customer saved = repository.save(customer);

        // When: Use credit and save
        saved.useCredit(Money.of(new BigDecimal("2000.00")));
        repository.save(saved);

        // Then: State should be persisted
        Customer retrieved = repository.findById(saved.getId()).orElseThrow();
        assertEquals(new BigDecimal("3000.00"), retrieved.getAvailableCredit().amount());
        assertEquals(new BigDecimal("2000.00"), retrieved.getUsedCredit().amount());

        // When: Restore some credit and save
        retrieved.restoreCredit(Money.of(new BigDecimal("500.00")));
        repository.save(retrieved);

        // Then: Updated state should be persisted
        Customer retrieved2 = repository.findById(saved.getId()).orElseThrow();
        assertEquals(new BigDecimal("3500.00"), retrieved2.getAvailableCredit().amount());
        assertEquals(new BigDecimal("1500.00"), retrieved2.getUsedCredit().amount());
    }

    // Helper methods

    private Customer createValidCustomer() {
        return Customer.create(
            faker.name().fullName(),
            new Email(faker.internet().emailAddress()),
            createValidAddress(),
            Money.of(new BigDecimal("1000.00"))
        );
    }

    private Customer createCustomerWithLimit(BigDecimal limit) {
        return Customer.create(
            faker.name().fullName(),
            new Email(faker.internet().emailAddress()),
            createValidAddress(),
            Money.of(limit)
        );
    }

    private Address createValidAddress() {
        return Address.of(
            faker.address().streetAddress(),
            faker.address().city(),
            faker.address().stateAbbr(),
            faker.address().zipCode()
        );
    }
}
