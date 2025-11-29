package com.example.poc.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
class CustomerRepositoryTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.8");

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry reg) {
        reg.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    CustomerRepository repository;

    @Test
    void saveAndFind() {
        Customer c = new Customer(null, "Alice", "alice@example.com");
        Customer saved = repository.save(c);

        assertThat(saved.id()).isNotNull();
        assertThat(repository.findById(saved.id())).isPresent();
    }
}
