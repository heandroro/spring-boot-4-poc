package com.example.poc.infrastructure.mapping;

import com.example.poc.domain.Customer;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.example.poc.web.CustomerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomerMapper
 * 
 * Tests bidirectional mapping between:
 * - Customer (domain entity with rich types)
 * - CustomerDto (DTO with primitive types)
 * 
 * References:
 * - architecture.md: Mapping layer, Value Objects
 * - api.md: REST API contract
 * - code-examples.md: Section 7 - MapStruct integration
 */
@DisplayName("Customer Mapper Tests")
class CustomerMapperTest {

    private final CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    private Customer testCustomer;
    private Email testEmail;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testEmail = new Email("test@example.com");
        testAddress = Address.of("123 Test St", "TestCity", "TC", "12345");
        testCustomer = Customer.create(
            "Test Customer",
            testEmail,
            testAddress,
            Money.of(new BigDecimal("1000.00"))
        );
    }

    @Test
    @DisplayName("should map Customer to CustomerDto")
    void testEntityToDtoMapping() {
        // When
        CustomerDto dto = mapper.toDto(testCustomer);
        
        // Then
        assertEquals("Test Customer", dto.name());
        assertEquals("test@example.com", dto.email());
        assertEquals("123 Test St", dto.street());
        assertEquals("TestCity", dto.city());
        assertEquals("TC", dto.state());
        assertEquals("12345", dto.postalCode());
        assertEquals(new BigDecimal("1000.00"), dto.creditLimit());
        assertEquals("ACTIVE", dto.status());
    }

    @Test
    @DisplayName("should map CustomerDto to Customer")
    void testDtoToEntityMapping() {
        // Given
        CustomerDto dto = new CustomerDto(
            null,  // ID
            "New Customer",
            "new@example.com",
            "456 New St",
            "NewCity",
            "NC",
            "54321",
            "United States",
            new BigDecimal("2000.00"),
            new BigDecimal("2000.00"),
            "ACTIVE"
        );
        
        // When
        Customer customer = mapper.toEntity(dto);
        
        // Then
        assertEquals("New Customer", customer.getName());
        assertEquals("new@example.com", customer.getEmail().toString());
        assertEquals("456 New St", customer.getAddress().street());
        assertEquals("NewCity", customer.getAddress().city());
        assertEquals("NC", customer.getAddress().state());
        assertEquals("54321", customer.getAddress().postalCode());
        assertEquals(new BigDecimal("2000.00"), customer.getCreditLimit().amount());
    }

    @Test
    @DisplayName("should map Customer roundtrip (Entity -> DTO -> Entity)")
    void testRoundTripMapping() {
        // When
        CustomerDto dto = mapper.toDto(testCustomer);
        Customer mapped = mapper.toEntity(dto);
        
        // Then
        assertEquals(testCustomer.getName(), mapped.getName());
        assertEquals(testCustomer.getEmail().toString(), mapped.getEmail().toString());
        assertEquals(testCustomer.getAddress().street(), mapped.getAddress().street());
        assertEquals(testCustomer.getCreditLimit().amount(), mapped.getCreditLimit().amount());
    }

    @Test
    @DisplayName("should preserve address components in mapping")
    void testAddressComponentsPreserved() {
        // Given
        Address complexAddress = new Address(
            "789 Complex Ave",
            "ComplexCity",
            "CC",
            "98765",
            "United Kingdom"
        );
        Customer customer = Customer.create(
            "Complex Address Customer",
            new Email("complex@example.com"),
            complexAddress,
            Money.of(new BigDecimal("5000.00"))
        );
        
        // When
        CustomerDto dto = mapper.toDto(customer);
        
        // Then
        assertEquals("789 Complex Ave", dto.street());
        assertEquals("ComplexCity", dto.city());
        assertEquals("CC", dto.state());
        assertEquals("98765", dto.postalCode());
        assertEquals("United Kingdom", dto.country());
    }

    @Test
    @DisplayName("should preserve monetary values in mapping")
    void testMonetaryValuesPreserved() {
        // Given
        Customer customer = Customer.create(
            "Wealthy Customer",
            new Email("wealthy@example.com"),
            testAddress,
            Money.of(new BigDecimal("999999.99"))
        );
        
        // When
        CustomerDto dto = mapper.toDto(customer);
        Customer mapped = mapper.toEntity(dto);
        
        // Then
        assertEquals(new BigDecimal("999999.99"), dto.creditLimit());
        assertEquals(new BigDecimal("999999.99"), mapped.getCreditLimit().amount());
    }
}
