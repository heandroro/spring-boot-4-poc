package com.example.poc.infrastructure.mapping;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.example.poc.domain.Customer;
import com.example.poc.web.CustomerDto;

class CustomerMapperTest {

    private final CustomerMapperImpl mapper = new CustomerMapperImpl();

    @Test
    void mapBetweenEntityAndDto() {
        Customer customer = Instancio.create(Customer.class);

        CustomerDto dto = mapper.toDto(customer);

        assertEquals(customer.name(), dto.name());
        assertEquals(customer.email(), dto.email());

        Customer back = mapper.toEntity(dto);
        assertEquals(dto.name(), back.name());
        assertEquals(dto.email(), back.email());
    }
}
