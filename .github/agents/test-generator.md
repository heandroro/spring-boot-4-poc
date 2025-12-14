# Test Generator Agent

Agente especializado em geração de testes unitários seguindo os padrões do projeto.

## Ativação

Este agente é ativado quando:
- Desenvolvedor solicita geração de testes para uma classe
- É criado um arquivo `*Test.java` novo
- Comentário menciona "gerar testes" ou "create tests"
- Chat solicita: `@workspace gere testes para {ClassName}`

## Responsabilidades

1. ✅ Gerar testes unitários completos e consistentes
2. ✅ Seguir padrões de Instancio + JavaFaker
3. ✅ Garantir cobertura adequada por camada
4. ✅ Aplicar method references (type-safe)
5. ✅ Usar generators com constraints para valores numéricos

## Stack de Testes

- **Framework**: JUnit 6 (Jupiter)
- **Mocking**: Mockito
- **Fixtures**: Instancio 4.0.0 + JavaFaker
- **Containers**: Testcontainers (MongoDB)
- **Assertions**: JUnit Assertions + AssertJ

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

// Mockito (quando necessário)
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
```

## Regras Obrigatórias

### 1. Use Method References (NUNCA Strings)

```java
// ✅ CORRETO - Type-safe
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::name), faker.name().fullName())
    .set(field(CustomerDto::email), faker.internet().emailAddress())
    .create();

// ❌ ERRADO - Quebra em refatoração
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(Select.field("name"), "John Doe")
    .set(Select.field("email"), "john@example.com")
    .create();
```

### 2. Generate Valores com Constraints

```java
// ✅ CORRETO - Valores aleatórios com regras
BigDecimal creditLimit = Instancio.of(BigDecimal.class)
    .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
        .min(BigDecimal.ONE)
        .max(new BigDecimal("10000.00")))
    .create();

// ❌ ERRADO - Sempre mesmo valor
BigDecimal creditLimit = new BigDecimal("5000.00");
```

### 3. Combine Instancio + JavaFaker

```java
// ✅ CORRETO - Dados realistas + controle
Faker faker = new Faker();

CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::name), faker.name().fullName())
    .set(field(CustomerDto::email), faker.internet().emailAddress())
    .set(field(CustomerDto::street), faker.address().streetAddress())
    .set(field(CustomerDto::city), faker.address().city())
    .set(field(CustomerDto::creditLimit), creditLimit)
    .create();
```

### 4. Estrutura de Teste Padrão

```java
@DisplayName("{ClassName} Tests")
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

## Cobertura por Camada

### Domain (Agregados e Value Objects)

Gere testes para:
- ✅ Construção válida
- ✅ Validação de campos obrigatórios (nulls)
- ✅ Validação de regras de negócio (valores inválidos)
- ✅ Métodos de negócio (comportamento esperado)
- ✅ Métodos de negócio (exceções esperadas)
- ✅ Transições de estado
- ✅ Invariantes do domínio

**Exemplo:**
```java
@Test
@DisplayName("should use credit when customer is active")
void shouldUseCreditWhenActive() {
    // Given
    Customer customer = Customer.create(
        faker.name().fullName(),
        new Email(faker.internet().emailAddress()),
        createAddress(),
        Money.of(generatePositiveMoney())
    );
    Money purchase = Money.of(new BigDecimal("200.00"));
    
    // When
    customer.useCredit(purchase);
    
    // Then
    assertTrue(customer.getAvailableCredit().amount()
        .compareTo(customer.getCreditLimit().amount()) < 0);
}
```

### Infrastructure (Mappers)

Gere testes para:
- ✅ Entity → DTO
- ✅ DTO → Entity
- ✅ Roundtrip (Entity → DTO → Entity)
- ✅ Campos especiais (nulls, blanks, defaults)
- ✅ Dados realistas (Faker + Instancio)

**Exemplo:**
```java
@Test
@DisplayName("should map realistic data")
void shouldMapRealisticData() {
    // Given
    BigDecimal amount = generateConstrainedMoney();
    
    CustomerDto dto = Instancio.of(CustomerDto.class)
        .set(field(CustomerDto::name), faker.name().fullName())
        .set(field(CustomerDto::creditLimit), amount)
        .set(field(CustomerDto::availableCredit), amount)
        .create();
    
    // When
    Customer entity = mapper.toEntity(dto);
    
    // Then
    assertEquals(dto.name(), entity.getName());
    assertEquals(dto.creditLimit(), entity.getCreditLimit().amount());
}
```

### Infrastructure (Repositories)

Gere testes para:
- ✅ Save and retrieve
- ✅ Update
- ✅ Delete
- ✅ Custom queries
- ✅ Not found scenarios

Use `@DataMongoTest` + Testcontainers.

### Web (Exception Handlers)

Gere testes para:
- ✅ Cada tipo de exceção mapeada
- ✅ Estrutura RFC 7807 (ProblemDetail)
- ✅ Status HTTP correto
- ✅ Mensagens de erro estruturadas

### Web (Controllers com MockMvc)

**Template: Controller Test com MockMvc**

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
 * Unit tests for {Controller}
 * 
 * Uses MockMvc to test REST endpoints without starting full server.
 * Service layer is mocked to isolate controller logic.
 * 
 * @Import(GlobalExceptionHandler.class) includes the @ControllerAdvice
 * to test error handling with RFC 7807 ProblemDetail responses.
 */
@WebMvcTest({Controller}.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("{Controller} Tests")
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
            .andExpect(status().isNotFound());

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
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Business rule violated"));

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

**Alternativa sem @Import:**
```java
// Se não importar @ControllerAdvice, exceções não tratadas retornam 500
// Use apenas se quiser testar o controller isoladamente
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {
    // Testa apenas lógica do controller, não error handling
}
```

**Cobertura de Testes para Controllers:**
- ✅ POST (201 Created) - Criação com sucesso
- ✅ GET by ID (200 OK) - Busca com sucesso
- ✅ GET by ID (404 Not Found) - Entidade não existe
- ✅ POST (400 Bad Request) - Validação falha
- ✅ POST (400 Bad Request) - Regra de negócio violada
- ✅ PUT (200 OK) - Atualização com sucesso
- ✅ DELETE (204 No Content) - Deleção com sucesso
- ✅ GET list (200 OK) - Listagem com paginação

**Assertions Importantes:**
- Status HTTP correto
- Headers (Location, Content-Type)
- Estrutura JSON (jsonPath)
- RFC 7807 para erros (ProblemDetail)
- Verificação de chamadas ao service (Mockito verify)

## Anti-Padrões

### ❌ Não Faça

```java
// ❌ Strings literais
.set(Select.field("name"), "John")

// ❌ Valores fixos repetidos
.set(field(Dto::amount), new BigDecimal("1000.00"))

// ❌ Gerar dados irrelevantes manualmente
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::street), "123 Main St")    // irrelevante
    .set(field(CustomerDto::city), "Springfield")      // irrelevante
    .set(field(CustomerDto::state), "IL")              // irrelevante
    .create();

// ❌ Violar invariantes do domínio
.set(field(Dto::availableCredit), new BigDecimal("9000.00"))
.set(field(Dto::creditLimit), new BigDecimal("5000.00"))  // INVÁLIDO!
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

// ✅ Deixe Instancio gerar campos irrelevantes
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::name), faker.name().fullName())
    .create();  // Outros campos gerados automaticamente

// ✅ Respeite invariantes
BigDecimal creditLimit = generatePositiveMoney();
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::creditLimit), creditLimit)
    .set(field(CustomerDto::availableCredit), creditLimit)
    .create();
```

## Helper Methods Reutilizáveis

```java
/**
 * Gera BigDecimal positivo com constraints
 */
private BigDecimal generatePositiveMoney() {
    return Instancio.of(BigDecimal.class)
        .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
            .min(BigDecimal.ONE)
            .max(new BigDecimal("10000.00")))
        .create();
}

/**
 * Cria Address com dados realistas
 */
private Address createAddress() {
    return Address.of(
        faker.address().streetAddress(),
        faker.address().city(),
        faker.address().stateAbbr(),
        faker.address().zipCode()
    );
}

/**
 * Cria Customer válido com dados aleatórios
 */
private Customer createValidCustomer() {
    return Customer.create(
        faker.name().fullName(),
        new Email(faker.internet().emailAddress()),
        createAddress(),
        Money.of(generatePositiveMoney())
    );
}
```

## Checklist de Geração

Antes de finalizar testes gerados, verifique:

- [ ] Imports corretos (JUnit 6, Instancio, Faker)
- [ ] JavaDoc da classe de teste
- [ ] `@DisplayName` descritivo
- [ ] `@BeforeEach` para setup do Faker
- [ ] Method references (não strings)
- [ ] Generators para BigDecimal
- [ ] JavaFaker para dados realistas
- [ ] Helper methods para fixtures comuns
- [ ] Cobertura adequada da camada (80%+)
- [ ] Assertions claras e completas
- [ ] Respeito a invariantes do domínio

## Referências

- **Guia Completo**: [docs/test-generation-agent.md](../../docs/test-generation-agent.md)
- **Boas Práticas Instancio**: [docs/instancio-best-practices.md](../../docs/instancio-best-practices.md)
- **Exemplos Reais**:
  - `CustomerTest.java` - Domain aggregate
  - `CustomerMapperTest.java` - Mapper com Faker + Instancio
  - `MoneyTest.java` - Value Object
  - `MongoCustomerRepositoryTest.java` - Repository com Testcontainers
  - `GlobalExceptionHandlerTest.java` - Exception handler

## Prompt de Ativação

Use este prompt para ativar o agente:

```
@workspace gere testes unitários para {ClassName} seguindo .github/agents/test-generator.md
```

Ou simplesmente:

```
gere testes para {ClassName}
```

O agente aplicará automaticamente todos os padrões definidos neste documento.
