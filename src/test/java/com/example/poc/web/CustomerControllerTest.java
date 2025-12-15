package com.example.poc.web;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.poc.application.CustomerService;
import com.github.javafaker.Faker;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Unit Tests")
class CustomerControllerTest {

    @Mock
    private CustomerService service;

    @InjectMocks
    private CustomerController controller;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

                
                Name("should create customer and return 201 Cre
                uldCreateCustomerAndReturn201Created() {
                omerDto in

            .set(field(CustomerDto::id), faker.internet().uuid())
            .set(field(CustomerDto::name), inputDto.name())
            .set(field(CustomerDto::email), inputDto.email())
            .create();
        
        when(service.create(any(CustomerDto.class))).thenReturn(createdDto);

        ResponseEntity<CustomerDto> response = controller.create(inputDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdDto.id(), response.getBody().id());
        assertTrue(response.getHeaders().getLocation().toString().contains("/customers/" + createdDto.id()));
        
        verify(service).create(any(CustomerDto.class));
    }

                
                Name("should return customer by ID and return 200 OK")
                uldReturnCustomerByIdAndReturn200Ok() {
                ng custome

            .set(field(CustomerDto::id), customerId)
            .set(field(CustomerDto::name), faker.name().fullName())
            .set(field(CustomerDto::email), faker.internet().emailAddress())
            .create();
        
        when(service.findById(customerId)).thenReturn(Optional.of(dto));


        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(customerId, response.getBody().id());
        
        verify(service).findById(customerId);
    }

    @Test
    @DisplayName("should return 404 Not Found when customer not exists")
    void shouldReturn404NotFoundWhenCustomerNotExists() {
        String nonExistentId = faker.internet().uuid();
        when(service.findById(nonExiste

        ResponseEntity<CustomerDto> response = controller.getById(nonExistentId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(service).findById(nonExistentId);
    }

    @Test
                Name("should map Location header correctl
                uldMapLocationHeaderCorrectly() {
                omerDto in

        CustomerDto createdDto = Instancio.of(CustomerDto.class)
            .set(field(CustomerDto::id), generatedId)
            .set(field(CustomerDto::name), inputDto.name())
            .create();
        
        when(service.create(any(CustomerDto.class))).thenReturn(createdDto);

        ResponseEntity<CustomerDto> response = controller.create(inputDto);

        assertNotNull(response.getHeaders().getLocation());
        assertTrue(response.getHeaders().getLo
                
                
                CustomerDto createValidDto() {
                ecimal creditLimit = generatePositiveMoney();
                rn Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::name), faker.na
                .set(field(CustomerDto::email), faker.internet().e
                .set(field(CustomerDto::street), faker.address().stree
                .set(field(CustomerDto::city), faker.address().city())
            .set(field(CustomerDto::state), faker.address().stateAbbr())
            .set(field(CustomerDto::country), "BR")
            .set(field(CustomerDto::creditLimit), creditLimit)
            .set(field(CustomerDto::available
                .create();
                        
                                
                                al generatePositiveMoney() {
                rn Instancio.of(BigDecimal.class)
            .generate(all(BigDecimal.class), 
                gen -> gen.math().bigDecimal()
                    .min(BigDecimal.ONE)
                    .max(new BigDecimal("10000.00")))
            .create();
    }
}
