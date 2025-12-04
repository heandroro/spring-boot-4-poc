# Diagramas de Arquitetura - E-commerce API

Este documento contém os diagramas de arquitetura do sistema em formato Mermaid.

---

## 1. Arquitetura em Camadas (DDD)

```mermaid
graph TB
    subgraph "Web Layer"
        A[ProductController]
        B[OrderController]
        C[CartController]
        D[ReviewController]
        E[AuthController]
    end
    
    subgraph "Application Layer"
        F[ProductService]
        G[OrderService]
        H[CartService]
        I[ReviewService]
        J[AuthService]
        K[PaymentService]
    end
    
    subgraph "Domain Layer"
        L[Product]
        M[Order]
        N[Cart]
        O[Review]
        P[Customer]
    end
    
    subgraph "Infrastructure Layer"
        Q[ProductRepository]
        R[OrderRepository]
        S[CartRepository]
        T[ReviewRepository]
        U[CustomerRepository]
    end
    
    subgraph "Database"
        V[(MongoDB)]
    end
    
    A --> F
    B --> G
    C --> H
    D --> I
    E --> J
    
    F --> L
    G --> M
    G --> K
    H --> N
    I --> O
    J --> P
    
    F --> Q
    G --> R
    H --> S
    I --> T
    J --> U
    
    Q --> V
    R --> V
    S --> V
    T --> V
    U --> V
    
    style A fill:#e1f5ff
    style B fill:#e1f5ff
    style C fill:#e1f5ff
    style D fill:#e1f5ff
    style E fill:#e1f5ff
    
    style F fill:#fff4e1
    style G fill:#fff4e1
    style H fill:#fff4e1
    style I fill:#fff4e1
    style J fill:#fff4e1
    style K fill:#fff4e1
    
    style L fill:#e8f5e9
    style M fill:#e8f5e9
    style N fill:#e8f5e9
    style O fill:#e8f5e9
    style P fill:#e8f5e9
    
    style Q fill:#f3e5f5
    style R fill:#f3e5f5
    style S fill:#f3e5f5
    style T fill:#f3e5f5
    style U fill:#f3e5f5
    
    style V fill:#ffebee
```

---

## 2. Fluxo de Autenticação JWT

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant CustomerRepository
    participant JwtTokenProvider
    participant MongoDB
    
    Client->>AuthController: POST /auth/register
    AuthController->>AuthService: register(dto)
    AuthService->>AuthService: hashPassword(BCrypt)
    AuthService->>CustomerRepository: save(customer)
    CustomerRepository->>MongoDB: insert
    MongoDB-->>CustomerRepository: customer saved
    CustomerRepository-->>AuthService: customer
    AuthService->>JwtTokenProvider: generateToken(customer)
    JwtTokenProvider-->>AuthService: JWT token
    AuthService-->>AuthController: AuthResponse(token, refreshToken)
    AuthController-->>Client: 201 Created + tokens
    
    Client->>AuthController: POST /auth/login
    AuthController->>AuthService: login(email, password)
    AuthService->>CustomerRepository: findByEmail(email)
    CustomerRepository->>MongoDB: query
    MongoDB-->>CustomerRepository: customer
    CustomerRepository-->>AuthService: customer
    AuthService->>AuthService: verifyPassword(BCrypt)
    AuthService->>JwtTokenProvider: generateToken(customer)
    JwtTokenProvider-->>AuthService: JWT token
    AuthService-->>AuthController: AuthResponse(token, refreshToken)
    AuthController-->>Client: 200 OK + tokens
    
    Client->>AuthController: GET /api/products (Authorization: Bearer token)
    AuthController->>JwtTokenProvider: validateToken(token)
    JwtTokenProvider-->>AuthController: valid
    AuthController->>AuthController: process request
    AuthController-->>Client: 200 OK + data
```

---

## 3. Fluxo de Criação de Pedido (Checkout)

```mermaid
sequenceDiagram
    participant Client
    participant OrderController
    participant OrderService
    participant CartService
    participant ProductService
    participant PaymentService
    participant OrderRepository
    participant MongoDB
    
    Client->>OrderController: POST /api/orders (checkout)
    OrderController->>OrderService: createOrder(customerId, checkoutDto)
    
    OrderService->>CartService: getCart(customerId)
    CartService-->>OrderService: cart with items
    
    OrderService->>OrderService: validate cart not empty
    
    loop For each cart item
        OrderService->>ProductService: getProduct(productId)
        ProductService-->>OrderService: product
        OrderService->>OrderService: createProductSnapshot(product)
        OrderService->>ProductService: reserveStock(productId, quantity)
    end
    
    OrderService->>OrderService: calculateTotals()
    OrderService->>OrderService: createOrder(PENDING status)
    
    OrderService->>PaymentService: processPayment(order, paymentMethod)
    PaymentService->>PaymentService: Stripe.createPaymentIntent()
    PaymentService-->>OrderService: PaymentResult(success, transactionId)
    
    alt Payment Success
        OrderService->>OrderService: updateStatus(PAYMENT_PROCESSING → CONFIRMED)
        OrderService->>OrderService: addTimelineEvent(CONFIRMED)
        OrderService->>OrderRepository: save(order)
        OrderRepository->>MongoDB: insert
        MongoDB-->>OrderRepository: order saved
        OrderRepository-->>OrderService: order
        
        OrderService->>CartService: clear(customerId)
        CartService->>MongoDB: delete cart
        
        OrderService-->>OrderController: order
        OrderController-->>Client: 201 Created + order
    else Payment Failed
        OrderService->>OrderService: updateStatus(PAYMENT_FAILED)
        OrderService->>ProductService: releaseStock(items)
        OrderService-->>OrderController: PaymentFailedException
        OrderController-->>Client: 400 Bad Request
    end
```

---

## 4. Máquina de Estados do Pedido

```mermaid
stateDiagram-v2
    [*] --> PENDING: Pedido criado
    
    PENDING --> PAYMENT_PROCESSING: Processar pagamento
    PAYMENT_PROCESSING --> CONFIRMED: Pagamento aprovado
    PAYMENT_PROCESSING --> PAYMENT_FAILED: Pagamento recusado
    PAYMENT_FAILED --> CANCELLED: Auto-cancelamento
    
    CONFIRMED --> PREPARING: Iniciar preparação
    CONFIRMED --> CANCELLED: Cancelamento manual
    
    PREPARING --> PICKED: Itens separados
    PICKED --> PACKED: Itens embalados
    PACKED --> SHIPPED: Enviado
    
    SHIPPED --> IN_TRANSIT: Em trânsito
    IN_TRANSIT --> OUT_FOR_DELIVERY: Saiu para entrega
    OUT_FOR_DELIVERY --> DELIVERED: Entregue
    OUT_FOR_DELIVERY --> FAILED_DELIVERY: Falha na entrega
    
    FAILED_DELIVERY --> OUT_FOR_DELIVERY: Nova tentativa
    FAILED_DELIVERY --> RETURNED: Devolver ao remetente
    
    DELIVERED --> COMPLETED: Confirmação final
    DELIVERED --> RETURNED: Cliente devolve
    
    COMPLETED --> RETURNED: Devolução tardia
    
    RETURNED --> REFUNDED: Reembolso processado
    CANCELLED --> REFUNDED: Reembolso processado
    
    REFUNDED --> [*]
    COMPLETED --> [*]
```

---

## 5. Modelo de Dados MongoDB (ER Diagram)

```mermaid
erDiagram
    CUSTOMER ||--o{ ORDER : places
    CUSTOMER ||--o| CART : has
    CUSTOMER ||--o{ REVIEW : writes
    PRODUCT ||--o{ REVIEW : receives
    PRODUCT ||--o{ CART_ITEM : contains
    PRODUCT ||--o{ ORDER_ITEM : includes
    ORDER ||--|{ ORDER_ITEM : contains
    CART ||--|{ CART_ITEM : contains
    
    CUSTOMER {
        string id PK
        string email UK
        string firstName
        string lastName
        string passwordHash
        array addresses
        object preferences
        int loyaltyPoints
        datetime createdAt
        datetime lastLogin
    }
    
    PRODUCT {
        string id PK
        string sku UK
        string name
        string description
        string category
        decimal price
        object stock
        map specifications
        array images
        object ratings
        datetime createdAt
        datetime updatedAt
    }
    
    ORDER {
        string id PK
        string orderNumber UK
        string customerId FK
        object customerSnapshot
        array items
        object shippingAddress
        object billingAddress
        object totals
        object payment
        object shipping
        enum status
        array timeline
        datetime createdAt
        datetime updatedAt
    }
    
    CART {
        string id PK
        string customerId UK
        array items
        object totals
        array appliedCoupons
        datetime expiresAt
        datetime createdAt
        datetime updatedAt
    }
    
    REVIEW {
        string id PK
        string productId FK
        string customerId FK
        string orderId FK
        int rating
        string title
        string content
        array images
        object helpful
        boolean verified
        enum status
        datetime createdAt
        datetime updatedAt
    }
    
    ORDER_ITEM {
        string productId FK
        object productSnapshot
        int quantity
        decimal subtotal
        decimal discount
    }
    
    CART_ITEM {
        string productId FK
        int quantity
        decimal price
        datetime addedAt
    }
```

---

## 6. Fluxo de Busca de Produtos com Filtros

```mermaid
flowchart TD
    A[Client: GET /api/products?category=Electronics&minPrice=100&maxPrice=1000] --> B{Request com filtros}
    
    B --> C[ProductController.getProducts]
    C --> D[ProductService.search]
    
    D --> E{Tem query de texto?}
    E -->|Sim| F[MongoDB Text Search]
    E -->|Não| G[MongoDB Match Query]
    
    F --> H[Aplicar filtros adicionais]
    G --> H
    
    H --> I{Tem categoria?}
    I -->|Sim| J[Add: category filter]
    I -->|Não| K{Tem faixa de preço?}
    
    J --> K
    K -->|Sim| L[Add: price range filter]
    K -->|Não| M{Apenas em estoque?}
    
    L --> M
    M -->|Sim| N[Add: stock.available > 0]
    M -->|Não| O[Executar query]
    
    N --> O
    O --> P[MongoDB Aggregation Pipeline]
    
    P --> Q[Sort por relevância/preço]
    Q --> R[Aplicar paginação]
    R --> S[Mapear para DTOs]
    S --> T[Retornar Page<ProductDto>]
    T --> U[Client recebe resultados paginados]
    
    style A fill:#e1f5ff
    style U fill:#e8f5e9
    style P fill:#fff4e1
```

---

## 7. Agregação de Ratings de Reviews

```mermaid
flowchart LR
    A[Review aprovada] --> B[Trigger: updateProductRatings]
    
    B --> C[MongoDB Aggregation]
    
    C --> D[Pipeline Stage 1:<br/>$match productId + status=APPROVED]
    D --> E[Pipeline Stage 2:<br/>$group by productId]
    E --> F[Pipeline Stage 3:<br/>Calculate avg rating & count]
    F --> G[Result: avgRating, count]
    
    G --> H[ProductService.updateRatings]
    H --> I[Update product.ratings.average]
    H --> J[Update product.ratings.count]
    
    I --> K[Save to MongoDB]
    J --> K
    
    K --> L[Product ratings atualizados]
    
    style A fill:#ffebee
    style L fill:#e8f5e9
    style C fill:#fff4e1
```

---

## 8. Sistema de Carrinho com TTL

```mermaid
flowchart TD
    A[Cliente adiciona item ao carrinho] --> B[CartService.addItem]
    
    B --> C{Carrinho existe?}
    C -->|Não| D[Criar novo carrinho]
    C -->|Sim| E[Atualizar carrinho existente]
    
    D --> F[Set expiresAt = now + 7 days]
    E --> G[Update expiresAt = now + 7 days]
    
    F --> H[Adicionar item]
    G --> H
    
    H --> I[Calcular totais]
    I --> J[Salvar no MongoDB]
    
    J --> K{TTL Index ativo?}
    K -->|Sim| L[MongoDB monitora expiresAt]
    
    L --> M{expiresAt < now?}
    M -->|Sim| N[MongoDB remove automaticamente]
    M -->|Não| O[Carrinho permanece ativo]
    
    N --> P[Carrinho expirado]
    O --> Q[Carrinho ativo]
    
    style A fill:#e1f5ff
    style P fill:#ffebee
    style Q fill:#e8f5e9
    style L fill:#fff4e1
```

---

## 9. Integração com Gateway de Pagamento (Stripe)

```mermaid
sequenceDiagram
    participant Order as OrderService
    participant Payment as PaymentService
    participant Stripe as Stripe API
    participant Webhook as StripeWebhook
    participant DB as MongoDB
    
    Order->>Payment: processPayment(order, paymentMethod)
    Payment->>Payment: Validate payment data
    
    Payment->>Stripe: POST /v1/payment_intents
    Note right of Stripe: amount: order.total<br/>currency: BRL<br/>payment_method: token<br/>confirm: true
    
    Stripe-->>Payment: PaymentIntent(id, status)
    
    alt Payment Succeeded
        Payment->>DB: Update order.payment.transactionId
        Payment->>DB: Update order.payment.status = PROCESSED
        Payment-->>Order: PaymentResult.success(transactionId)
        
        Note over Stripe,Webhook: Async webhook (confirmação)
        Stripe->>Webhook: POST /webhooks/stripe
        Webhook->>Webhook: Verify signature
        Webhook->>DB: Confirm payment.status
        Webhook-->>Stripe: 200 OK
    else Payment Failed
        Payment->>DB: Update order.payment.status = FAILED
        Payment-->>Order: PaymentResult.failure(errorMessage)
        Order->>Order: Update order.status = PAYMENT_FAILED
    else Payment Requires Action
        Payment-->>Order: PaymentResult.requiresAction(clientSecret)
        Note right of Order: Frontend 3D Secure<br/>Customer confirms
    end
```

---

## 10. Estrutura de Pacotes DDD

```mermaid
graph TB
    subgraph "com.example.ecommerce"
        direction TB
        
        subgraph "domain"
            D1[product/<br/>Product.java<br/>Stock.java<br/>ProductImage.java]
            D2[customer/<br/>Customer.java<br/>Address.java<br/>CustomerPreferences.java]
            D3[order/<br/>Order.java<br/>OrderItem.java<br/>OrderStatus.java]
            D4[cart/<br/>Cart.java<br/>CartItem.java<br/>CartTotals.java]
            D5[review/<br/>Review.java<br/>ReviewStatus.java]
        end
        
        subgraph "application"
            A1[service/<br/>ProductService.java<br/>OrderService.java<br/>CartService.java<br/>ReviewService.java<br/>AuthService.java<br/>PaymentService.java]
            A2[dto/<br/>ProductDto.java<br/>OrderDto.java<br/>CheckoutDto.java]
        end
        
        subgraph "infrastructure"
            I1[repository/<br/>ProductRepository.java<br/>OrderRepository.java<br/>CartRepository.java]
            I2[config/<br/>SecurityConfig.java<br/>MongoConfig.java<br/>JwtConfig.java]
            I3[mapping/<br/>ProductMapper.java<br/>OrderMapper.java<br/>MapStruct]
        end
        
        subgraph "web"
            W1[controller/<br/>ProductController.java<br/>OrderController.java<br/>CartController.java<br/>AuthController.java]
            W2[exception/<br/>GlobalExceptionHandler.java<br/>Custom Exceptions]
        end
    end
    
    W1 --> A1
    W1 --> A2
    A1 --> D1
    A1 --> D2
    A1 --> D3
    A1 --> D4
    A1 --> D5
    A1 --> I1
    A1 --> I3
    I1 --> I2
    
    style D1 fill:#e8f5e9
    style D2 fill:#e8f5e9
    style D3 fill:#e8f5e9
    style D4 fill:#e8f5e9
    style D5 fill:#e8f5e9
    
    style A1 fill:#fff4e1
    style A2 fill:#fff4e1
    
    style I1 fill:#f3e5f5
    style I2 fill:#f3e5f5
    style I3 fill:#f3e5f5
    
    style W1 fill:#e1f5ff
    style W2 fill:#e1f5ff
```

---

**Documento criado:** 4 de dezembro de 2025  
**Versão:** 1.0  
**Formato:** Mermaid Diagrams
