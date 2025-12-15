# Unit Test Generator Agent

Agente especializado em geração de testes unitários rápidos e isolados usando Mockito + Instancio.

## Ativação

Este agente é ativado quando:
- Testar classes do **domínio** (agregados, value objects)
- Testar **mappers** (conversões DTO ↔ Entity)
- Testar **services** com dependências mockadas
- Comando: `@workspace gere teste unitário para {ClassName}`
- Comentário menciona "unit test" ou "teste unitário"

## Responsabilidades

1. ✅ Gerar testes unitários rápidos (ms, sem Spring context)
2. ✅ Usar Instancio + JavaFaker para fixtures realistas
3. ✅ Aplicar method references (type-safe)
4. ✅ Mockar dependências com Mockito
5. ✅ Garantir cobertura de 80%+ por classe

## Stack de Testes Unitários

- **Framework**: JUnit 6 (Jupiter)
- **Mocking**: Mockito (para services)
- **Fixtures**: Instancio 4.0.0 + JavaFaker
- **Assertions**: JUnit Assertions + AssertJ
- **Contexto**: **Sem Spring** (@Test apenas)

## Imports Padrão

```java
// JUnit 6
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Instancio
import org.instancio.Instancio;
import static org.instancio.Select.field;
import static org.instancio.Select.all;

// JavaFaker
import com.github.javafaker.Faker;

// Mockito (apenas para services)
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
```

## Regras Obrigatórias

### 1. Use Method References (NUNCA Strings)

```java
// ✅ CORRETO - Type-safe, refactoring-friendly
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::name), faker.name().fullName())
    .set(field(CustomerDto::email), faker.internet().emailAddress())
    .create();

// ❌ ERRADO - Strings quebram em refatoração
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(Select.field("name"), "John Doe")
    .set(Select.field("email"), "john@example.com")
    .create();
```

### 2. Generate Valores com Constraints

```java
// ✅ CORRETO - Valores aleatórios com regras de negócio
BigDecimal creditLimit = Instancio.of(BigDecimal.class)
    .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
        .min(BigDecimal.ONE)
        .max(new BigDecimal("10000.00")))
    .create();

// ❌ ERRADO - Sempre mesmo valor fixo
BigDecimal creditLimit = new BigDecimal("5000.00");
```

### 3. Combine Instancio + JavaFaker

```java
// ✅ CORRETO - Dados realistas que mudam a cada teste
Faker faker = new Faker();

CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::name), faker.name().fullName())
    .set(field(CustomerDto::email), faker.internet().emailAddress())
    .set(field(CustomerDto::street), faker.address().streetAddress())
    .set(field(CustomerDto::city), faker.address().city())
    .set(field(CustomerDto::creditLimit), generatePositiveMoney())
    .create();
```

### 4. Estrutura de Teste Padrão

```java
@DisplayName("{ClassName} Unit Tests")
class {ClassName}Test {

    private Faker faker;
    
    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    @DisplayName("should {expectedBehavior} when {condition}")
    void should{Behavior}When{Condition}() {
        // Given - Setup usando Instancio + Faker
        
        // When - Executa método testado
        
        // Then - Assertions
    }
    
    // Helper methods para fixtures comuns
    private {Entity} createValid{Entity}() {
        return Instancio.of({Entity}.class)
            .set(field({Entity}::{field}), validValue)
            .create();
    }
}
```

---

## Templates por Camada

### 1. Domain: Agregados

**Cobertura obrigatória:**
- ✅ Construção válida
- ✅ Validação de campos obrigatórios (nulls)
- ✅ Validação de regras de negócio
- ✅ Métodos de negócio (comportamento esperado)
- ✅ Métodos de negócio (exceções esperadas)
- ✅ Transições de estado
- ✅ Invariantes do domínio

**Template:**

```java
package com.example.poc.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import java.math.BigDecimal;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("{Aggregate} Unit Tests")
class {Aggregate}Test {

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    @DisplayName("should create valid aggregate")
    void shouldCreateValidAggregate() {
        // Given
        String name = faker.name().fullName();
        Email email = new Email(faker.internet().emailAddress());
        Address address = createAddress();
        Money creditLimit = Money.of(generatePositiveMoney());

        // When
        {Aggregate} aggregate = {Aggregate}.create(name, email, address, creditLimit);

        // Then
        assertNotNull(aggregate.getId());
        assertEquals(name, aggregate.getName());
        assertEquals(email, aggregate.getEmail());
        assertEquals(creditLimit, aggregate.getCreditLimit());
        assertEquals(creditLimit, aggregate.getAvailableCredit());
        assertTrue(aggregate.isActive());
    }

    @Test
    @DisplayName("should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
        // Given
        String nullName = null;

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            {Aggregate}.create(nullName, createEmail(), createAddress(), createMoney());
        });
    }

    @Test
    @DisplayName("should throw exception when name is blank")
    void shouldThrowExceptionWhenNameIsBlank() {
        // Given
        String blankName = "   ";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            {Aggregate}.create(blankName, createEmail(), createAddress(), createMoney());
        });
    }

    @Test
    @DisplayName("should apply business logic correctly")
    void shouldApplyBusinessLogic() {
        // Given
        {Aggregate} aggregate = createValidAggregate();
        Money purchase = Money.of(new BigDecimal("200.00"));

        // When
        aggregate.useCredit(purchase);

        // Then
        Money expectedAvailable = aggregate.getCreditLimit().subtract(purchase);
        assertEquals(expectedAvailable, aggregate.getAvailableCredit());
    }

    @Test
    @DisplayName("should throw exception when business rule violated")
    void shouldThrowExceptionWhenBusinessRuleViolated() {
        // Given
        {Aggregate} aggregate = createValidAggregate();
        Money excessivePurchase = aggregate.getCreditLimit().add(Money.of(BigDecimal.ONE));

        // When/Then
        assertThrows(InsufficientCreditException.class, () -> {
            aggregate.useCredit(excessivePurchase);
        });
    }

    @Test
    @DisplayName("should transition state correctly")
    void shouldTransitionStateCorrectly() {
        // Given
        {Aggregate} aggregate = createValidAggregate();
        assertTrue(aggregate.isActive());

        // When
        aggregate.deactivate();

        // Then
        assertFalse(aggregate.isActive());
    }

    // === Helper Methods ===

    private {Aggregate} createValidAggregate() {
        return {Aggregate}.create(
            faker.name().fullName(),
            new Email(faker.internet().emailAddress()),
            createAddress(),
            Money.of(generatePositiveMoney())
        );
    }

    private Email createEmail() {
        return new Email(faker.internet().emailAddress());
    }

    private Address createAddress() {
        return Address.of(
            faker.address().streetAddress(),
            faker.address().city(),
            faker.address().stateAbbr(),
            faker.address().zipCode()
        );
    }

    private Money createMoney() {
        return Money.of(generatePositiveMoney());
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

### 2. Domain: Value Objects

**Cobertura obrigatória:**
- ✅ Construção válida
- ✅ Validação de formato/constraints
- ✅ Igualdade (equals/hashCode)
- ✅ Imutabilidade
- ✅ Métodos de comparação/operações

**Template:**

```java
package com.example.poc.domain.vo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import java.math.BigDecimal;

import static org.instancio.Select.all;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("{ValueObject} Unit Tests")
class {ValueObject}Test {

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    @DisplayName("should create valid value object")
    void shouldCreateValidValueObject() {
        // Given
        String validValue = faker.internet().emailAddress();

        // When
        {ValueObject} vo = new {ValueObject}(validValue);

        // Then
        assertEquals(validValue, vo.value());
    }

    @Test
    @DisplayName("should throw exception for invalid format")
    void shouldThrowExceptionForInvalidFormat() {
        // Given
        String invalidValue = "invalid-format";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            new {ValueObject}(invalidValue);
        });
    }

    @Test
    @DisplayName("should throw exception when null")
    void shouldThrowExceptionWhenNull() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            new {ValueObject}(null);
        });
    }

    @Test
    @DisplayName("should be equal when values are equal")
    void shouldBeEqualWhenValuesAreEqual() {
        // Given
        String value = faker.internet().emailAddress();
        {ValueObject} vo1 = new {ValueObject}(value);
        {ValueObject} vo2 = new {ValueObject}(value);

        // Then
        assertEquals(vo1, vo2);
        assertEquals(vo1.hashCode(), vo2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when values differ")
    void shouldNotBeEqualWhenValuesDiffer() {
        // Given
        {ValueObject} vo1 = new {ValueObject}(faker.internet().emailAddress());
        {ValueObject} vo2 = new {ValueObject}(faker.internet().emailAddress());

        // Then
        assertNotEquals(vo1, vo2);
    }

    @Test
    @DisplayName("should perform operations correctly")
    void shouldPerformOperationsCorrectly() {
        // Given
        BigDecimal value1 = generatePositiveMoney();
        BigDecimal value2 = generatePositiveMoney();
        Money money1 = Money.of(value1);
        Money money2 = Money.of(value2);

        // When
        Money result = money1.add(money2);

        // Then
        assertEquals(value1.add(value2), result.amount());
    }

    // === Helper Methods ===

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

### 3. Infrastructure: Mappers

**Cobertura obrigatória:**
- ✅ Entity → DTO
- ✅ DTO → Entity
- ✅ Roundtrip (Entity → DTO → Entity)
- ✅ Campos null/blank/default
- ✅ Dados realistas (Faker + Instancio)

**Template:**

```java
package com.example.poc.infrastructure.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import java.math.BigDecimal;

import static org.instancio.Select.field;
import static org.instancio.Select.all;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("{Mapper} Unit Tests")
class {Mapper}Test {

    private {Mapper} mapper;
    private Faker faker;

    @BeforeEach
    void setUp() {
        mapper = new {Mapper}Impl();  // ou injeção se necessário
        faker = new Faker();
    }

    @Test
    @DisplayName("should map entity to DTO")
    void shouldMapEntityToDto() {
        // Given
        {Entity} entity = createValidEntity();

        // When
        {Dto} dto = mapper.toDto(entity);

        // Then
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getEmail().value(), dto.email());
        assertEquals(entity.getCreditLimit().amount(), dto.creditLimit());
    }

    @Test
    @DisplayName("should map DTO to entity")
    void shouldMapDtoToEntity() {
        // Given
        {Dto} dto = createValidDto();

        // When
        {Entity} entity = mapper.toEntity(dto);

        // Then
        assertEquals(dto.name(), entity.getName());
        assertEquals(dto.email(), entity.getEmail().value());
        assertEquals(dto.creditLimit(), entity.getCreditLimit().amount());
    }

    @Test
    @DisplayName("should roundtrip entity to DTO and back")
    void shouldRoundtripEntityToDtoAndBack() {
        // Given
        {Entity} original = createValidEntity();

        // When
        {Dto} dto = mapper.toDto(original);
        {Entity} roundtripped = mapper.toEntity(dto);

        // Then
        assertEquals(original.getName(), roundtripped.getName());
        assertEquals(original.getEmail(), roundtripped.getEmail());
        assertEquals(original.getCreditLimit(), roundtripped.getCreditLimit());
    }

    @Test
    @DisplayName("should map realistic data using Faker")
    void shouldMapRealisticDataUsingFaker() {
        // Given
        BigDecimal amount = generatePositiveMoney();
        
        {Dto} dto = Instancio.of({Dto}.class)
            .set(field({Dto}::name), faker.name().fullName())
            .set(field({Dto}::email), faker.internet().emailAddress())
            .set(field({Dto}::street), faker.address().streetAddress())
            .set(field({Dto}::city), faker.address().city())
            .set(field({Dto}::creditLimit), amount)
            .set(field({Dto}::availableCredit), amount)
            .create();

        // When
        {Entity} entity = mapper.toEntity(dto);

        // Then
        assertEquals(dto.name(), entity.getName());
        assertEquals(dto.email(), entity.getEmail().value());
        assertEquals(dto.creditLimit(), entity.getCreditLimit().amount());
    }

    @Test
    @DisplayName("should handle null fields correctly")
    void shouldHandleNullFieldsCorrectly() {
        // Given
        {Dto} dto = Instancio.of({Dto}.class)
            .set(field({Dto}::optionalField), null)
            .create();

        // When
        {Entity} entity = mapper.toEntity(dto);

        // Then
        assertNull(entity.getOptionalField());
    }

    @Test
    @DisplayName("should handle blank strings correctly")
    void shouldHandleBlankStringsCorrectly() {
        // Given
        {Dto} dto = Instancio.of({Dto}.class)
            .set(field({Dto}::optionalString), "   ")
            .create();

        // When
        {Entity} entity = mapper.toEntity(dto);

        // Then - Depende da estratégia (null ou trim)
        // Ajuste conforme comportamento esperado
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

    private {Dto} createValidDto() {
        BigDecimal amount = generatePositiveMoney();
        return Instancio.of({Dto}.class)
            .set(field({Dto}::name), faker.name().fullName())
            .set(field({Dto}::email), faker.internet().emailAddress())
            .set(field({Dto}::creditLimit), amount)
            .set(field({Dto}::availableCredit), amount)
            .create();
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

### 4. Application: Services (com Mockito)

**Cobertura obrigatória:**
- ✅ CRUD operations (sucesso)
- ✅ Business rules (exceções esperadas)
- ✅ Interação com dependências (verify)
- ✅ Edge cases (not found, duplicates)

**Template:**

```java
package com.example.poc.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.instancio.Instancio;
import com.github.javafaker.Faker;

import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("{Service} Unit Tests")
class {Service}Test {

    @Mock
    private {Repository} repository;

    @Mock
    private {Mapper} mapper;

    @InjectMocks
    private {Service} service;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    @DisplayName("should create entity successfully")
    void shouldCreateEntitySuccessfully() {
        // Given
        {Dto} dto = createValidDto();
        {Entity} entity = createValidEntity();
        
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        // When
        {Dto} result = service.create(dto);

        // Then
        assertNotNull(result);
        assertEquals(dto.name(), result.name());
        
        verify(mapper).toEntity(dto);
        verify(repository).save(entity);
        verify(mapper).toDto(entity);
    }

    @Test
    @DisplayName("should find entity by ID when exists")
    void shouldFindEntityByIdWhenExists() {
        // Given
        String id = "123";
        {Entity} entity = createValidEntity();
        {Dto} dto = createValidDto();
        
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // When
        Optional<{Dto}> result = service.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(dto.name(), result.get().name());
        
        verify(repository).findById(id);
        verify(mapper).toDto(entity);
    }

    @Test
    @DisplayName("should return empty when entity not found")
    void shouldReturnEmptyWhenEntityNotFound() {
        // Given
        String id = "nonexistent";
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<{Dto}> result = service.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findById(id);
        verify(mapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw exception when business rule violated")
    void shouldThrowExceptionWhenBusinessRuleViolated() {
        // Given
        {Dto} invalidDto = Instancio.of({Dto}.class)
            .set(field({Dto}::creditLimit), new BigDecimal("-100"))  // Violação
            .create();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.create(invalidDto);
        });
        
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("should update entity successfully")
    void shouldUpdateEntitySuccessfully() {
        // Given
        String id = "123";
        {Dto} updateDto = createValidDto();
        {Entity} existingEntity = createValidEntity();
        {Entity} updatedEntity = createValidEntity();
        
        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(mapper.toEntity(updateDto)).thenReturn(updatedEntity);
        when(repository.save(updatedEntity)).thenReturn(updatedEntity);
        when(mapper.toDto(updatedEntity)).thenReturn(updateDto);

        // When
        {Dto} result = service.update(id, updateDto);

        // Then
        assertNotNull(result);
        verify(repository).findById(id);
        verify(repository).save(updatedEntity);
    }

    @Test
    @DisplayName("should delete entity successfully")
    void shouldDeleteEntitySuccessfully() {
        // Given
        String id = "123";
        {Entity} entity = createValidEntity();
        
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        doNothing().when(repository).delete(entity);

        // When
        service.delete(id);

        // Then
        verify(repository).findById(id);
        verify(repository).delete(entity);
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

    private {Dto} createValidDto() {
        BigDecimal amount = generatePositiveMoney();
        return Instancio.of({Dto}.class)
            .set(field({Dto}::name), faker.name().fullName())
            .set(field({Dto}::email), faker.internet().emailAddress())
            .set(field({Dto}::creditLimit), amount)
            .create();
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

## Anti-Padrões

### ❌ Não Faça

```java
// ❌ Strings literais
.set(Select.field("name"), "John")

// ❌ Valores fixos repetidos
.set(field(Dto::amount), new BigDecimal("1000.00"))

// ❌ Violar invariantes do domínio
.set(field(Dto::availableCredit), new BigDecimal("9000.00"))
.set(field(Dto::creditLimit), new BigDecimal("5000.00"))  // INVÁLIDO!

// ❌ Usar @SpringBootTest para testes unitários
@SpringBootTest  // Desnecessário, lento!
class ServiceTest {
    @Autowired  // Não precisa!
    private Service service;
}
```

### ✅ Faça

```java
// ✅ Method references
.set(field(CustomerDto::name), faker.name().fullName())

// ✅ Generators com constraints
BigDecimal amount = Instancio.of(BigDecimal.class)
    .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
        .min(BigDecimal.ONE).max(new BigDecimal("10000.00")))
    .create();

// ✅ Respeite invariantes
BigDecimal creditLimit = generatePositiveMoney();
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::creditLimit), creditLimit)
    .set(field(CustomerDto::availableCredit), creditLimit)
    .create();

// ✅ Apenas @Test para testes unitários
@ExtendWith(MockitoExtension.class)  // Apenas se usar @Mock
class ServiceTest {
    @Mock
    private Repository repository;
    
    @InjectMocks
    private Service service;
}
```

---

## Checklist de Geração

Antes de finalizar testes unitários gerados, verifique:

- [ ] Imports corretos (JUnit 6, Instancio, Faker)
- [ ] **SEM @SpringBootTest** (testes unitários não usam Spring)
- [ ] `@ExtendWith(MockitoExtension.class)` apenas se usar @Mock
- [ ] JavaDoc da classe de teste
- [ ] `@DisplayName` descritivo
- [ ] `@BeforeEach` para setup do Faker
- [ ] Method references (não strings)
- [ ] Generators para BigDecimal
- [ ] JavaFaker para dados realistas
- [ ] Helper methods para fixtures comuns
- [ ] Cobertura 80%+ da classe
- [ ] Assertions claras e completas
- [ ] verify() para interações mockadas
- [ ] Respeito a invariantes do domínio

---

## Características de Testes Unitários

| Característica | Unit Test | Integration Test |
|----------------|-----------|------------------|
| **Velocidade** | Rápido (ms) | Lento (segundos) |
| **Contexto Spring** | ❌ Não | ✅ Sim |
| **Dependências** | Mockadas | Reais |
| **Annotations** | @Test, @Mock | @SpringBootTest |
| **Infraestrutura** | ❌ Não | Testcontainers |
| **Escopo** | Classe isolada | Sistema E2E |

---

## Referências

- **Boas Práticas Instancio**: [docs/instancio-best-practices.md](../../docs/instancio-best-practices.md)
- **Guia de Testes**: [docs/testing.md](../../docs/testing.md)
- **Integration Tests**: [agents/integration-test.md](integration-test.md)
- **Exemplos Reais**:
  - `CustomerTest.java` - Domain aggregate
  - `MoneyTest.java` - Value Object
  - `CustomerMapperTest.java` - Mapper com Faker + Instancio

## Prompt de Ativação

Use este prompt para ativar o agente:

```
@workspace gere teste unitário para {ClassName}
```

Ou simplesmente:

```
teste unitário para {ClassName}
```
