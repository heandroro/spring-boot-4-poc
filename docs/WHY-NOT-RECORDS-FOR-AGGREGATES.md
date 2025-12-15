## Por que não usar Record para Aggregate Roots?

### Resumo
- **Value Objects** (Money, Email, Address) = **Records** ✅
- **Aggregate Roots** (Customer, Order) = **Classes tradicionais** ✅
- **Entities** dentro do agregado = **Classes tradicionais** ✅

---

## 1. Diferenças Fundamentais

### Record (Java 16+)
```java
public record Money(BigDecimal amount, String currency) { }
```

**Características:**
- ✅ Imutável por padrão (`final` em todos os campos)
- ✅ Sem mutadores
- ✅ Sem estado persistido
- ✅ Sem lógica complexa
- ✅ Perfeito para Value Objects

**Limitações:**
- ❌ Não pode adicionar campos mutáveis
- ❌ Não pode ter métodos que mutem estado
- ❌ Não pode ter campos `transient` (bem, pode, mas não faz sentido)
- ❌ Não é adequado para agregados

### Classe Tradicional
```java
public class Customer {
    private Money availableCredit;
    
    public void useCredit(Money amount) {
        this.availableCredit = availableCredit.subtract(amount); // ✅ Mutation
    }
}
```

**Características:**
- ✅ Mutável (fields podem ser modificados)
- ✅ Múltiplos métodos com lógica complexa
- ✅ Pode ter campos `transient` para estado temporário
- ✅ Adequado para agregados com estado mutável

---

## 2. Por que Customer é Classe (Não Record)?

### Razão 1: Agregados precisam ser mutáveis

```java
// ❌ NÃO FUNCIONA com Record
public record Customer(Money availableCredit) {
    public void useCredit(Money amount) {
        // ❌ ERRO: availableCredit é final, não pode mutar!
        this.availableCredit = availableCredit.subtract(amount);
    }
}

// ✅ FUNCIONA com Classe
public class Customer {
    private Money availableCredit;
    
    public void useCredit(Money amount) {
        // ✅ OK: classe pode mutar seu estado
        this.availableCredit = availableCredit.subtract(amount);
    }
}
```

### Razão 2: Agregados têm múltiplas operações

```java
public class Customer {
    // ✅ Métodos de negócio ricos
    public void useCredit(Money amount) { ... }
    public void restoreCredit(Money amount) { ... }
    public void increaseCreditLimit(Money increase) { ... }
    public void setStatus(Status status) { ... }
    public boolean canPurchase(Money amount) { ... }
    public Money getUsedCredit() { ... }
    public double getCreditUtilizationPercentage() { ... }
    
    // Records não são bons para isso - muito simples demais
}
```

### Razão 3: Agregados precisam de eventos de domínio

```java
public class Customer {
    // ✅ Campo transient para eventos (não persistido)
    private transient List<DomainEvent> events = new ArrayList<>();
    
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }
}

// ❌ Records não suportam bem campos transient
// (tecnicamente suportam, mas não é a intenção do padrão)
```

### Razão 4: Constructor privado para Factory Method

```java
public class Customer {
    // ✅ Construtor privado
    private Customer() { }
    
    // ✅ Factory method como ÚNICO modo de criar
    public static Customer create(String name, Email email, ...) {
        Customer customer = new Customer();
        // ... validações e inicialização
        customer.events.add(new CustomerCreatedEvent(...));
        return customer;
    }
}

// ❌ Records têm constructor gerado automaticamente
// Não é possível torná-lo private para forçar factory method
```

---

## 3. Comparação Prática: Value Object vs Aggregate

### Value Object (DEVE ser Record)
```java
public record Money(BigDecimal amount, String currency) {
    public Money {
        // Validação no constructor
        if (amount.signum() < 0) 
            throw new IllegalArgumentException("Amount must be non-negative");
    }
    
    // ✅ Métodos imutáveis - retornam novo Money
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money multiply(int qty) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(qty)), this.currency);
    }
}
```

**Por que Record:**
- Sempre imutável ✅
- Nenhuma mudança de estado ✅
- Simples, sem lógica complexa ✅
- Usa apenas getters (amount(), currency()) ✅

### Aggregate Root (DEVE ser Classe)
```java
public class Customer {
    private String id;
    private String name;
    private Email email;
    private Money availableCredit;
    private Status status;
    private transient List<DomainEvent> events;
    
    // ✅ Factory method obrigatório
    public static Customer create(String name, Email email, ...) { }
    
    // ✅ Métodos que MUDAM estado
    public void useCredit(Money amount) {
        this.availableCredit = availableCredit.subtract(amount);
    }
    
    // ✅ Eventos de domínio (campo transient)
    public List<DomainEvent> pullEvents() { }
}
```

**Por que Classe:**
- Precisa ser mutável ✅
- Gerencia múltiplas operações ✅
- Usa factory method ✅
- Tem eventos de domínio ✅

---

## 4. Hierarquia DDD

```
┌─────────────────────────────────────┐
│          AGREGADO (Classe)          │ ← Customer
│  - Raiz do agregado                 │
│  - Gerencia entidades internas      │
│  - Estado MUTÁVEL                   │
│  - Factory method private           │
│  - Domain events (transient)        │
└─────────────────────────────────────┘
         │
         ├─── Entidades (Classe)
         │    - Objetos com ID
         │    - Mutáveis
         │    - Dentro do agregado
         │
         └─── Value Objects (Record) ✅
              - Money
              - Email
              - Address
              - Imutáveis
              - Sem identidade
              - Sem estado persistido
```

---

## 5. Quando usar cada um?

### Use **Record** para:
```java
✅ public record Email(String value) { }
✅ public record Money(BigDecimal amount, String currency) { }
✅ public record Address(String street, String city, String state, ...) { }
✅ public record OrderLineItem(Product product, int quantity, Money price) { }
```

**Características comuns:**
- Nenhuma lógica de negócio complexa
- Imutáveis e stateless
- Usados como campos em entidades/agregados
- Validação apenas no constructor

### Use **Classe** para:
```java
✅ public class Customer { }  // Agregado
✅ public class Order { }     // Agregado
✅ public class User { }      // Agregado
✅ public class Product { }   // Agregado
```

**Características comuns:**
- Gerenciam múltiplas operações de negócio
- Estado mutável
- Têm identidade (ID)
- Frequentemente agregados raiz
- Podem ter eventos de domínio

---

## 6. Exemplo Real: Order Aggregate

```java
// ✅ CORRETO
public class Order {
    private String id;
    private Email customerEmail;
    private List<OrderLineItem> items;  // Value Object Record
    private Money totalAmount;          // Value Object Record
    private OrderStatus status;
    private transient List<DomainEvent> events;
    
    public static Order create(Email customer, List<OrderLineItem> items) {
        Order order = new Order();
        order.customerEmail = customer;
        order.items = items;
        order.totalAmount = calculateTotal(items);
        order.status = OrderStatus.PENDING;
        order.events.add(new OrderCreatedEvent(...));
        return order;
    }
    
    public void addItem(OrderLineItem item) {
        this.items.add(item);  // ✅ Mutação permitida
        this.totalAmount = recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void place() {
        this.status = OrderStatus.PLACED;
        this.events.add(new OrderPlacedEvent(...));
    }
}

// ✅ OrderLineItem é Record (Value Object)
public record OrderLineItem(
    Product product,
    int quantity,
    Money unitPrice
) {
    public OrderLineItem {
        Objects.requireNonNull(product);
        Objects.requireNonNull(unitPrice);
        if (quantity <= 0) 
            throw new IllegalArgumentException("Quantity must be positive");
    }
    
    public Money getLineTotal() {
        return unitPrice.multiply(quantity);
    }
}
```

---

## 7. Conclusão

| Aspecto | Record | Classe |
|---------|--------|--------|
| **Value Objects** | ✅ IDEAL | ❌ Desnecessário |
| **Aggregate Roots** | ❌ INADEQUADO | ✅ IDEAL |
| **Entities** | ❌ INADEQUADO | ✅ IDEAL |
| **Imutabilidade** | ✅ Garantida | ⚠️ Responsabilidade do dev |
| **Métodos** | ✅ Poucos, simples | ✅ Muitos, complexos |
| **Mutação** | ❌ Não | ✅ Sim |
| **Eventos** | ❌ Não faz sentido | ✅ Suportado |

**Regra de ouro:**
> **Records são para VALUE OBJECTS** (Money, Email, Address, etc.)
> 
> **Classes são para AGREGADOS e ENTIDADES** (Customer, Order, Product, etc.)
