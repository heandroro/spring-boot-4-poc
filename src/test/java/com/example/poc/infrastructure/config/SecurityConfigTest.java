package com.example.poc.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    @DisplayName("Should configure CORS with default origins and methods")
    void shouldConfigureCorsWithDefaults() {
        var config = new SecurityConfig("*", "GET,POST,PUT,PATCH,DELETE,OPTIONS");

        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(null);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("*");
        assertThat(cors.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
        assertThat(cors.getAllowCredentials()).isFalse();
    }

    @Test
    @DisplayName("Should expose BCryptPasswordEncoder bean")
    void shouldProvidePasswordEncoder() {
        var config = new SecurityConfig("*", "GET,POST");

        PasswordEncoder encoder = config.passwordEncoder();

        assertThat(encoder).isNotNull();
        assertThat(encoder.encode("secret")).isNotEmpty();
    }
}
