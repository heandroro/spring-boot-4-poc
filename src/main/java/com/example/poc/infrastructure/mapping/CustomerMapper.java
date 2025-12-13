package com.example.poc.infrastructure.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.poc.domain.Customer;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.example.poc.web.CustomerDto;

/**
 * MapStruct Mapper for Customer entity
 * 
 * Handles conversion between:
 * - Customer (domain entity with rich types) -> CustomerDto (DTO with primitives)
 * - CustomerDto (DTO) -> Customer (domain entity)
 * 
 * Custom mappings are required for Value Objects (Money, Email, Address).
 * 
 * References:
 * - architecture.md: Mapping layer, Value Objects
 * - api.md: REST API contract
 * - code-examples.md: Section 7 - MapStruct integration
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * Convert Customer entity to DTO
     * 
     * MapStruct automatically extracts:
     * - email.value() -> email
     * - address.street() -> street
     * - address.city() -> city
     * - etc.
     * - creditLimit.amount() -> creditLimit
     * - availableCredit.amount() -> availableCredit
     */
    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "address.street", target = "street")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.state", target = "state")
    @Mapping(source = "address.postalCode", target = "postalCode")
    @Mapping(source = "address.country", target = "country")
    @Mapping(source = "creditLimit.amount", target = "creditLimit")
    @Mapping(source = "availableCredit.amount", target = "availableCredit")
    @Mapping(source = "status", target = "status")
    CustomerDto toDto(Customer customer);

    /**
     * Convert DTO to Customer entity
     * 
     * Note: This creates a new Customer instance (without ID).
     * The factory method Customer.create() is responsible for initialization.
     */
    default Customer toEntity(CustomerDto dto) {
        return Customer.create(
            dto.name(),
            new Email(dto.email()),
            new Address(
                dto.street(),
                dto.city(),
                dto.state(),
                dto.postalCode(),
                dto.country()
            ),
            new Money(dto.creditLimit(), "USD")
        );
    }
}

