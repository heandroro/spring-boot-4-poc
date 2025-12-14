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

- **Framework**: JUnit 5 (Jupiter)
- **Mocking**: Mockito
- **Fixtures**: Instancio 4.0.0 + JavaFaker
- **Containers**: Testcontainers (MongoDB)
- **Assertions**: JUnit Assertions + AssertJ

## Imports Padrão

```java
// JUnit
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

- [ ] Imports corretos (JUnit 5, Instancio, Faker)
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
