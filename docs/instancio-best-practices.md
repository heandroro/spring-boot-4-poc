# Instancio - Boas Práticas em Testes Unitários

## Índice
1. [Visão Geral](#visão-geral)
2. [Por Que Usar Instancio](#por-que-usar-instancio)
3. [Configuração Básica](#configuração-básica)
4. [Padrões Recomendados](#padrões-recomendados)
5. [Integração com JavaFaker](#integração-com-javafaker)
6. [Casos de Uso](#casos-de-uso)
7. [Anti-Padrões](#anti-padrões)
8. [Referências](#referências)

---

## Visão Geral

**Instancio** é uma biblioteca Java para geração automática de dados de teste. Ela elimina o boilerplate de criação manual de objetos e permite controle fino sobre campos específicos quando necessário.

### Benefícios Principais
- ✅ Reduz código repetitivo (boilerplate)
- ✅ Gera dados realistas e variados
- ✅ Type-safe com method references
- ✅ Facilita refatoração (detecta mudanças em compile-time)
- ✅ Foca nos dados relevantes para cada teste

---

## Por Que Usar Instancio

### Antes (Manual)
```java
@Test
void testMapping() {
    CustomerDto dto = new CustomerDto(
        null,
        "John Doe",
        "john@example.com",
        "123 Main St",
        "Springfield",
        "IL",
        "62701",
        "United States",
        new BigDecimal("5000.00"),
        new BigDecimal("5000.00"),
        "ACTIVE"
    );
    
    Customer entity = mapper.toEntity(dto);
    
    assertEquals("John Doe", entity.getName());
    assertEquals("john@example.com", entity.getEmail().toString());
}
```

**Problemas:**
- 🔴 11 parâmetros, maioria irrelevante para o teste
- 🔴 Frágil: quebra se adicionar/remover campos
- 🔴 Dificulta leitura: qual campo é importante?
- 🔴 Valores fixos não testam edge cases

### Depois (Instancio)
```java
@Test
void testMapping() {
    CustomerDto dto = Instancio.of(CustomerDto.class)
        .set(field(CustomerDto::name), "John Doe")
        .set(field(CustomerDto::email), "john@example.com")
        .create();
    
    Customer entity = mapper.toEntity(dto);
    
    assertEquals("John Doe", entity.getName());
    assertEquals("john@example.com", entity.getEmail().toString());
}
```

**Benefícios:**
- ✅ Foco nos dados relevantes (name, email)
- ✅ Outros campos gerados automaticamente
- ✅ Type-safe: refactoring seguro
- ✅ Resiliente a mudanças na estrutura

---

## Configuração Básica

### Dependência (Gradle)
```kotlin
testImplementation("org.instancio:instancio-junit:4.0.0")
```

### Imports Recomendados
```java
import org.instancio.Instancio;
import static org.instancio.Select.field;
import static org.instancio.Select.all;
```

---

## Padrões Recomendados

### 1. Use Method References (Não Strings)

#### ❌ Evite
```java
.set(Select.field("name"), "John Doe")
.set(Select.field("email"), "john@example.com")
```

**Problemas:**
- Não detecta erros de digitação em compile-time
- Quebra silenciosamente em refatorações
- Não funciona com ofuscação/minificação

#### ✅ Recomendado
```java
.set(field(CustomerDto::name), "John Doe")
.set(field(CustomerDto::email), "john@example.com")
```

**Vantagens:**
- Type-safe
- IDE refactoring automático
- Erros detectados em compile-time

---

### 2. Use Generators para Constraints

#### ❌ Evite Valores Fixos
```java
.set(field(CustomerDto::creditLimit), new BigDecimal("5000.00"))
.set(field(CustomerDto::availableCredit), new BigDecimal("5000.00"))
```

**Problema:** Não testa variabilidade; sempre mesmos valores.

#### ✅ Use Generators com Constraints
```java
BigDecimal creditLimit = Instancio.of(BigDecimal.class)
    .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
        .min(BigDecimal.ONE)
        .max(new BigDecimal("10000.00")))
    .create();

CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::creditLimit), creditLimit)
    .set(field(CustomerDto::availableCredit), creditLimit)
    .create();
```

**Vantagens:**
- Testa com diferentes valores a cada execução
- Garante constraints do domínio (> 0, ≤ 10k)
- Detecta bugs que valores fixos mascarariam

---

### 3. Mantenha Invariantes do Domínio

#### ❌ Evite Dados Inconsistentes
```java
// ❌ availableCredit > creditLimit (inválido!)
.set(field(CustomerDto::creditLimit), new BigDecimal("1000.00"))
.set(field(CustomerDto::availableCredit), new BigDecimal("5000.00"))
```

#### ✅ Respeite Regras de Negócio
```java
// ✅ Cliente novo: available = limit
BigDecimal creditLimit = generatePositiveMoney();

CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::creditLimit), creditLimit)
    .set(field(CustomerDto::availableCredit), creditLimit)
    .create();
```

```java
// ✅ Cliente com crédito usado: available < limit
BigDecimal creditLimit = new BigDecimal("5000.00");
BigDecimal usedCredit = new BigDecimal("2000.00");
BigDecimal availableCredit = creditLimit.subtract(usedCredit);

CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::creditLimit), creditLimit)
    .set(field(CustomerDto::availableCredit), availableCredit)
    .create();
```

---

## Integração com JavaFaker

Combine Instancio (estrutura) com JavaFaker (valores realistas):

### Exemplo Completo
```java
@Test
@DisplayName("should map realistic data generated by Faker + Instancio")
void testFakerInstancioCombined() {
    Faker faker = new Faker();
    
    // Generate constrained monetary value
    BigDecimal creditLimit = Instancio.of(BigDecimal.class)
        .generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
            .min(BigDecimal.ONE)
            .max(new BigDecimal("10000.00")))
        .create();

    // Build DTO with realistic + controlled data
    CustomerDto dto = Instancio.of(CustomerDto.class)
        .set(field(CustomerDto::id), null)
        .set(field(CustomerDto::name), faker.name().fullName())
        .set(field(CustomerDto::email), faker.internet().emailAddress())
        .set(field(CustomerDto::street), faker.address().streetAddress())
        .set(field(CustomerDto::city), faker.address().city())
        .set(field(CustomerDto::state), faker.address().stateAbbr())
        .set(field(CustomerDto::postalCode), faker.address().zipCode())
        .set(field(CustomerDto::country), "United States")
        .set(field(CustomerDto::creditLimit), creditLimit)
        .set(field(CustomerDto::availableCredit), creditLimit)
        .set(field(CustomerDto::status), "ACTIVE")
        .create();

    Customer entity = mapper.toEntity(dto);

    // Assertions validam mapeamento, não valores específicos
    assertEquals(dto.name(), entity.getName());
    assertEquals(dto.email(), entity.getEmail().toString());
    assertEquals(dto.creditLimit(), entity.getCreditLimit().amount());
    
    // Valida constraints
    assertTrue(entity.getCreditLimit().amount().compareTo(BigDecimal.ZERO) > 0);
}
```

### Quando Usar Faker vs Instancio
| Aspecto | Instancio | JavaFaker |
|---------|-----------|-----------|
| **Estrutura** | ✅ Melhor escolha | ❌ Não aplicável |
| **Strings realistas** | ⚠️ Aleatórias simples | ✅ Nome, email, endereço |
| **Números com constraints** | ✅ Generators | ⚠️ Menos controle |
| **Datas** | ⚠️ Aleatórias | ✅ Períodos específicos |
| **Type-safety** | ✅ Method references | ❌ Strings/manual |

---

## Casos de Uso

### 1. Teste de Mapeamento (Mapper)
**Objetivo:** Verificar conversão entre DTO ↔ Entity

```java
@Test
void shouldMapDtoToEntity() {
    CustomerDto dto = Instancio.of(CustomerDto.class)
        .set(field(CustomerDto::id), null)
        .set(field(CustomerDto::status), "ACTIVE")
        .create();
    
    Customer entity = mapper.toEntity(dto);
    
    assertEquals(dto.name(), entity.getName());
    assertEquals(dto.creditLimit(), entity.getCreditLimit().amount());
}
```

**Por quê usar Instancio aqui?**
- Foca na lógica de mapeamento, não em valores específicos
- Testa com dados variados (detecta falhas em edge cases)

---

### 2. Teste de Validação (Bean Validation)
**Objetivo:** Garantir que campos obrigatórios sejam validados

```java
@Test
void shouldRejectBlankEmail() {
    CustomerDto dto = Instancio.of(CustomerDto.class)
        .set(field(CustomerDto::email), "   ")  // Campo específico: blank
        .create();
    
    Set<ConstraintViolation<CustomerDto>> violations = validator.validate(dto);
    
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
}
```

**Por quê usar Instancio aqui?**
- Outros campos preenchidos automaticamente
- Foco no campo sob teste (email)

---

### 3. Teste de Normalização (Address.state)
**Objetivo:** Validar que `state` em branco vira `null`

```java
@Test
void shouldNormalizeBlankStateToNull() {
    CustomerDto dto = Instancio.of(CustomerDto.class)
        .set(field(CustomerDto::state), "   ")  // Específico: blank
        .create();
    
    Customer entity = mapper.toEntity(dto);
    
    assertNull(entity.getAddress().state());
}
```

---

### 4. Testes Parametrizados
**Objetivo:** Testar múltiplos cenários

```java
@ParameterizedTest
@ValueSource(strings = {"ACTIVE", "INACTIVE", "SUSPENDED"})
void shouldMapAllStatuses(String status) {
    CustomerDto dto = Instancio.of(CustomerDto.class)
        .set(field(CustomerDto::status), status)
        .create();
    
    Customer entity = mapper.toEntity(dto);
    
    assertEquals(status, entity.getStatus().name());
}
```

---

## Anti-Padrões

### ❌ 1. Gerar Dados Irrelevantes Manualmente
```java
// ❌ Ruim: 90% dos dados não importam para o teste
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::name), "John")
    .set(field(CustomerDto::email), "john@example.com")
    .set(field(CustomerDto::street), "123 Main St")  // irrelevante
    .set(field(CustomerDto::city), "Springfield")    // irrelevante
    .set(field(CustomerDto::state), "IL")            // irrelevante
    .set(field(CustomerDto::postalCode), "62701")    // irrelevante
    .set(field(CustomerDto::country), "USA")         // irrelevante
    .create();
```

✅ **Solução:** Deixe Instancio gerar campos irrelevantes.

---

### ❌ 2. Usar Strings ao Invés de Method References
```java
// ❌ Ruim: quebra em refatoração
.set(Select.field("customerName"), "John")
```

✅ **Solução:** `field(CustomerDto::name)`

---

### ❌ 3. Valores Fixos Onde Variabilidade é Importante
```java
// ❌ Ruim: sempre mesmos valores
.set(field(CustomerDto::creditLimit), new BigDecimal("5000.00"))
```

✅ **Solução:** Use generators para constraints:
```java
.generate(all(BigDecimal.class), gen -> gen.math().bigDecimal()
    .min(BigDecimal.ONE).max(new BigDecimal("10000.00")))
```

---

### ❌ 4. Duplicar Lógica de Builder
```java
// ❌ Ruim: replicando factory method
CustomerDto dto = Instancio.of(CustomerDto.class)
    .set(field(CustomerDto::id), null)
    .set(field(CustomerDto::creditLimit), limit)
    .set(field(CustomerDto::availableCredit), limit)
    .set(field(CustomerDto::status), "ACTIVE")
    .set(field(CustomerDto::country), "United States")
    .create();

// Repetido em 10 testes...
```

✅ **Solução:** Crie métodos auxiliares:
```java
private CustomerDto createNewCustomerDto(BigDecimal creditLimit) {
    return Instancio.of(CustomerDto.class)
        .set(field(CustomerDto::id), null)
        .set(field(CustomerDto::creditLimit), creditLimit)
        .set(field(CustomerDto::availableCredit), creditLimit)
        .set(field(CustomerDto::status), "ACTIVE")
        .create();
}

@Test
void test() {
    CustomerDto dto = createNewCustomerDto(new BigDecimal("5000.00"));
    // ...
}
```

---

## Referências

### Documentação Oficial
- [Instancio User Guide](https://www.instancio.org/user-guide/)
- [Instancio API Reference](https://javadoc.io/doc/org.instancio/instancio-core/latest/index.html)

### Exemplos no Projeto
- `CustomerMapperTest.java` - Mapeamento DTO ↔ Entity
- `CustomerRepositoryTest.java` - Testes de persistência
- `DomainEventPublisherTest.java` - Eventos de domínio

### Boas Práticas Relacionadas
- [testing.md](testing.md) - Estratégia geral de testes
- [java-records-best-practices.md](java-records-best-practices.md) - Uso de Records com Instancio
- [code-examples.md](code-examples.md) - Padrões do projeto

---

## Resumo Rápido

### ✅ Faça
- Use method references: `field(CustomerDto::name)`
- Combine com JavaFaker para dados realistas
- Use generators para constraints numéricos
- Foque apenas nos campos relevantes para o teste
- Respeite invariantes do domínio
- Crie métodos auxiliares para cenários recorrentes

### ❌ Evite
- Strings literais em `Select.field("name")`
- Valores fixos onde variabilidade importa
- Gerar manualmente campos irrelevantes
- Violar regras de negócio (ex: availableCredit > creditLimit)
- Duplicar código de setup entre testes

---

**Última atualização:** 14 de dezembro de 2025  
**Versão do Instancio:** 4.0.0
