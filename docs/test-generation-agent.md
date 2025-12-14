# Agente Gerador de Testes Unitários

## Objetivo

Este documento define as regras e padrões para geração automática de testes unitários no projeto. Use-o como especificação para:
- Agentes de IA (GitHub Copilot, GPT, etc.)
- Ferramentas de geração de código
- Desenvolvedores criando novos testes

---

## Princípios Fundamentais

### 1. Stack e Ferramentas
- **Framework:** JUnit 5 (Jupiter)
- **Mocking:** Mockito
- **Fixtures:** Instancio + JavaFaker
- **Assertions:** JUnit Assertions + AssertJ (quando necessário)
- **Cobertura:** Jacoco (mínimo 80%)

### 2. Arquitetura DDD
Testes organizados por camada:
```
src/test/java/com/example/poc/
├── domain/              # Agregados, Value Objects, Repositórios
├── application/         # Casos de uso, Serviços
├── infrastructure/      # Persistência, Mapeamento, Eventos
└── web/                 # Controllers, DTOs, Exception Handlers
```

### 3. Convenções de Nomenclatura
- **Classe:** `{ClasseTestada}Test` (ex: `CustomerTest`, `CustomerMapperTest`)
- **Método:** `should{Comportamento}When{Contexto}` (ex: `shouldRejectNegativeCreditWhenCreating`)
- **DisplayName:** Português ou inglês descritivo

---

## Templates por Camada

## 1. Domain Layer (Agregados e Value Objects)

### Estrutura Base
```java
package com.example.poc.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.instancio.Select.field;
import static org.instancio.Select.all;

/**
 * Unit tests for {ClassName}
 * 
 * Tests:
 * - Construction and validation
 * - Business rules
 * - State transitions
 * - Invariants
 * 
 * References:
 * - architecture.md: Domain layer patterns
 * - {ClassName}.java: Implementation
 */
@DisplayName("{ClassName} Tests")
class {ClassName}Test {

    private Faker faker;
    
    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    // === Construction Tests ===
    
    @Test
    @DisplayName("should create valid {entity}")
    void shouldCreateValid() {
        // Given
        // ... prepare valid inputs
        
        // When
        {ClassName} entity = {ClassName}.create(...);
        
        // Then
        assertNotNull(entity);
        // ... assert invariants
    }
    
    @Test
    @DisplayName("should reject null {field}")
    void shouldRejectNull{Field}() {
        // When/Then
        assertThrows(NullPointerException.class, 
            () -> {ClassName}.create(null, ...));
    }
    
    @Test
    @DisplayName("should reject invalid {field}")
    void shouldRejectInvalid{Field}() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
            () -> {ClassName}.create(invalidValue, ...));
    }

    // === Business Logic Tests ===
    
    @Test
    @DisplayName("should {businessRule}")
    void should{BusinessRule}() {
        // Given
        {ClassName} entity = createValid{ClassName}();
        
        // When
        entity.{method}(...);
        
        // Then
        // ... assert post-conditions
    }
    
    @Test
    @DisplayName("should reject {businessRule} when {condition}")
    void shouldReject{BusinessRule}When{Condition}() {
        // Given
        {ClassName} entity = createValid{ClassName}();
        
        // When/Then
        assertThrows(IllegalStateException.class, 
            () -> entity.{method}(...));
    }
    
    // === Helper Methods ===
    
    private {ClassName} createValid{ClassName}() {
        return {ClassName}.create(
            faker.name().fullName(),
            // ... outros campos válidos
        );
    }
}
```

### Exemplo Concreto: Customer
```java
@Test
@DisplayName("should use credit when customer is active")
void shouldUseCreditWhenActive() {
    // Given
    Customer customer = Customer.create(
        "John Doe",
        new Email("john@example.com"),
        Address.of("123 Main St", "City", "ST", "12345"),
        Money.of(new BigDecimal("1000.00"))
    );
    Money purchaseAmount = Money.of(new BigDecimal("200.00"));
    
    // When
    customer.useCredit(purchaseAmount);
    
    // Then
    assertEquals(new BigDecimal("800.00"), customer.getAvailableCredit().amount());
}
```

---

## 2. Infrastructure Layer (Mappers, Repositories)

### Template: Mapper Test
```java
package com.example.poc.infrastructure.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.instancio.Select.field;
import static org.instancio.Select.all;

/**
 * Unit tests for {Mapper}
 * 
 * Tests bidirectional mapping:
 * - Entity -> DTO
 * - DTO -> Entity
 * - Roundtrip mapping
 * - Special cases (nulls, blanks, edge values)
 * 
 * This is a pure unit test using MapStruct factory pattern.
 */
@DisplayName("{Mapper} Tests")
class {Mapper}Test {

    private final {Mapper} mapper = Mappers.getMapper({Mapper}.class);
    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    @DisplayName("should map entity to DTO")
    void shouldMapEntityToDto() {
        // Given
        {Entity} entity = create{Entity}();
        
        // When
        {Dto} dto = mapper.toDto(entity);
        
        // Then
        assertNotNull(dto);
        assertEquals(entity.get{Field}(), dto.{field}());
        // ... assert all mapped fields
    }

    @Test
    @DisplayName("should map DTO to entity")
    void shouldMapDtoToEntity() {
        // Given
        {Dto} dto = create{Dto}();
        
        // When
        {Entity} entity = mapper.toEntity(dto);
        
        // Then
        assertNotNull(entity);
        assertEquals(dto.{field}(), entity.get{Field}());
        // ... assert all mapped fields
    }

    @Test
    @DisplayName("should preserve values in roundtrip mapping")
    void shouldPreserveValuesInRoundtrip() {
        // Given
        {Entity} original = create{Entity}();
        
        // When
        {Dto} dto = mapper.toDto(original);
        {Entity} mapped = mapper.toEntity(dto);
        
        // Then
        assertEquals(original.get{Field}(), mapped.get{Field}());
        // ... assert key fields preserved
    }

    @Test
    @DisplayName("should map realistic data generated by Faker + Instancio")
    void shouldMapRealisticData() {
        // Given
        BigDecimal amount = Instancio.of(BigDecimal.class)
            .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
                .min(BigDecimal.ONE)
                .max(new BigDecimal("10000.00")))
            .create();

        {Dto} dto = Instancio.of({Dto}.class)
            .set(field({Dto}::id), null)
            .set(field({Dto}::name), faker.name().fullName())
            .set(field({Dto}::email), faker.internet().emailAddress())
            .set(field({Dto}::{moneyField}), amount)
            .create();
        
        // When
        {Entity} entity = mapper.toEntity(dto);
        
        // Then
        assertEquals(dto.name(), entity.getName());
        assertEquals(dto.email(), entity.getEmail().toString());
        assertTrue(entity.get{MoneyField}().amount().compareTo(BigDecimal.ZERO) > 0);
    }

    // === Helper Methods ===
    
    private {Entity} create{Entity}() {
        return {Entity}.create(
            faker.name().fullName(),
            new Email(faker.internet().emailAddress()),
            createAddress(),
            Money.of(generatePositiveMoney())
        );
    }

    private {Dto} create{Dto}() {
        BigDecimal amount = generatePositiveMoney();
        return Instancio.of({Dto}.class)
            .set(field({Dto}::id), null)
            .set(field({Dto}::status), "ACTIVE")
            .set(field({Dto}::{moneyField}), amount)
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

### Template: Repository Test (MongoDB)
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
import com.github.javafaker.Faker;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {Repository}
 * 
 * Uses Testcontainers for real MongoDB instance.
 * Tests CRUD operations and custom queries.
 */
@DataMongoTest
@Testcontainers
@DisplayName("{Repository} Tests")
class {Repository}Test {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
        .withExposedPorts(27017);

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
    void shouldSaveAndRetrieve() {
        // Given
        {Entity} entity = create{Entity}();
        
        // When
        {Entity} saved = repository.save(entity);
        Optional<{Entity}> retrieved = repository.findById(saved.getId());
        
        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().get{Field}()).isEqualTo(entity.get{Field}());
    }

    @Test
    @DisplayName("should update entity")
    void shouldUpdate() {
        // Given
        {Entity} entity = repository.save(create{Entity}());
        
        // When
        entity.{updateMethod}(...);
        {Entity} updated = repository.save(entity);
        
        // Then
        Optional<{Entity}> retrieved = repository.findById(updated.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().get{Field}()).isEqualTo(updatedValue);
    }

    @Test
    @DisplayName("should delete entity")
    void shouldDelete() {
        // Given
        {Entity} entity = repository.save(create{Entity}());
        
        // When
        repository.deleteById(entity.getId());
        
        // Then
        assertThat(repository.findById(entity.getId())).isEmpty();
    }

    @Test
    @DisplayName("should find by custom query")
    void shouldFindByCustomQuery() {
        // Given
        {Entity} entity = repository.save(create{Entity}());
        
        // When
        Optional<{Entity}> found = repository.findBy{Field}(entity.get{Field}());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(entity.getId());
    }

    // === Helper Methods ===
    
    private {Entity} create{Entity}() {
        return {Entity}.create(
            faker.name().fullName(),
            // ... campos válidos
        );
    }
}
```

---

## 3. Web Layer (Controllers, Exception Handlers)

### Template: Exception Handler Test
```java
package com.example.poc.web.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {Handler}
 * 
 * Validates RFC 7807 ProblemDetail responses for:
 * - Validation errors (400)
 * - Business logic errors (400/422)
 * - Not found errors (404)
 */
@DisplayName("{Handler} Tests")
class {Handler}Test {

    private final {Handler} handler = new {Handler}();

    @Test
    @DisplayName("should return 400 with structured errors for validation failures")
    void shouldReturn400ForValidationErrors() {
        // Given
        MethodArgumentNotValidException exception = createValidationException();
        
        // When
        ProblemDetail problem = handler.handleValidationException(exception);
        
        // Then
        assertEquals(400, problem.getStatus());
        assertEquals("Validation failed", problem.getTitle());
        assertNotNull(problem.getProperties());
        
        @SuppressWarnings("unchecked")
        List<Object> errors = (List<Object>) problem.getProperties().get("errors");
        assertFalse(errors.isEmpty());
    }

    @Test
    @DisplayName("should return 400 for IllegalArgumentException")
    void shouldReturn400ForIllegalArgument() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");
        
        // When
        ProblemDetail problem = handler.handleBusinessException(exception);
        
        // Then
        assertEquals(400, problem.getStatus());
        assertEquals("Bad Request", problem.getTitle());
        assertEquals("Invalid input", problem.getDetail());
    }

    // === Helper Methods ===
    
    private MethodArgumentNotValidException createValidationException() {
        // ... create mock validation exception
    }
}
```

---

## Regras de Geração

### 1. Cobertura de Testes

Para cada classe, gere testes para:

#### Domain (Agregados)
- ✅ Construção válida
- ✅ Validação de campos obrigatórios (nulls)
- ✅ Validação de regras de negócio (valores inválidos)
- ✅ Métodos de negócio (comportamento esperado)
- ✅ Métodos de negócio (exceções esperadas)
- ✅ Transições de estado
- ✅ Invariantes do domínio

#### Domain (Value Objects)
- ✅ Construção válida
- ✅ Validação de formato
- ✅ Imutabilidade (se aplicável)
- ✅ Métodos utilitários (format, parse, etc.)
- ✅ Equals/HashCode (se relevante)

#### Infrastructure (Mappers)
- ✅ Entity → DTO
- ✅ DTO → Entity
- ✅ Roundtrip (Entity → DTO → Entity)
- ✅ Campos especiais (nulls, blanks, valores default)
- ✅ Valores realistas (Faker + Instancio)

#### Infrastructure (Repositories)
- ✅ Save and retrieve
- ✅ Update
- ✅ Delete
- ✅ Custom queries
- ✅ Not found scenarios

#### Web (Exception Handlers)
- ✅ Cada tipo de exceção mapeada
- ✅ Estrutura de resposta (RFC 7807)
- ✅ Status HTTP correto
- ✅ Mensagens de erro

---

### 2. Patterns Obrigatórios

#### Use Instancio para Fixtures
```java
// ✅ BOM
{Entity} entity = Instancio.of({Entity}.class)
    .set(field({Entity}::{criticalField}), knownValue)
    .create();

// ❌ RUIM
{Entity} entity = new {Entity}(
    "value1", "value2", "value3", "value4", "value5", 
    "value6", "value7", "value8", "value9", "value10"
);
```

#### Use JavaFaker para Valores Realistas
```java
// ✅ BOM
String name = faker.name().fullName();
String email = faker.internet().emailAddress();
String street = faker.address().streetAddress();

// ❌ RUIM
String name = "John Doe";
String email = "test@example.com";
String street = "123 Main St";
```

#### Use Method References
```java
// ✅ BOM
.set(field(CustomerDto::name), "John")

// ❌ RUIM
.set(Select.field("name"), "John")
```

#### Generate Constrained Values
```java
// ✅ BOM - valores variados com constraints
BigDecimal amount = Instancio.of(BigDecimal.class)
    .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
        .min(BigDecimal.ONE)
        .max(new BigDecimal("10000.00")))
    .create();

// ❌ RUIM - sempre mesmo valor
BigDecimal amount = new BigDecimal("5000.00");
```

---

### 3. Estrutura de Assertions

#### Use AssertJ para Collections e Objects
```java
// ✅ BOM
assertThat(customers)
    .hasSize(2)
    .extracting(Customer::getName)
    .containsExactly("Alice", "Bob");

// ⚠️ ACEITÁVEL mas menos expressivo
assertEquals(2, customers.size());
assertEquals("Alice", customers.get(0).getName());
```

#### Use JUnit Assertions para Primitivos
```java
// ✅ BOM
assertEquals(expected, actual);
assertTrue(condition);
assertThrows(Exception.class, () -> method());

// ✅ TAMBÉM BOM (AssertJ)
assertThat(actual).isEqualTo(expected);
```

---

### 4. Nomenclatura e Documentação

#### JavaDoc da Classe
```java
/**
 * Unit tests for {ClassName}
 * 
 * Tests:
 * - {Categoria1}: {descrição}
 * - {Categoria2}: {descrição}
 * - {Categoria3}: {descrição}
 * 
 * References:
 * - {doc}.md: {contexto}
 * - {ClassName}.java: Implementation
 */
```

#### DisplayName Descritivo
```java
@DisplayName("should reject negative credit limit when creating customer")
void shouldRejectNegativeCreditLimitWhenCreating() { ... }

// Ou em português
@DisplayName("deve rejeitar limite de crédito negativo ao criar cliente")
void deveRejeitarLimiteNegativo() { ... }
```

---

## Checklist de Geração

Ao gerar testes para uma classe, verifique:

- [ ] Imports corretos (JUnit 5, Mockito, Instancio, Faker)
- [ ] JavaDoc da classe de teste
- [ ] `@DisplayName` na classe
- [ ] `@BeforeEach` para setup comum
- [ ] Testes de construção/validação
- [ ] Testes de comportamento de negócio
- [ ] Testes de exceções esperadas
- [ ] Helper methods para criação de fixtures
- [ ] Uso de Instancio com method references
- [ ] Uso de JavaFaker para dados realistas
- [ ] Uso de generators para constraints
- [ ] Assertions claras e completas
- [ ] Coverage de branches principais (> 80%)

---

## Exemplos Reais do Projeto

### ✅ Bons Exemplos
- `CustomerTest.java` - Testes completos de agregado
- `CustomerMapperTest.java` - Mapper com Faker + Instancio
- `MoneyTest.java` - Value Object com validações
- `MongoCustomerRepositoryTest.java` - Repository com Testcontainers
- `GlobalExceptionHandlerTest.java` - Exception handler com ProblemDetail

### 📚 Documentação Relacionada
- [testing.md](testing.md) - Estratégia geral de testes
- [instancio-best-practices.md](instancio-best-practices.md) - Uso de Instancio
- [architecture.md](architecture.md) - Arquitetura DDD
- [code-examples.md](code-examples.md) - Padrões do projeto

---

## Prompt para IA Agent

Use o seguinte prompt ao solicitar geração de testes:

```
Gere testes unitários para a classe {ClassName} seguindo:

1. Framework: JUnit 5, Mockito, Instancio, JavaFaker
2. Padrões: Use method references, generators com constraints, JavaFaker para dados realistas
3. Cobertura: Teste construção, validações, regras de negócio, exceções
4. Estrutura: JavaDoc, @DisplayName, @BeforeEach, helper methods
5. Referência: Siga exemplos de CustomerMapperTest.java e CustomerTest.java

Classe a testar:
{código da classe}

Gere os testes completos seguindo as diretrizes em docs/test-generation-agent.md
```

---

**Última atualização:** 14 de dezembro de 2025  
**Mantido por:** Time de Arquitetura
