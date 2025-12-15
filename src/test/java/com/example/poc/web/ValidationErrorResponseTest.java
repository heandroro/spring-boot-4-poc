package com.example.poc.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
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
            @Email String email) {
    }

    @RestControllerAdvice
    static class ValidationExceptionHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        org.springframework.http.ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
            var errors = ex.getBindingResult().getFieldErrors().stream()
                    .map(err -> Map.of("field", err.getField(), "message", err.getDefaultMessage()))
                    .toList();
            var pd = org.springframework.http.ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            pd.setTitle("Validation failed");
            pd.setDetail("Request body contains invalid fields");
            pd.setProperty("errors", errors);
            return pd;
        }
    }
}
