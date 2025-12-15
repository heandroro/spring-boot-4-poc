package com.example.poc.web;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.poc.application.CustomerService;
import com.example.poc.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController MVC Tests")
@SuppressWarnings("removal")
class CustomerControllerMvcTest {

    @Mock
    private CustomerService service;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        objectMapper = new ObjectMapper();
        // Register JavaTimeModule to handle java.time.Instant serialization
        objectMapper.findAndRegisterModules();
        
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        CustomerController controller = new CustomerController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("should return 201 and Location header when creating customer")
    void shouldReturn201AndLocationHeaderWhenCreatingCustomer() throws Exception {
        CustomerDto request = validRequestDto();

        CustomerDto created = Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::id), faker.internet().uuid())
                .set(field(CustomerDto::name), request.name())
                .set(field(CustomerDto::email), request.email())
                .set(field(CustomerDto::creditLimit), request.creditLimit())
                .set(field(CustomerDto::availableCredit), request.creditLimit())
                .create();

        when(service.create(any(CustomerDto.class))).thenReturn(created);

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/customers/" + created.id()))
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.name").value(created.name()))
                .andExpect(jsonPath("$.email").value(created.email()));
    }

    @Test
    @DisplayName("should return 400 with ProblemDetail when payload is invalid")
    void shouldReturn400WhenPayloadInvalid() throws Exception {
        BigDecimal creditLimit = null; // deliberately invalid

        CustomerDto invalid = Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::name), " ")
                .set(field(CustomerDto::email), "not-an-email")
                .set(field(CustomerDto::street), faker.address().streetAddress())
                .set(field(CustomerDto::city), faker.address().city())
                .set(field(CustomerDto::postalCode), faker.address().zipCode())
                .set(field(CustomerDto::country), "BR")
                .set(field(CustomerDto::creditLimit), creditLimit)
                .create();

        String responseBody = mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Validate Problem Detail RFC 7807 structure by parsing JSON
        org.junit.jupiter.api.Assertions.assertFalse(responseBody.isEmpty(), 
                "Response body should not be empty for validation errors");
        
        // Parse JSON response (catch parsing errors to provide clear failure message)
        Map<String, Object> response;
        try {
            response = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Failed to parse JSON response: " + responseBody, e);
            return; // unreachable, but satisfies compiler
        }
        
        // Validate RFC 7807 fields
        org.junit.jupiter.api.Assertions.assertTrue(response.containsKey("title"), 
                "Response should contain 'title' field");
        org.junit.jupiter.api.Assertions.assertEquals("Validation failed", response.get("title"), 
                "Title should be 'Validation failed'");
        
        org.junit.jupiter.api.Assertions.assertTrue(response.containsKey("detail"), 
                "Response should contain 'detail' field");
        org.junit.jupiter.api.Assertions.assertEquals("Request body contains invalid fields", response.get("detail"), 
                "Detail should describe validation failure");
        
        org.junit.jupiter.api.Assertions.assertTrue(response.containsKey("status"), 
                "Response should contain 'status' field");
        org.junit.jupiter.api.Assertions.assertEquals(400, response.get("status"), 
                "Status should be 400");
        
        org.junit.jupiter.api.Assertions.assertTrue(response.containsKey("instance"), 
                "Response should contain 'instance' field");
        org.junit.jupiter.api.Assertions.assertEquals("/customers", response.get("instance"), 
                "Instance should be '/customers'");
        
        // Validate properties contains timestamp and errors
        org.junit.jupiter.api.Assertions.assertTrue(response.containsKey("properties"), 
                "Response should contain 'properties' field");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) response.get("properties");
        
        org.junit.jupiter.api.Assertions.assertTrue(properties.containsKey("timestamp"), 
                "Properties should contain 'timestamp'");
        org.junit.jupiter.api.Assertions.assertNotNull(properties.get("timestamp"), 
                "Timestamp should not be null");
        
        org.junit.jupiter.api.Assertions.assertTrue(properties.containsKey("errors"), 
                "Properties should contain 'errors' field");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) properties.get("errors");
        org.junit.jupiter.api.Assertions.assertFalse(errors.isEmpty(), 
                "Errors array should not be empty");
        
        // Validate error structure (field and message)
        Map<String, String> firstError = errors.get(0);
        org.junit.jupiter.api.Assertions.assertTrue(firstError.containsKey("field"), 
                "Error should contain 'field' property");
        org.junit.jupiter.api.Assertions.assertTrue(firstError.containsKey("message"), 
                "Error should contain 'message' property");
        org.junit.jupiter.api.Assertions.assertNotNull(firstError.get("field"), 
                "Error field should not be null");
        org.junit.jupiter.api.Assertions.assertNotNull(firstError.get("message"), 
                "Error message should not be null");

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("should return 404 when customer does not exist")
    void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        String missingId = faker.internet().uuid();
        when(service.findById(missingId)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/customers/" + missingId))
                .andExpect(status().isNotFound());
    }

    private CustomerDto validRequestDto() {
        BigDecimal creditLimit = generatePositiveMoney();
        return Instancio.of(CustomerDto.class)
                .set(field(CustomerDto::name), faker.name().fullName())
                .set(field(CustomerDto::email), faker.internet().emailAddress())
                .set(field(CustomerDto::street), faker.address().streetAddress())
                .set(field(CustomerDto::city), faker.address().city())
                .set(field(CustomerDto::state), faker.address().stateAbbr())
                .set(field(CustomerDto::postalCode), faker.address().zipCode())
                .set(field(CustomerDto::country), "BR")
                .set(field(CustomerDto::creditLimit), creditLimit)
                .set(field(CustomerDto::availableCredit), creditLimit)
                .set(field(CustomerDto::status), "ACTIVE")
                .create();
    }

    private BigDecimal generatePositiveMoney() {
        return Instancio.of(BigDecimal.class)
                .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
                        .min(BigDecimal.ONE)
                        .max(new BigDecimal("10000.00")))
                .create();
    }
}
