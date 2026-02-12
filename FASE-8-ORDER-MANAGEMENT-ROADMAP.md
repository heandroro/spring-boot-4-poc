# 🚀 Próximos Passos - Fase 8: Order Management

## 📌 Visão Geral

A **Fase 8** deve implementar o sistema de **Orders (Pedidos)** completo, com:
- Agregado Order com máquina de estados
- Integração com Product e Customer
- Fluxo de pedidos completo (criação, confirmação, envio, entrega)
- Eventos de domínio para rastreabilidade
- Testes unitários e de integração

---

## 📋 Checklist de Implementação para Fase 8

### 1. Domain Layer

#### Value Objects para Order
- [ ] `domain/vo/OrderNumber.java` - Número único do pedido
- [ ] `domain/vo/OrderItem.java` - Item do pedido com snapshot de preço
- [ ] `domain/vo/OrderTotals.java` - Subtotal, imposto, frete, total
- [ ] `domain/vo/OrderStatus.java` - Enum com estados: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
- [ ] `domain/vo/ShippingInfo.java` - Informações de envio
- [ ] `domain/vo/PaymentInfo.java` - Informações de pagamento

#### Agregado Order
- [ ] `domain/Order.java` - Agregado Root principal
  - Factory method: `Order.create(CustomerId, List<OrderItem>)`
  - Máquina de estados (transitions validadas)
  - Operações: `confirm()`, `ship()`, `deliver()`, `cancel()`
  - Cálculo de totais automático

#### Eventos de Domínio
- [ ] `domain/event/OrderCreatedEvent.java`
- [ ] `domain/event/OrderConfirmedEvent.java`
- [ ] `domain/event/OrderShippedEvent.java`
- [ ] `domain/event/OrderDeliveredEvent.java`
- [ ] `domain/event/OrderCancelledEvent.java`

#### Interfaces
- [ ] `domain/OrderRepository.java` - Contrato de persistência

---

### 2. Application Layer

#### Services
- [ ] `application/OrderService.java`
  - `create(OrderCreateDto)` - cria pedido
  - `findById(String)` - obtém pedido
  - `findByCustomerId(String)` - lista pedidos do cliente
  - `confirm(String orderId)` - confirma pedido
  - `ship(String orderId, ShippingInfo)` - envia pedido
  - `deliver(String orderId)` - marca como entregue
  - `cancel(String orderId, String reason)` - cancela

#### Validadores
- [ ] `application/OrderValidator.java`
  - Validar transições de estado
  - Validar estoque disponível
  - Validar dados do cliente

---

### 3. Infrastructure Layer

#### Repositories
- [ ] `infrastructure/persistence/MongoOrderRepository.java`
  - Spring Data MongoDB

#### Mappers
- [ ] `infrastructure/mapping/OrderMapper.java`
  - MapStruct para Order ↔ OrderDto
  - Mapeamento de Value Objects

#### Config
- [ ] Adicionar índices MongoDB em OrderRepository

---

### 4. Web Layer

#### DTOs
- [ ] `web/OrderDto.java` - Resposta
- [ ] `web/OrderCreateDto.java` - Request de criação
- [ ] `web/OrderItemDto.java`
- [ ] `web/OrderTotalsDto.java`
- [ ] `web/ShippingInfoDto.java`
- [ ] `web/PaymentInfoDto.java`

#### Controllers
- [ ] `web/OrderController.java`
  - POST /api/orders - criar
  - GET /api/orders/{id} - obter
  - GET /api/customers/{customerId}/orders - listar do cliente
  - PUT /api/orders/{id}/confirm - confirmar
  - PUT /api/orders/{id}/ship - enviar
  - PUT /api/orders/{id}/deliver - entregar
  - PUT /api/orders/{id}/cancel - cancelar

---

### 5. Test Layer

#### Unit Tests
- [ ] `test/domain/OrderTest.java` (20+ testes)
  - Criação com validações
  - Transições de estado
  - Cálculo de totais
  - Eventos de domínio

#### Integration Tests
- [ ] `integrationTest/infrastructure/persistence/MongoOrderRepositoryTest.java` (10+ testes)
  - CRUD
  - Queries por customer
  - Paginação

- [ ] `integrationTest/web/OrderControllerTest.java` (15+ testes)
  - Endpoints
  - Validações
  - Status HTTP

---

## 🎯 Integração com Fases Anteriores

### Referenciar Product
```java
public class Order {
    // Referência via customerId (não embedding direto)
    @DBRef
    private String customerId;
    
    // Snapshot do produto (imutável)
    private List<OrderItem> items;
}

// OrderItem contém ProductSnapshot
public record OrderItem(
    String productId,          // Referência
    ProductSnapshot snapshot,  // Snapshot para auditoria
    Integer quantity,
    Money unitPrice
) {}

public record ProductSnapshot(
    String name,
    String sku,
    BigDecimal price
) {}
```

### Referenciar Customer
```java
public class Order {
    @DBRef
    private String customerId;
    
    private CustomerSnapshot customerSnapshot;
}

public record CustomerSnapshot(
    String name,
    String email,
    String phone
) {}
```

---

## 🔐 Segurança para Fase 8

### Endpoints de Order

| Endpoint | Método | Roles | Lógica |
|----------|--------|-------|--------|
| POST /api/orders | POST | CUSTOMER | Criar pedido (seu próprio) |
| GET /api/orders/{id} | GET | CUSTOMER, MANAGER, ADMIN | Ver se seu, MANAGER/ADMIN veem todos |
| GET /api/customers/{cid}/orders | GET | MANAGER, ADMIN | Listar pedidos do cliente |
| PUT /api/orders/{id}/confirm | PUT | MANAGER, ADMIN | Confirmar pedido |
| PUT /api/orders/{id}/ship | PUT | MANAGER, ADMIN | Enviar pedido |
| PUT /api/orders/{id}/deliver | PUT | MANAGER, ADMIN | Entregar |
| PUT /api/orders/{id}/cancel | PUT | CUSTOMER (se pending), MANAGER, ADMIN | Cancelar |

---

## 📊 Máquina de Estados

```
    PENDING
      │
      ├─→ CONFIRMED
      │      │
      │      ├─→ SHIPPED
      │      │     │
      │      │     └─→ DELIVERED
      │      │
      │      └─→ CANCELLED
      │
      └─→ CANCELLED
```

**Transições válidas:**
- PENDING → CONFIRMED (confirma pedido)
- PENDING → CANCELLED (cancela sem confirmar)
- CONFIRMED → SHIPPED (prepara e envia)
- CONFIRMED → CANCELLED (cancela após confirmar)
- SHIPPED → DELIVERED (entrega)

---

## 💡 Exemplo de Implementação

### Domain Layer - Order Aggregate
```java
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String orderNumber;
    
    @DBRef
    private String customerId;
    
    private CustomerSnapshot customerSnapshot;
    private List<OrderItem> items;
    private OrderTotals totals;
    private OrderStatus status;
    private List<StatusTimeline> timeline;
    
    private transient List<DomainEvent> events;
    
    public static Order create(String customerId, CustomerSnapshot snap, List<OrderItem> items) {
        // Validações
        // Factory logic
        // Event add
    }
    
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only confirm pending orders");
        }
        this.status = OrderStatus.CONFIRMED;
        this.events.add(new OrderConfirmedEvent(...));
    }
    
    public void ship(ShippingInfo shipping) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Can only ship confirmed orders");
        }
        this.status = OrderStatus.SHIPPED;
        this.events.add(new OrderShippedEvent(...));
    }
}
```

### Application Layer - OrderService
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper mapper;
    
    public OrderDto create(OrderCreateDto dto) {
        // Validar produtos existem e estão em estoque
        // Validar cliente existe
        // Criar agregado
        // Persistir
        // Retornar DTO
    }
    
    public OrderDto confirm(String orderId) {
        // Encontrar pedido
        // Validar estado
        // Chamar confirm()
        // Publicar eventos
        // Persistir
    }
}
```

---

## 🧪 Testes para Fase 8

### Testes de Máquina de Estados
```java
@Test
void shouldTransitionFromPendingToConfirmed() {
    var order = Order.create(...);
    assertEquals(PENDING, order.getStatus());
    
    order.confirm();
    
    assertEquals(CONFIRMED, order.getStatus());
    assertTrue(order.pullEvents().stream().anyMatch(e -> e instanceof OrderConfirmedEvent));
}

@Test
void shouldThrowErrorWhenConfirmingShippedOrder() {
    var order = Order.create(...);
    order.confirm();
    order.ship(...);
    
    assertThrows(IllegalStateException.class, () -> order.confirm());
}
```

### Testes de Cálculo de Totais
```java
@Test
void shouldCalculateTotalsCorrectly() {
    var items = List.of(
        new OrderItem("p1", snapshot, 2, money(100)),
        new OrderItem("p2", snapshot, 1, money(50))
    );
    var order = Order.create(..., items);
    
    assertEquals(money(250), order.getTotals().subtotal());
    // Com impostos e frete
}
```

---

## 📚 Recursos de Referência

- **DDD Design**: https://martinfowler.com/bliki/DomainEvent.html
- **State Pattern**: https://refactoring.guru/design-patterns/state
- **MongoDB Transactions**: https://docs.mongodb.com/manual/core/transactions/
- **Spring Data Auditing**: Para timeline de mudanças

---

## 🎯 Cronograma Sugerido

| Dia | Atividade |
|-----|-----------|
| 1-2 | Domain Layer (Order aggregate + Value Objects) |
| 3 | Domain Events |
| 4 | Repository Interface |
| 5 | Application Layer (OrderService) |
| 6 | Infrastructure Layer (MongoDB + Mapper) |
| 7 | Web Layer (Controller + DTOs) |
| 8-9 | Tests (Unit + Integration) |
| 10 | Validação e Documentação |

---

## ✅ Checklist Final

Antes de considerar Fase 8 pronta:

- [ ] Código compila
- [ ] 30+ testes (unit + integration)
- [ ] Cobertura > 90%
- [ ] Máquina de estados validada
- [ ] Snapshots de dados funcionando
- [ ] Integração com Product e Customer OK
- [ ] @PreAuthorize implementado
- [ ] Documentação atualizada
- [ ] Sem Lombok
- [ ] Clean code

---

**Boa sorte! 🚀**

Próxima fase: Order Management (Fase 8)

