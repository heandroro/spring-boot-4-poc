package com.example.poc.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ValidationErrorResponseTest {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new DummyController())
        .setControllerAdvice(new ValidationExceptionHandler())
        .build();

    @Test
    @DisplayName("should return ProblemDetail with errors for invalid request body")
    void shouldReturnProblemDetailForInvalidBody() throws Exception {
        var payload = "{" +
            "\"name\":\"\"," +
            "\"email\":\"not-an-email\"" +
            "}";

        mockMvc.perform(post("/api/dummy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title", is("Validation failed")))
            .andExpect(jsonPath("$.detail", containsString("invalid")))
            .andExpect(jsonPath("$.errors", is(notNullValue())))
            .andExpect(jsonPath("$.errors[*].field", hasItems("name", "email")));
    }

    @RestController
    @RequestMapping("/api/dummy")
    static class DummyController {
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Map<String, Object> create(@Valid @RequestBody DummyRequest request) {
            return Map.of("ok", true);
        }
    }

    public record DummyRequest(
        @NotBlank String name,
        @Email String email
    ) {}

    @RestControllerAdvice
    static class ValidationExceptionHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        Object handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
            var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> Map.of("field", err.getField(), "message", err.getDefaultMessage()))
                .toList();
            return Map.of(
                "title", "Validation failed",
                "detail", "Request body contains invalid fields",
                "errors", errors
            );
        }
    }
}
