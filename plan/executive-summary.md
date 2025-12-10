# Resumo Executivo - E-commerce Backend API

## 📌 Visão Geral

Sistema backend completo para e-commerce/marketplace desenvolvido com **Spring Boot 4** e **MongoDB**, seguindo princípios de **Domain-Driven Design (DDD)**.

---

## 🎯 Objetivos

- Criar API REST escalável para operações de e-commerce
- Implementar autenticação JWT com controle de acesso baseado em roles
- Gerenciar catálogo de produtos com busca avançada
- Processar pedidos com fluxo de status completo
- Integrar gateway de pagamento (Stripe)
- Sistema de reviews e ratings

---

## 🏗️ Arquitetura

**Padrão:** DDD (Domain-Driven Design) em camadas

```
┌─────────────────────────────────────┐
│         Web Layer (Controllers)      │  ← API REST
├─────────────────────────────────────┤
│      Application Layer (Services)    │  ← Lógica de negócio
├─────────────────────────────────────┤
│        Domain Layer (Entities)       │  ← Modelos de domínio
├─────────────────────────────────────┤
│  Infrastructure Layer (Repositories) │  ← Persistência
└─────────────────────────────────────┘
              ↓
         MongoDB
```

---

## 🛠️ Stack Tecnológica

| Componente | Tecnologia | Versão |
|------------|-----------|--------|
| **Linguagem** | Java | 25 |
| **Framework** | Spring Boot | 4.x |
| **Banco de Dados** | MongoDB | 7.0 |
| **Mapeamento** | MapStruct | Latest |
| **Testes** | JUnit Jupiter | 6.x |
| **Dados de Teste** | Instancio | Latest |
| **Containers** | Testcontainers | Latest |
| **Autenticação** | JWT (jjwt) | Latest |
| **Pagamentos** | Stripe SDK | Latest |
| **Documentação** | SpringDoc OpenAPI | Latest |

---

## 📦 Entidades Principais

### 1. **Product** (Produto)
- Catálogo com atributos dinâmicos
- Controle de estoque (disponível, reservado, total)
- Especificações flexíveis (Map)
- Imagens e ratings agregados

### 2. **Customer** (Cliente)
- Dados cadastrais com validação
- Múltiplos endereços
- Preferências personalizadas
- Sistema de pontos de fidelidade

### 3. **Order** (Pedido)
- Snapshots imutáveis de produtos
- Máquina de estados (9 status)
- Timeline com rastreabilidade
- Informações de pagamento e envio

### 4. **Cart** (Carrinho)
- Itens com preço capturado
- Cálculo automático de totais
- TTL de 7 dias (expiração automática)
- Suporte a cupons de desconto

### 5. **Review** (Avaliação)
- Rating de 1-5 estrelas
- Moderação (pending/approved/rejected)
- Sistema de "útil" (helpful votes)
- Validação de compra verificada

---

## 🔐 Segurança

### Autenticação
- **JWT Tokens** com expiração configurável
- **Refresh tokens** para renovação
- **BCrypt** para hash de senhas (12 rounds)

### Autorização (RBAC)
| Role | Permissões |
|------|-----------|
| **CUSTOMER** | Ver produtos, gerenciar carrinho, criar pedidos próprios |
| **MANAGER** | Gerenciar pedidos, atualizar status, visualizar relatórios |
| **ADMIN** | Acesso completo, gerenciar produtos, aprovar reviews |

### Proteções
- Validação de entrada em todos endpoints
- Schema validation no MongoDB
- Tokenização de dados de pagamento (nunca armazenar cartões)
- Auditoria de operações críticas

---

## 🚀 Funcionalidades Principais

### Gestão de Produtos
- [x] CRUD completo com validações
- [x] Busca por categoria, faixa de preço
- [x] Full-text search (MongoDB text index)
- [x] Controle de estoque com reservas

### Carrinho de Compras
- [x] Adicionar/remover/atualizar itens
- [x] Cálculo automático de totais
- [x] Aplicação de cupons
- [x] Expiração automática (TTL)

### Processamento de Pedidos
- [x] Checkout com snapshot de dados
- [x] Integração com Stripe
- [x] Fluxo de status (pending → delivered)
- [x] Timeline de eventos
- [x] Cancelamento e devolução

### Reviews e Ratings
- [x] Criar review (apenas compradores)
- [x] Sistema de moderação
- [x] Agregação de ratings
- [x] Votos de utilidade

### Relatórios
- [x] Vendas por período (aggregation)
- [x] Top produtos mais vendidos
- [x] Métricas de pedidos por status

---

## 📊 MongoDB - Estratégia de Dados

### Embedding vs. Referencing

| Relacionamento | Estratégia | Justificativa |
|----------------|-----------|---------------|
| Customer → Addresses | **Embedding** | Poucos endereços, acessados juntos |
| Order → Items | **Embedding** | Snapshot imutável, performance |
| Product → Reviews | **Referencing** | Crescimento ilimitado |
| Order → Customer | **Hybrid** | Referência + snapshot |

### Índices Principais
```javascript
// Products
{ sku: 1 } // unique
{ category: 1, price: 1 } // compound
{ name: "text", description: "text" } // full-text

// Orders
{ orderNumber: 1 } // unique
{ customerId: 1, createdAt: -1 } // compound

// Carts
{ customerId: 1 } // unique
{ expiresAt: 1 } // TTL
```

---

## 🔄 Fluxo de Pedidos

```
PENDING
  ↓
PAYMENT_PROCESSING
  ↓
CONFIRMED
  ↓
PREPARING
  ↓
SHIPPED
  ↓
DELIVERED
  ↓
COMPLETED
```

**Exceções:** CANCELLED, RETURNED, REFUNDED

---

## 📈 Timeline de Implementação

| Fase | Duração | Entregas |
|------|---------|----------|
| **1. Configuração** | 2-3 dias | MongoDB setup, estrutura de pacotes |
| **2. Domínio** | 3-4 dias | Entidades e validações |
| **3. Persistência** | 2-3 dias | Repositórios e índices |
| **4. Autenticação** | 3-4 dias | JWT e Spring Security |
| **5. Serviços** | 5-6 dias | Lógica de negócio |
| **6. DTOs/Mappers** | 2-3 dias | MapStruct configuration |
| **7. API REST** | 4-5 dias | Controllers e endpoints |
| **8. Testes** | 4-5 dias | Unit + Integration tests |
| **9. Deploy** | 3-4 dias | Docker, docs, Swagger |

**Total:** ~5 semanas

---

## 🎯 Métricas de Sucesso

- [ ] **Cobertura de Testes:** >90%
- [ ] **Performance:** Resposta < 500ms (p95)
- [ ] **Disponibilidade:** 99.9% uptime
- [ ] **Segurança:** Zero vulnerabilidades críticas
- [ ] **Documentação:** 100% endpoints documentados
- [ ] **Code Quality:** SonarQube score > 90%

---

## 🚦 Riscos e Mitigações

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| Escalabilidade MongoDB | Alto | Sharding, réplicas, índices otimizados |
| Segurança de pagamento | Crítico | Tokenização, PCI-DSS compliance |
| Consistência de dados | Médio | Transações MongoDB, snapshots |
| Performance | Médio | Cache (Redis), aggregation pipelines |

---

## 📚 Próximos Passos

1. ✅ Aprovar arquitetura e stack
2. ⏳ Setup inicial do projeto
3. ⏳ Implementar entidades de domínio
4. ⏳ Configurar autenticação
5. ⏳ Desenvolver serviços core
6. ⏳ Criar API REST
7. ⏳ Suite de testes completa
8. ⏳ Deploy e monitoramento

---

**Documento criado:** 4 de dezembro de 2025  
**Versão:** 1.0  
**Status:** Planejamento
