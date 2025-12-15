package com.example.poc.application;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.poc.domain.Customer;
import com.example.poc.domain.CustomerRepository;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.example.poc.infrastructure.mapping.CustomerMapper;
import com.example.poc.web.CustomerDto;
import com.github.javafaker.Faker;

/**
 * Unit tests for {@link CustomerService}.
 * 
 * Tests cover CRUD operations, business logic, and mapper interactions using
 * Mockito for repository/mapper mocks and Instancio for realistic test data.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CustomerService service;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    @DisplayName("should create customer successfully")
    void shouldCreateCustomerSuccessfully() {
        // Given
        CustomerDto inputDto = createValidDto();
        Customer entity = createValidEntity();
        Customer saved = createValidEntity();

        when(mapper.toEntity(inputDto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(inputDto);

        // When
        CustomerDto result = service.create(inputDto);

        // Then
        assertNotNull(result);
        assertEquals(inputDto.name(), result.name());
        assertEquals(inputDto.email(), result.email());

        verify(mapper).toEntity(inputDto);
        verify(repository).save(entity);
        verify(mapper).toDto(saved);
    }

    @Test
    @DisplayName("should find customer by ID when exists")
    void shouldFindCustomerByIdWhenExists() {
        // Given
        String id = faker.internet().uuid();
        Customer entity = createValidEntity();
        CustomerDto dto = createValidDto();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // When
        Optional<CustomerDto> result = service.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(dto.name(), result.get().name());
        assertEquals(dto.email(), result.get().email());

        verify(repository).findById(id);
        verify(mapper).toDto(entity);
    }

    @Test
    @DisplayName("should return empty when customer not found")
    void shouldReturnEmptyWhenCustomerNotFound() {
        // Given
        String id = faker.internet().uuid();
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<CustomerDto> result = service.findById(id);

        // Then
        assertFalse(result.isPresent());

        verify(repository).findById(id);
        verify(mapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should call mapper and repository in correct order")
    void shouldCallMapperAndRepositoryInCorrectOrder() {
        // Given
        CustomerDto inputDto = createValidDto();
        Customer entity = createValidEntity();
        Customer saved = createValidEntity();

        when(mapper.toEntity(inputDto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(inputDto);

        // When
        service.create(inputDto);

        // Then - Verify call order
        InOrder inOrder = inOrder(mapper, repository, mapper);
        inOrder.verify(mapper).toEntity(inputDto);
        inOrder.verify(repository).save(entity);
        inOrder.verify(mapper).toDto(saved);
    }

    @Test
    @DisplayName("should return dto from repository save result")
    void shouldReturnDtoFromRepositorySaveResult() {
        // Given
        CustomerDto inputDto = createValidDto();
        Customer entity = createValidEntity();
        Customer savedEntity = Instancio.of(Customer.class)
                .set(field(Customer::getId), faker.internet().uuid())
                .create();
        CustomerDto expectedDto = Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::id), savedEntity.getId())
                .create();

        when(mapper.toEntity(inputDto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(expectedDto);

        // When
        CustomerDto result = service.create(inputDto);

        // Then
        assertEquals(expectedDto.id(), result.id());
    }

    // === Helper Methods ===

    private Customer createValidEntity() {
        BigDecimal creditLimit = generatePositiveMoney();
        return Customer.create(
                faker.name().fullName(),
                new Email(faker.internet().emailAddress()),
                createAddress(),
                Money.of(creditLimit));
    }

    private CustomerDto createValidDto() {
        BigDecimal creditLimit = generatePositiveMoney();
        return Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::name), faker.name().fullName())
                .set(field(CustomerDto::email), faker.internet().emailAddress())
                .set(field(CustomerDto::street), faker.address().streetAddress())
                .set(field(CustomerDto::city), faker.address().city())
                .set(field(CustomerDto::state), faker.address().stateAbbr())
                .set(field(CustomerDto::country), "BR")
                .set(field(CustomerDto::creditLimit), creditLimit)
                .set(field(CustomerDto::availableCredit), creditLimit)
                .create();
    }

    private Address createAddress() {
        return Address.of(
                faker.address().streetAddress(),
                faker.address().city(),
                faker.address().stateAbbr(),
                faker.address().zipCode());
    }

    private BigDecimal generatePositiveMoney() {
        return Instancio.of(BigDecimal.class)
                .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
                        .min(BigDecimal.ONE)
                        .max(new BigDecimal("10000.00")))
                .create();
    }
}
