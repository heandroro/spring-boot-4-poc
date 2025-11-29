package com.example.poc.application;

import org.springframework.stereotype.Service;
import com.example.poc.domain.CustomerRepository;
import com.example.poc.infrastructure.mapping.CustomerMapper;
import com.example.poc.web.CustomerDto;
import com.example.poc.domain.Customer;

import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerService(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public CustomerDto create(CustomerDto dto) {
        Customer entity = mapper.toEntity(dto);
        Customer saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    public Optional<CustomerDto> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }
}
