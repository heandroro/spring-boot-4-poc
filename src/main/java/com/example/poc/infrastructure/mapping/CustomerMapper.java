package com.example.poc.infrastructure.mapping;

import org.mapstruct.Mapper;
import com.example.poc.domain.Customer;
import com.example.poc.web.CustomerDto;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDto toDto(Customer customer);
    Customer toEntity(CustomerDto dto);
}
