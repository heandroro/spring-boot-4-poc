package com.example.poc.infrastructure.persistence;

import static org.instancio.Select.all;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.example.poc.domain.Customer;
import com.example.poc.domain.CustomerRepository;
import com.example.poc.domain.event.DomainEvent;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.example.poc.infrastructure.event.DomainEventPublisher;
import com.github.javafaker.Faker;

@ExtendWith(MockitoExtension.class)
@DisplayName("MongoCustomerRepository Unit Tests")
class MongoCustomerRepositoryTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MongoCustomerRepository repository;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    @DisplayName("should save customer and publish pulled events")
    void shouldSaveCustomerAndPublishPulledEvents() {
        // Given
        Customer customer = createValidCustomer();
        List<DomainEvent> initialEvents = customer.pullEvents();
        assertEquals(1, initialEvents.size());

        // Re-create to accumulate events again
        customer = Customer.create(customer.getName(), customer.getEmail(), customer.getAddress(),
                customer.getCreditLimit());

        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Customer saved = repository.save(customer);

        // Then
        assertNotNull(saved);
        verify(customerRepository).save(customer);

        ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).publishAll(eventsCaptor.capture());
        List<DomainEvent> published = eventsCaptor.getValue();
        assertFalse(published.isEmpty());
    }

    @Test
    @DisplayName("should filter customers by high credit utilization using MongoTemplate")
    void shouldFilterCustomersByHighCreditUtilizationUsingMongoTemplate() {
        // Given: two ACTIVE customers with different utilization
        Customer high = createCustomerWithLimit(new BigDecimal("1000.00"));
        Customer low = createCustomerWithLimit(new BigDecimal("1000.00"));

        high.useCredit(Money.of(new BigDecimal("300.00")));
        low.useCredit(Money.of(new BigDecimal("50.00")));

        when(mongoTemplate.find(any(Query.class), eq(Customer.class)))
                .thenReturn(List.of(high, low));

        double threshold = 20.0; // percent

        // When
        List<Customer> result = repository.findHighCreditUtilization(threshold);

        // Then: verify results
        assertEquals(1, result.size());
        assertTrue(result.contains(high));
        assertFalse(result.contains(low));
        
        // Capture and verify the Query that was constructed
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Customer.class));
        
        Query capturedQuery = queryCaptor.getValue();
        assertNotNull(capturedQuery, "Query should not be null");
        
        // Verify query contains criteria for ACTIVE status
        String queryString = capturedQuery.toString();
        assertTrue(queryString.contains("status"), 
                "Query should filter by status field");
        assertTrue(queryString.contains("ACTIVE"), 
                "Query should filter for ACTIVE customers");
        
        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("should delegate findById and existsByEmail to CustomerRepository")
    void shouldDelegateFindAndExistsToCustomerRepository() {
        // Given
        Customer customer = createValidCustomer();
        when(customerRepository.findById("123")).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail(customer.getEmail().toString())).thenReturn(Optional.of(customer));

        // When
        Optional<Customer> found = repository.findById("123");
        boolean exists = repository.existsByEmail(customer.getEmail().toString());

        // Then
        assertTrue(found.isPresent());
        assertTrue(exists);
        verify(customerRepository).findById("123");
        verify(customerRepository).findByEmail(customer.getEmail().toString());
    }

    @Test
    @DisplayName("should delegate email and collection lookups to CustomerRepository")
    void shouldDelegateEmailAndCollectionLookupsToCustomerRepository() {
        // Given
        Customer customer = createValidCustomer();
        when(customerRepository.findByEmail(customer.getEmail().toString())).thenReturn(Optional.of(customer));
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        // When
        Optional<Customer> foundByEmail = repository.findByEmail(customer.getEmail().toString());
        List<Customer> all = repository.findAll();

        // Then
        assertTrue(foundByEmail.isPresent());
        assertEquals(customer, foundByEmail.orElseThrow());
        assertEquals(1, all.size());
        assertTrue(all.contains(customer));
        verify(customerRepository).findByEmail(customer.getEmail().toString());
        verify(customerRepository).findAll();
    }

    @Test
    @DisplayName("should delegate status queries to CustomerRepository")
    void shouldDelegateStatusQueriesToCustomerRepository() {
        // Given
        Customer active1 = createValidCustomer();
        Customer active2 = createValidCustomer();
        when(customerRepository.findByStatus(Customer.Status.ACTIVE)).thenReturn(List.of(active1, active2));
        when(customerRepository.countByStatus(Customer.Status.ACTIVE)).thenReturn(2L);
        when(customerRepository.findAllActive()).thenReturn(List.of(active1, active2));

        // When
        List<Customer> byStatus = repository.findByStatus(Customer.Status.ACTIVE);
        long count = repository.countByStatus(Customer.Status.ACTIVE);
        List<Customer> allActive = repository.findAllActive();

        // Then
        assertEquals(2, byStatus.size());
        assertEquals(2, count);
        assertEquals(byStatus, allActive);
        verify(customerRepository).findByStatus(Customer.Status.ACTIVE);
        verify(customerRepository).countByStatus(Customer.Status.ACTIVE);
        verify(customerRepository).findAllActive();
    }

    @Test
    @DisplayName("should delegate deleteById to CustomerRepository")
    void shouldDelegateDeleteByIdToCustomerRepository() {
        // When
        repository.deleteById("abc");

        // Then
        verify(customerRepository).deleteById("abc");
    }

    private Customer createValidCustomer() {
        return Customer.create(
                faker.name().fullName(),
                new Email(faker.internet().emailAddress()),
                Address.of(
                        faker.address().streetAddress(),
                        faker.address().city(),
                        faker.address().stateAbbr(),
                        faker.address().zipCode()),
                Money.of(generatePositiveMoney()));
    }

    private BigDecimal generatePositiveMoney() {
        return Instancio.of(BigDecimal.class)
                .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
                        .min(new BigDecimal("500.00"))
                        .max(new BigDecimal("2000.00")))
                .create();
    }

    private Customer createCustomerWithLimit(BigDecimal limit) {
        return Customer.create(
                faker.name().fullName(),
                new Email(faker.internet().emailAddress()),
                Address.of(
                        faker.address().streetAddress(),
                        faker.address().city(),
                        faker.address().stateAbbr(),
                        faker.address().zipCode()),
                Money.of(limit));
    }
}
