# Integration Test Generator Agent

Agente especializado em geração de testes de integração com Spring Boot, Testcontainers e MockMvc.

## Ativação

Este agente é ativado quando:
- Testar **repositories** com banco de dados real (MongoDB)
- Testar **controllers REST** com MockMvc
- Testar **fluxos E2E** (end-to-end)
- Comando: `@workspace gere teste de integração para {ClassName}`
- Comentário menciona "integration test" ou "teste de integração"

## Responsabilidades

1. ✅ Gerar testes de integração com Spring context completo
2. ✅ Configurar Testcontainers para MongoDB
3. ✅ Testar controllers REST com MockMvc
4. ✅ Validar fluxos E2E (múltiplas camadas)
5. ✅ Garantir RFC 7807 em error responses

## Stack de Testes de Integração

- **Framework**: JUnit 6 (Jupiter)
- **Spring**: @SpringBootTest, @DataMongoTest, @WebMvcTest
- **Containers**: Testcontainers (MongoDB 7.0)
- **HTTP**: MockMvc para REST endpoints
- **Assertions**: JUnit Assertions + JSONPath
- **Contexto**: **Com Spring** (@SpringBootTest ou slices)

## Imports Padrão

```java
// JUnit 6
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Spring Boot Test
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

// Testcontainers
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

// MockMvc
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

// Instancio + Faker (para fixtures)
import org.instancio.Instancio;
import com.github.javafaker.Faker;
import static org.instancio.Select.field;
import static org.instancio.Select.all;

// ObjectMapper (JSON)
import com.fasterxml.jackson.databind.ObjectMapper;
```

---

## Templates por Tipo

### 1. Repository Tests (com Testcontainers)

**Cobertura obrigatória:**
- ✅ Save and retrieve
- ✅ Update
- ✅ Delete
- ✅ Custom queries
- ✅ Not found scenarios
- ✅ Constraints (unique, indexes)

**Template:**

```java
package com.example.poc.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.instancio.Select.all;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Testcontainers
@DisplayName("{Repository} Integration Tests")

> Naming rule: Integration tests should use the `IT` suffix (e.g., `CustomerRepositoryIT`). The review agent will flag classes annotated with integration annotations that do not follow this convention.
class {Repository}Test {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(
        DockerImageName.parse("mongo:7.0")
    );

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private {Repository} repository;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        repository.deleteAll();
    }

    @Test
    @DisplayName("should save and retrieve entity")
    void shouldSaveAndRetrieveEntity() {
        // Given
        {Entity} entity = createValidEntity();

        // When
        {Entity} saved = repository.save(entity);
        Optional<{Entity}> retrieved = repository.findById(saved.getId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(saved.getId(), retrieved.get().getId());
        assertEquals(saved.getName(), retrieved.get().getName());
    }

    @Test
    @DisplayName("should update entity")
    void shouldUpdateEntity() {
        // Given
        {Entity} entity = createValidEntity();
        {Entity} saved = repository.save(entity);

        // When
        saved.updateName(faker.name().fullName());
        {Entity} updated = repository.save(saved);

        // Then
        Optional<{Entity}> retrieved = repository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(saved.getName(), retrieved.get().getName());
    }

    @Test
    @DisplayName("should delete entity")
    void shouldDeleteEntity() {
        // Given
        {Entity} entity = createValidEntity();
        {Entity} saved = repository.save(entity);

        // When
        repository.delete(saved);

        // Then
        Optional<{Entity}> retrieved = repository.findById(saved.getId());
        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("should find by custom query")
    void shouldFindByCustomQuery() {
        // Given
        String email = faker.internet().emailAddress();
        {Entity} entity = createValidEntity();
        entity.updateEmail(new Email(email));
        repository.save(entity);

        // When
        Optional<{Entity}> found = repository.findByEmail(email);

        // Then
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail().value());
    }

    @Test
    @DisplayName("should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
        // When
        Optional<{Entity}> found = repository.findById("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("should enforce unique constraint")
    void shouldEnforceUniqueConstraint() {
        // Given
        String uniqueEmail = faker.internet().emailAddress();
        {Entity} entity1 = createValidEntity();
        entity1.updateEmail(new Email(uniqueEmail));
        repository.save(entity1);

        // When
        {Entity} entity2 = createValidEntity();
        entity2.updateEmail(new Email(uniqueEmail));

        // Then
        assertThrows(Exception.class, () -> {
            repository.save(entity2);
        });
    }

    // === Helper Methods ===

    private {Entity} createValidEntity() {
        return {Entity}.create(
            faker.name().fullName(),
            new Email(faker.internet().emailAddress()),
            createAddress(),
            Money.of(generatePositiveMoney())
        );
    }

    private Address createAddress() {
        return Address.of(
            faker.address().streetAddress(),
            faker.address().city(),
            faker.address().stateAbbr(),
            faker.address().zipCode()
        );
    }

    private BigDecimal generatePositiveMoney() {
        return Instancio.of(BigDecimal.class)
            .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
                .min(BigDecimal.ONE)
                .max(new BigDecimal("10000.00")))
            .create();
    }
}
```

---

### 2. Controller Tests (com MockMvc)

**Cobertura obrigatória:**
- ✅ POST (201 Created)
- ✅ GET by ID (200 OK / 404 Not Found)
- ✅ PUT (200 OK)
- ✅ DELETE (204 No Content)
- ✅ Validation errors (400 Bad Request)
- ✅ Business errors (400 Bad Request)
- ✅ Pagination (GET list)
- ✅ RFC 7807 ProblemDetail

**Template:**

```java
package com.example.poc.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import com.example.poc.web.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.instancio.Select.all;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {Controller}
 * 
 * Uses MockMvc to test REST endpoints without starting full server.
 * Service layer is mocked to isolate controller logic.
 * 
 * @Import(GlobalExceptionHandler.class) includes the @ControllerAdvice
 * to test error handling with RFC 7807 ProblemDetail responses.
 */
@WebMvcTest({Controller}.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("{Controller} Integration Tests")
class {Controller}Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private {Service} service;

    private Faker faker = new Faker();

    @Test
    @DisplayName("should create entity and return 201")
    void shouldCreateEntity() throws Exception {
        // Given
        BigDecimal amount = generatePositiveMoney();
        {Dto} requestDto = Instancio.of({Dto}.class)
            .set(field({Dto}::id), null)
            .set(field({Dto}::name), faker.name().fullName())
            .set(field({Dto}::creditLimit), amount)
            .create();
        
        {Dto} responseDto = Instancio.of({Dto}.class)
            .set(field({Dto}::id), "123")
            .set(field({Dto}::name), requestDto.name())
            .create();

        when(service.create(any({Dto}.class))).thenReturn(responseDto);

        // When/Then
        mockMvc.perform(post("/api/{endpoint}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value("123"))
            .andExpect(jsonPath("$.name").value(requestDto.name()));

        verify(service).create(any({Dto}.class));
    }

    @Test
    @DisplayName("should return 200 when entity exists")
    void shouldReturnEntityWhenExists() throws Exception {
        // Given
        String id = "123";
        {Dto} dto = Instancio.of({Dto}.class)
            .set(field({Dto}::id), id)
            .set(field({Dto}::name), faker.name().fullName())
            .create();

        when(service.findById(id)).thenReturn(Optional.of(dto));

        // When/Then
        mockMvc.perform(get("/api/{endpoint}/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value(dto.name()));

        verify(service).findById(id);
    }

    @Test
    @DisplayName("should return 404 when entity not found")
    void shouldReturn404WhenNotFound() throws Exception {
        // Given
        String id = "nonexistent";
        when(service.findById(id)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/{endpoint}/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(header().string("Content-Type", containsString("application/problem+json")))
            .andExpect(jsonPath("$.title").exists())
            .andExpect(jsonPath("$.status").value(404));

        verify(service).findById(id);
    }

    @Test
    @DisplayName("should return 400 for validation errors")
    void shouldReturn400ForValidationErrors() throws Exception {
        // Given
        {Dto} invalidDto = Instancio.of({Dto}.class)
            .set(field({Dto}::name), "")  // Blank name (invalid)
            .set(field({Dto}::email), "invalid-email")  // Invalid format
            .create();

        // When/Then
        mockMvc.perform(post("/api/{endpoint}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andExpect(status().isBadRequest())
            .andExpect(header().string("Content-Type", containsString("application/problem+json")))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[*].field", hasItem("name")))
            .andExpect(jsonPath("$.errors[*].field", hasItem("email")));

        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("should return 400 for business rule violation")
    void shouldReturn400ForBusinessRuleViolation() throws Exception {
        // Given
        {Dto} dto = createValidDto();
        when(service.create(any({Dto}.class)))
            .thenThrow(new IllegalArgumentException("Business rule violated"));

        // When/Then
        mockMvc.perform(post("/api/{endpoint}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(header().string("Content-Type", containsString("application/problem+json")))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Business rule violated"))
            .andExpect(jsonPath("$.status").value(400));

        verify(service).create(any({Dto}.class));
    }

    @Test
    @DisplayName("should update entity and return 200")
    void shouldUpdateEntity() throws Exception {
        // Given
        String id = "123";
        {Dto} updateDto = createValidDto();
        {Dto} updatedDto = Instancio.of({Dto}.class)
            .set(field({Dto}::id), id)
            .create();

        when(service.update(eq(id), any({Dto}.class))).thenReturn(updatedDto);

        // When/Then
        mockMvc.perform(put("/api/{endpoint}/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));

        verify(service).update(eq(id), any({Dto}.class));
    }

    @Test
    @DisplayName("should delete entity and return 204")
    void shouldDeleteEntity() throws Exception {
        // Given
        String id = "123";
        doNothing().when(service).delete(id);

        // When/Then
        mockMvc.perform(delete("/api/{endpoint}/{id}", id))
            .andExpect(status().isNoContent());

        verify(service).delete(id);
    }

    @Test
    @DisplayName("should list entities with pagination")
    void shouldListEntitiesWithPagination() throws Exception {
        // Given
        // Service retorna Page<Dto>

        // When/Then
        mockMvc.perform(get("/api/{endpoint}")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.pageable").exists())
            .andExpect(jsonPath("$.totalElements").exists());

        verify(service).findAll(any());
    }

    // === Helper Methods ===

    private {Dto} createValidDto() {
        BigDecimal amount = generatePositiveMoney();
        return Instancio.of({Dto}.class)
            .set(field({Dto}::id), null)
            .set(field({Dto}::name), faker.name().fullName())
            .set(field({Dto}::email), faker.internet().emailAddress())
            .set(field({Dto}::creditLimit), amount)
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
```

**⚠️ Importante: @ControllerAdvice nos Testes**

`@WebMvcTest` **não carrega automaticamente** classes anotadas com `@ControllerAdvice`. Para testar o tratamento de exceções com `GlobalExceptionHandler`:

```java
@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)  // ✅ Inclui o exception handler
class CustomerControllerTest {
    // Agora os testes validam respostas RFC 7807 ProblemDetail
}
```

**Quando usar @Import:**
- ✅ Quando testar erro 400 (validação, business rules)
- ✅ Quando testar erro 404 (not found)
- ✅ Quando validar estrutura de ProblemDetail
- ❌ Não necessário se testar apenas 200/201 (happy path)

---

### 3. Exception Handler Tests

**Cobertura obrigatória:**
- ✅ Cada tipo de exceção mapeada
- ✅ Estrutura RFC 7807 (ProblemDetail)
- ✅ Status HTTP correto
- ✅ Mensagens de erro estruturadas

**Template:**

```java
package com.example.poc.web.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(TestController.class)  // Controller dummy para testar exceções
@Import(GlobalExceptionHandler.class)
@DisplayName("GlobalExceptionHandler Integration Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestService testService;

    @Test
    @DisplayName("should return 404 for EntityNotFoundException")
    void shouldReturn404ForEntityNotFoundException() throws Exception {
        // Given
        String id = "nonexistent";
        when(testService.findById(id))
            .thenThrow(new EntityNotFoundException("Entity not found with id: " + id));

        // When/Then
        mockMvc.perform(get("/api/test/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(header().string("Content-Type", containsString("application/problem+json")))
            .andExpect(jsonPath("$.type").exists())
            .andExpect(jsonPath("$.title").value("Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value(containsString("Entity not found")))
            .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    @DisplayName("should return 400 for IllegalArgumentException")
    void shouldReturn400ForIllegalArgumentException() throws Exception {
        // Given
        when(testService.create(any()))
            .thenThrow(new IllegalArgumentException("Invalid credit limit"));

        // When/Then
        mockMvc.perform(post("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid credit limit"));
    }

    @Test
    @DisplayName("should return 400 for validation errors with field details")
    void shouldReturn400ForValidationErrors() throws Exception {
        // Given
        String invalidJson = """
            {
                "name": "",
                "email": "invalid-email"
            }
            """;

        // When/Then
        mockMvc.perform(post("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[*].field", hasItems("name", "email")))
            .andExpect(jsonPath("$.errors[*].message").exists());
    }

    @Test
    @DisplayName("should return 500 for unexpected exceptions")
    void shouldReturn500ForUnexpectedExceptions() throws Exception {
        // Given
        when(testService.findById(any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When/Then
        mockMvc.perform(get("/api/test/{id}", "123"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value(containsString("Unexpected error")));
    }
}
```

---

### 4. E2E Tests (Full Spring Context)

**Quando usar:**
- Testar fluxo completo (múltiplas camadas)
- Validar integração entre componentes
- Smoke tests críticos

**Template:**

```java
package com.example.poc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("{Feature} E2E Tests")
class {Feature}E2ETest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(
        DockerImageName.parse("mongo:7.0")
    );

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private {Repository} repository;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        repository.deleteAll();
    }

    @Test
    @DisplayName("should complete full CRUD flow")
    void shouldCompleteFullCrudFlow() {
        // CREATE
        {Dto} createDto = createValidDto();
        ResponseEntity<{Dto}> createResponse = restTemplate.postForEntity(
            "/api/{endpoint}",
            createDto,
            {Dto}.class
        );
        
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        String id = createResponse.getBody().id();

        // READ
        ResponseEntity<{Dto}> getResponse = restTemplate.getForEntity(
            "/api/{endpoint}/" + id,
            {Dto}.class
        );
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(id, getResponse.getBody().id());

        // UPDATE
        {Dto} updateDto = Instancio.of({Dto}.class)
            .set(field({Dto}::name), faker.name().fullName())
            .create();
        
        restTemplate.put("/api/{endpoint}/" + id, updateDto);
        
        ResponseEntity<{Dto}> updatedResponse = restTemplate.getForEntity(
            "/api/{endpoint}/" + id,
            {Dto}.class
        );
        assertEquals(updateDto.name(), updatedResponse.getBody().name());

        // DELETE
        restTemplate.delete("/api/{endpoint}/" + id);
        
        ResponseEntity<{Dto}> deletedResponse = restTemplate.getForEntity(
            "/api/{endpoint}/" + id,
            {Dto}.class
        );
        assertEquals(HttpStatus.NOT_FOUND, deletedResponse.getStatusCode());
    }

    // === Helper Methods ===

    private {Dto} createValidDto() {
        BigDecimal amount = generatePositiveMoney();
        return Instancio.of({Dto}.class)
            .set(field({Dto}::id), null)
            .set(field({Dto}::name), faker.name().fullName())
            .set(field({Dto}::email), faker.internet().emailAddress())
            .set(field({Dto}::creditLimit), amount)
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
```

---

## Configuração de Testcontainers

### MongoDB Container

```java
@Container
static MongoDBContainer mongoDBContainer = new MongoDBContainer(
    DockerImageName.parse("mongo:7.0")
);

@DynamicPropertySource
static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
}
```

### Boas Práticas Testcontainers

- ✅ Use versão específica (`mongo:7.0`)
- ✅ Container `static` (compartilhado entre testes da classe)
- ✅ `@DynamicPropertySource` para configurar URL dinamicamente
- ✅ `@BeforeEach` para limpar dados (`repository.deleteAll()`)
- ❌ Não crie container por teste (lento)

---

## Anti-Padrões

### ❌ Não Faça

```java
// ❌ @SpringBootTest para testes simples (lento!)
@SpringBootTest
class MapperTest {  // Mapper não precisa de Spring!
    @Autowired
    private Mapper mapper;
}

// ❌ Container por teste (muito lento!)
@Container
MongoDBContainer container = new MongoDBContainer(...);  // NÃO static!

// ❌ Não limpar dados entre testes
@Test
void test1() {
    repository.save(entity);  // Poluiu o banco
}
@Test
void test2() {
    // Pode falhar por dados do test1!
}
```

### ✅ Faça

```java
// ✅ Use @DataMongoTest para repositories
@DataMongoTest
@Testcontainers
class RepositoryTest {
    // Slice test: apenas MongoDB context
}

// ✅ Container static (compartilhado)
@Container
static MongoDBContainer container = new MongoDBContainer(...);

// ✅ Limpe dados antes de cada teste
@BeforeEach
void setUp() {
    repository.deleteAll();
}
```

---

## Checklist de Geração

Antes de finalizar testes de integração gerados, verifique:

- [ ] **@DataMongoTest** para repositories
- [ ] **@WebMvcTest** para controllers
- [ ] **@SpringBootTest** apenas para E2E completo
- [ ] **@Testcontainers** + container static
- [ ] **@DynamicPropertySource** para MongoDB URI
- [ ] `@Import(GlobalExceptionHandler.class)` em @WebMvcTest
- [ ] `@BeforeEach` limpa dados (`repository.deleteAll()`)
- [ ] MockMvc para HTTP requests
- [ ] JSONPath para assertions JSON
- [ ] Validação de RFC 7807 (ProblemDetail)
- [ ] Verify interactions com @MockBean
- [ ] Cobertura de error paths (404, 400)

---

## Características de Testes de Integração

| Característica | Unit Test | Integration Test |
|----------------|-----------|------------------|
| **Velocidade** | Rápido (ms) | Lento (segundos) |
| **Contexto Spring** | ❌ Não | ✅ Sim |
| **Dependências** | Mockadas | Reais |
| **Annotations** | @Test, @Mock | @SpringBootTest, @DataMongoTest |
| **Infraestrutura** | ❌ Não | ✅ Testcontainers |
| **Escopo** | Classe isolada | Sistema E2E |
| **Banco de Dados** | ❌ Não | ✅ MongoDB real |
| **HTTP** | ❌ Não | ✅ MockMvc/TestRestTemplate |

---

## Referências

- **Boas Práticas Instancio**: [docs/instancio-best-practices.md](../../docs/instancio-best-practices.md)
- **Guia de Testes**: [docs/testing.md](../../docs/testing.md)
- **Unit Tests**: [agents/unit-test.md](unit-test.md)
- **Exemplos Reais**:
  - `MongoCustomerRepositoryTest.java` - Repository com Testcontainers
  - `GlobalExceptionHandlerTest.java` - Exception handler
  - Busque por `@DataMongoTest` no projeto

## Prompt de Ativação

Use este prompt para ativar o agente:

```
@workspace gere teste de integração para {ClassName}
```

Ou simplesmente:

```
teste de integração para {ClassName}
```
