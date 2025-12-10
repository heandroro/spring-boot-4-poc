# API Reference

## Base URL

```
http://localhost:8080/api
```

## Authentication

All endpoints (except `/authenticate`) require JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

## Response Format

All responses follow [RFC 7807 - Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807).

### Success Response (2xx)

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2025-12-09T10:30:00Z"
}
```

### Error Response (4xx, 5xx)

```json
{
  "type": "https://api.example.com/errors/customer-not-found",
  "title": "Customer Not Found",
  "status": 404,
  "detail": "Customer with ID 'abc123' does not exist",
  "instance": "/api/customers/abc123",
  "timestamp": "2025-12-09T10:35:00Z"
}
```

## Endpoints

### Authentication

#### POST /authenticate

Generate JWT token for subsequent requests.

**Request:**
```bash
curl -X POST http://localhost:8080/api/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

**Status Codes:**
- `200 OK` - Authentication successful
- `401 Unauthorized` - Invalid credentials
- `400 Bad Request` - Missing or invalid fields

---

### Customers

#### GET /customers

List all customers (paginated).

**Query Parameters:**
- `page` (optional, default: 0) - Page number (0-indexed)
- `size` (optional, default: 20) - Items per page
- `sort` (optional) - Sort field and direction (e.g., `name,asc`)

**Request:**
```bash
curl -X GET 'http://localhost:8080/api/customers?page=0&size=10' \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "507f1f77bcf86cd799439011",
      "name": "John Doe",
      "email": "john@example.com",
      "createdAt": "2025-12-01T10:30:00Z"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

**Status Codes:**
- `200 OK` - Customers retrieved
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions

---

#### GET /customers/{id}

Retrieve a specific customer.

**Request:**
```bash
curl -X GET http://localhost:8080/api/customers/507f1f77bcf86cd799439011 \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2025-12-01T10:30:00Z"
}
```

**Status Codes:**
- `200 OK` - Customer found
- `404 Not Found` - Customer does not exist
- `401 Unauthorized` - Missing or invalid token

---

#### POST /customers

Create a new customer (Admin only).

**Request:**
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "email": "jane@example.com",
    "phone": "+1234567890"
  }'
```

**Request Body:**
```json
{
  "name": "Jane Smith",
  "email": "jane@example.com",
  "phone": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "id": "507f1f77bcf86cd799439012",
  "name": "Jane Smith",
  "email": "jane@example.com",
  "createdAt": "2025-12-09T14:20:00Z"
}
```

**Status Codes:**
- `201 Created` - Customer created successfully
- `400 Bad Request` - Invalid input (see validation errors)
- `409 Conflict` - Email already exists
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Requires ADMIN role

**Validation Errors (400):**
```json
{
  "type": "https://api.example.com/errors/validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Input validation failed",
  "errors": {
    "email": "Invalid email format",
    "name": "Name must be between 2 and 100 characters"
  }
}
```

---

#### PUT /customers/{id}

Update a customer (Admin only).

**Request:**
```bash
curl -X PUT http://localhost:8080/api/customers/507f1f77bcf86cd799439011 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "phone": "+9876543210"
  }'
```

**Request Body:**
```json
{
  "name": "John Updated",
  "phone": "+9876543210"
}
```

**Response (200 OK):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "John Updated",
  "email": "john@example.com",
  "phone": "+9876543210",
  "createdAt": "2025-12-01T10:30:00Z",
  "updatedAt": "2025-12-09T14:25:00Z"
}
```

**Status Codes:**
- `200 OK` - Customer updated
- `404 Not Found` - Customer does not exist
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Requires ADMIN role

---

#### DELETE /customers/{id}

Delete a customer (Admin only).

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/customers/507f1f77bcf86cd799439011 \
  -H "Authorization: Bearer <token>"
```

**Response (204 No Content):**
```
[No response body]
```

**Status Codes:**
- `204 No Content` - Customer deleted successfully
- `404 Not Found` - Customer does not exist
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Requires ADMIN role

---

## Error Codes

| Status | Type | Description |
|--------|------|-------------|
| 400 | `validation-failed` | Input validation failed |
| 401 | `unauthorized` | Missing or invalid authentication |
| 403 | `forbidden` | Insufficient permissions |
| 404 | `not-found` | Resource does not exist |
| 409 | `conflict` | Resource already exists (e.g., duplicate email) |
| 500 | `internal-error` | Internal server error |

## Rate Limiting

Current implementation does not include rate limiting. Contact API team for SLA requirements.

## Versioning

API uses URL path versioning (future): `/api/v1/customers`, `/api/v2/customers`, etc.

Currently on implicit v1 (no version prefix).

## See Also

- [Architecture](architecture.md) - API design patterns
- [Security](security.md) - Authentication, authorization, validation
- [Testing](testing.md) - API testing examples
