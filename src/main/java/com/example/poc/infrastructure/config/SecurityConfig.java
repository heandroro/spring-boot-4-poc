package com.example.poc.infrastructure.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String allowedOrigins;
    private final String allowedMethods;

    public SecurityConfig(
        @Value("${app.security.cors.allowed-origins:*}") String allowedOrigins,
        @Value("${app.security.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}") String allowedMethods
    ) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // TODO: Adjust to authenticated once JWT is implemented
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            List<String> origins = splitByComma(allowedOrigins);
            config.setAllowedOrigins(origins);
            config.setAllowedMethods(splitByComma(allowedMethods));
            config.setAllowedHeaders(List.of("*"));
            
            // Prevent misconfiguration: credentials require explicit origins
            // This validation is defensive programming for future configuration changes
            boolean hasWildcard = origins.contains("*");
            boolean allowCredentials = false; // Currently hardcoded, but this validation protects against future changes
            if (hasWildcard && allowCredentials) {
                throw new IllegalStateException("CORS: Cannot use allowCredentials=true with wildcard origins");
            }
            config.setAllowCredentials(allowCredentials);
            
            // NOTE: If you need to support credentials (cookies, authorization headers) in the future,
            // you must specify explicit origins instead of '*'. Browsers reject requests with
            // credentials if allowed origins is '*'. See CORS specification for details.
            return config;
        };
    }

    private List<String> splitByComma(String value) {
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }
}
