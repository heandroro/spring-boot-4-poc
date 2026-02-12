package com.example.poc.web.exception;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(jacksonConverter)
                .build();
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {
        @PostMapping("/validate")
        TestRequest validate(@Valid @RequestBody TestRequest request) {
            return request;
        }

        @GetMapping("/illegal")
        void illegal() {
            throw new IllegalArgumentException("bad request");
        }

        @GetMapping("/generic")
        void generic() {
            throw new RuntimeException("boom");
        }
    }

    record TestRequest(@NotBlank String name) {
    }

    @Test
    @DisplayName("Should return validation error ProblemDetail with field errors")
    void shouldHandleValidationErrors() throws Exception {
        var body = objectMapper.writeValueAsString(new TestRequest(""));

        mockMvc.perform(post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("Request body contains invalid fields"))
                .andExpect(jsonPath("$.properties.errors[0].field").value("name"))
                .andExpect(jsonPath("$.properties.errors[0].message").value(containsString("must not be blank")))
                .andExpect(jsonPath("$.properties.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException as Bad Request")
    void shouldHandleIllegalArgument() throws Exception {
        mockMvc.perform(get("/test/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("bad request"))
                .andExpect(jsonPath("$.properties.timestamp").exists());
    }

    @Test
    @DisplayName("Should return generic ProblemDetail without leaking internals")
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.properties.timestamp").exists());
    }
}
