# Entidades de Domínio - E-commerce API

## Product (Produto)

```java
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String sku;
    
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    
    private Stock stock;
    private Map<String, Object> specifications;
    private List<ProductImage> images;
    private ProductRatings ratings;
    
    private Instant createdAt;
    private Instant updatedAt;
}

public record Stock(
    Integer available,
    Integer reserved,
    Integer total
) {}

public record ProductImage(
    String url,
    String alt,
    Boolean isPrimary
) {}

public record ProductRatings(
    Double average,
    Integer count
) {}
```

---

## Customer (Cliente)

```java
@Document(collection = "customers")
public class Customer {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    private String firstName;
    private String lastName;
    private String phone;
    private String passwordHash;
    
    private List<Address> addresses;
    private CustomerPreferences preferences;
    private Integer loyaltyPoints;
    
    private Instant createdAt;
    private Instant lastLogin;
}

public record Address(
    String id,
    String type,  // billing, shipping, default
    String street,
    String city,
    String state,
    String zipCode,
    String country,
    Boolean isDefault
) {}

public record CustomerPreferences(
    Boolean newsletter,
    Boolean notifications,
    String language,
    String currency
) {}
```

---

## Order (Pedido)

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
    private Address shippingAddress;
    private Address billingAddress;
    private OrderTotals totals;
    private PaymentInfo payment;
    private ShippingInfo shipping;
    private OrderStatus status;
    private List<StatusTimeline> timeline;
    
    private Instant createdAt;
    private Instant updatedAt;
}

public record CustomerSnapshot(
    String name,
    String email,
    String phone
) {}

public record OrderItem(
    String productId,
    ProductSnapshot productSnapshot,
    Integer quantity,
    BigDecimal subtotal,
    BigDecimal discount
) {}

public record ProductSnapshot(
    String name,
    String sku,
    BigDecimal price
) {}

public record OrderTotals(
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal shipping,
    BigDecimal discount,
    BigDecimal finalAmount
) {}

public record PaymentInfo(
    String method,
    String status,
    String transactionId,
    Instant processedAt
) {}

public record ShippingInfo(
    String method,
    String trackingNumber,
    String carrier,
    Instant estimatedDelivery,
    Instant actualDelivery
) {}

public record StatusTimeline(
    OrderStatus status,
    Instant timestamp,
    String notes
) {}

public enum OrderStatus {
    PENDING,
    PAYMENT_PROCESSING,
    CONFIRMED,
    PREPARING,
    PICKED,
    PACKED,
    SHIPPED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    RETURNED,
    REFUNDED,
    PAYMENT_FAILED,
    FAILED_DELIVERY
}
```

---

## Cart (Carrinho)

```java
@Document(collection = "carts")
public class Cart {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String customerId;
    
    private List<CartItem> items;
    private CartTotals totals;
    private List<Coupon> appliedCoupons;
    
    @Indexed(expireAfterSeconds = 604800) // 7 dias
    private Instant expiresAt;
    
    private Instant createdAt;
    private Instant updatedAt;
}

public record CartItem(
    String productId,
    Integer quantity,
    BigDecimal price,
    Instant addedAt
) {}

public record CartTotals(
    BigDecimal subtotal,
    BigDecimal estimatedTax,
    BigDecimal estimatedShipping,
    BigDecimal total
) {}

public record Coupon(
    String code,
    BigDecimal discount,
    Instant expiresAt
) {}
```

---

## Review (Avaliação)

```java
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    
    @DBRef
    private String productId;
    
    @DBRef
    private String customerId;
    
    @DBRef
    private String orderId;
    
    @Min(1) @Max(5)
    private Integer rating;
    
    private String title;
    private String content;
    private List<String> images;
    private HelpfulCount helpful;
    private Boolean verified;
    private ReviewStatus status;
    
    private Instant createdAt;
    private Instant updatedAt;
}

public record HelpfulCount(
    Integer yes,
    Integer no
) {}

public enum ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED
}
```

---

**Nota:** Use Java `record` para tipos imutáveis (DTOs, value objects).
