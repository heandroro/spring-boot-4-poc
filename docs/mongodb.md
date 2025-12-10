# MongoDB

## Database Design

### Collections

#### customers

Stores customer information with email indexing for fast lookups.

```javascript
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "address": {
    "street": "123 Main St",
    "city": "Springfield",
    "zipCode": "12345",
    "country": "USA"
  },
  "createdAt": ISODate("2025-12-01T10:30:00Z"),
  "updatedAt": ISODate("2025-12-09T14:20:00Z")
}
```

**Indexes:**
- `_id` (primary key)
- `email` (unique, for fast lookups and duplicate prevention)
- `createdAt` (for sorting by creation date)

#### orders

Stores order information with embedded items and status tracking.

```javascript
{
  "_id": ObjectId("507f1f77bcf86cd799439012"),
  "customerId": ObjectId("507f1f77bcf86cd799439011"),
  "items": [
    {
      "productId": ObjectId("507f1f77bcf86cd799439013"),
      "productName": "Widget",
      "quantity": 2,
      "unitPrice": 29.99
    }
  ],
  "status": "PENDING",
  "totalAmount": 59.98,
  "createdAt": ISODate("2025-12-09T10:30:00Z"),
  "updatedAt": ISODate("2025-12-09T10:35:00Z")
}
```

**Indexes:**
- `_id` (primary key)
- `customerId` (for querying customer's orders)
- `status` (for filtering by order status)
- `createdAt` (for sorting orders by date)

#### products

Stores product catalog.

```javascript
{
  "_id": ObjectId("507f1f77bcf86cd799439013"),
  "name": "Widget",
  "description": "A useful widget",
  "price": 29.99,
  "stock": 100,
  "category": "GADGETS",
  "createdAt": ISODate("2025-12-01T10:30:00Z")
}
```

**Indexes:**
- `_id` (primary key)
- `category` (for product filtering)
- `stock` (for availability queries)

## Entity Mapping

### Customer Entity

```java
@Document(collection = "customers")
public record Customer(
    @Id 
    String id,
    
    @Indexed(unique = true)
    @NotBlank
    String email,
    
    @NotBlank
    String name,
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    String phone,
    
    Address address,
    
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

// Embedded value object
public record Address(
    String street,
    String city,
    String zipCode,
    String country
) {}
```

### Order Entity

```java
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    
    @Indexed
    private String customerId;
    
    private List<OrderItem> items;
    
    @Indexed
    private OrderStatus status;
    
    private BigDecimal totalAmount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Rich behavior methods
    public void addItem(Product product, int quantity) {
        if (quantity <= 0) throw new InvalidQuantityException();
        
        items.stream()
            .filter(item -> item.productId().equals(product.id()))
            .findFirst()
            .ifPresentOrElse(
                item -> item.increaseQuantity(quantity),
                () -> items.add(new OrderItem(product.id(), product.name(), quantity, product.price()))
            );
        
        recalculateTotal();
    }
    
    public void transitionTo(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(status, newStatus);
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    private void recalculateTotal() {
        totalAmount = items.stream()
            .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

public record OrderItem(
    String productId,
    String productName,
    int quantity,
    BigDecimal unitPrice
) {
    public void increaseQuantity(int amount) {
        // Would need mutable wrapper or new record
    }
}
```

## Spring Data MongoDB Repository

### CrudRepository Pattern

```java
@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    
    // Derived query methods
    Optional<Customer> findByEmail(String email);
    List<Customer> findByNameContainingIgnoreCase(String name);
    List<Customer> findByCreatedAtAfter(LocalDateTime date);
    
    // Custom query
    @Query("{ 'email': ?0 }")
    Optional<Customer> findByEmailCustom(String email);
    
    // Parameterized query with @Param
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Customer> searchByName(String namePattern);
}

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    
    List<Order> findByCustomerId(String customerId);
    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    
    @Query("{ 'customerId': ?0, 'status': ?1 }")
    List<Order> findCustomerOrdersByStatus(String customerId, String status);
}
```

### Repository Usage

```java
@Service
public class CustomerService {
    
    private final CustomerRepository repository;
    
    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }
    
    public Customer getByEmail(String email) {
        return repository.findByEmail(email)
            .orElseThrow(() -> new CustomerNotFoundException(email));
    }
    
    public Customer save(Customer customer) {
        return repository.save(customer);
    }
    
    public void delete(String id) {
        repository.deleteById(id);
    }
}
```

## Query Examples

### Find Customer by Email

```java
Customer customer = repository.findByEmail("john@example.com")
    .orElseThrow(() -> new CustomerNotFoundException("john@example.com"));
```

### Find Orders by Customer

```java
List<Order> orders = orderRepository.findByCustomerId(customerId);
```

### Find Pending Orders

```java
List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);
```

### Search Customers by Name

```java
List<Customer> results = repository.searchByName("john");
```

## Transactions

MongoDB supports multi-document ACID transactions (since 4.0).

```java
@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final MongoTemplate mongoTemplate;
    
    @Transactional
    public Order createOrder(String customerId, List<OrderItem> items) {
        // Verify customer exists
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        // Create order
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setItems(items);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        // Save order and update customer
        Order saved = orderRepository.save(order);
        
        // If any operation fails, transaction rolls back
        return saved;
    }
}
```

## Aggregation Pipeline

For complex queries, use MongoDB aggregation:

```java
@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    
    @Aggregation(pipeline = {
        "{ $match: { 'status': ?0 } }",
        "{ $group: { '_id': '$category', 'count': { $sum: 1 } } }",
        "{ $sort: { 'count': -1 } }"
    })
    List<CategoryCount> countByCategory(String category);
}

record CategoryCount(
    @Field("_id") String category,
    int count
) {}
```

## Connection Configuration

```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/spring-boot-poc}
      auto-index-creation: true  # Create indexes on application startup
```

Environment-based connection strings:

```bash
# Local development
export MONGODB_URI="mongodb://localhost:27017/spring-boot-poc"

# Docker Compose (see docker-compose.yml)
export MONGODB_URI="mongodb://mongodb:27017/spring-boot-poc"

# MongoDB Atlas (cloud)
export MONGODB_URI="mongodb+srv://user:password@cluster.mongodb.net/spring-boot-poc?retryWrites=true&w=majority"
```

## Indexes

### Automatic Index Creation

Spring Data MongoDB can automatically create indexes defined via `@Indexed`:

```yaml
spring:
  data:
    mongodb:
      auto-index-creation: true
```

### Manual Index Creation

For complex indexes, create manually:

```java
@Configuration
public class MongoConfig {
    
    @Bean
    public IndexOperationsSessionCallback indexCreator() {
        return session -> {
            var indexOps = session.indexOps(Customer.class);
            
            // Create compound index
            indexOps.ensureIndex(new Index()
                .on("createdAt", Sort.Direction.DESC)
                .on("status", Sort.Direction.ASC));
            
            return null;
        };
    }
}
```

## Best Practices

1. **Use Embedded Documents for Tight Coupling**: Address is embedded in Customer
2. **Use References for Loose Coupling**: Order references Customer by ID
3. **Index Frequently Queried Fields**: Email (unique), status, customerId
4. **Avoid Deep Nesting**: Keep embedded documents shallow (2-3 levels max)
5. **Denormalize When Necessary**: Include product name in OrderItem (avoid join)
6. **Use Transactions for Consistency**: Multi-document operations
7. **Monitor Query Performance**: Use MongoDB profiler
8. **Use Projections**: Only fetch needed fields
9. **Implement Soft Deletes**: Add `deletedAt` field instead of hard delete
10. **Validate Data at Domain Layer**: Rich entities enforce constraints

## See Also

- [Architecture](architecture.md) - Entity design patterns
- [Testing](testing.md) - MongoDB test fixtures
- [Troubleshooting](troubleshooting.md) - Connection issues
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Spring Data MongoDB Reference](https://spring.io/projects/spring-data-mongodb)
