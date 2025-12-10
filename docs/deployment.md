# Deployment

## Development Environment

### Prerequisites

- **Java**: 25+
- **Maven/Gradle**: Latest
- **Docker**: 20.10+ (for MongoDB and testing)
- **Git**: 2.30+

### Local Setup

1. **Clone repository**:
   ```bash
   git clone https://github.com/heandroro/spring-boot-4-poc.git
   cd spring-boot-4-poc
   ```

2. **Start MongoDB** (Docker):
   ```bash
   docker-compose up -d mongodb
   ```

3. **Build project**:
   ```bash
   ./gradlew clean build
   ```

4. **Run application**:
   ```bash
   ./gradlew bootRun
   ```

5. **Verify**:
   ```bash
   curl http://localhost:8080/health
   ```

## Docker Deployment

### Build Docker Image

```bash
# Using Gradle bootBuildImage task
./gradlew bootBuildImage --imageName=spring-boot-poc:latest

# Or with custom name and version
./gradlew bootBuildImage \
  --imageName=myregistry.azurecr.io/spring-boot-poc:1.0.0
```

### Docker Compose (Development)

```yaml
# docker-compose.yml (already in repo)
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      MONGODB_URI: mongodb://mongodb:27017/spring-boot-poc
      JWT_SECRET: dev-secret-key
    depends_on:
      - mongodb
    
  mongodb:
    image: mongo:7.0
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    environment:
      MONGO_INITDB_DATABASE: spring-boot-poc

volumes:
  mongodb_data:
```

Run with Docker Compose:
```bash
docker-compose up
```

### Docker Registry Push

Push to container registry:

```bash
# Docker Hub
docker tag spring-boot-poc:latest myusername/spring-boot-poc:latest
docker push myusername/spring-boot-poc:latest

# Azure Container Registry
az acr build --registry myregistry --image spring-boot-poc:latest .

# AWS ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin myregistry.dkr.ecr.us-east-1.amazonaws.com
docker tag spring-boot-poc:latest myregistry.dkr.ecr.us-east-1.amazonaws.com/spring-boot-poc:latest
docker push myregistry.dkr.ecr.us-east-1.amazonaws.com/spring-boot-poc:latest
```

## Production Deployment

### Environment Variables

Required production environment variables:

```bash
# Database
MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/spring-boot-poc?retryWrites=true&w=majority

# Security
JWT_SECRET=your-production-secret-key-32-chars-minimum
JWT_EXPIRATION=3600  # 1 hour in seconds
CORS_ALLOWED_ORIGINS=https://app.example.com,https://www.example.com

# Server
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

# Logging
LOG_LEVEL=INFO
SPRING_PROFILES_ACTIVE=production
```

### SSL/TLS Configuration

```yaml
# application-production.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: /etc/secrets/keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
```

Provide keystore via:
- Kubernetes Secret
- Docker volume mount
- Environment variable (base64 encoded)

### Kubernetes Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-boot-poc
  labels:
    app: spring-boot-poc
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-boot-poc
  template:
    metadata:
      labels:
        app: spring-boot-poc
    spec:
      containers:
      - name: app
        image: myregistry.azurecr.io/spring-boot-poc:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: mongodb-uri
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: jwt-secret
        - name: SPRING_PROFILES_ACTIVE
          value: production
        
        # Health checks
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
        
        # Resource limits
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"

---
apiVersion: v1
kind: Service
metadata:
  name: spring-boot-poc
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: spring-boot-poc
```

Deploy to Kubernetes:
```bash
kubectl apply -f k8s/deployment.yaml
kubectl get pods
kubectl logs -f <pod-name>
```

### Database Migration

Ensure MongoDB database and indexes are created before deployment:

```bash
# Spring Data auto-creates indexes if configured:
spring:
  data:
    mongodb:
      auto-index-creation: true
```

Or manually:
```javascript
// MongoDB shell
use spring-boot-poc

// Create indexes
db.customers.createIndex({ "email": 1 }, { unique: true })
db.customers.createIndex({ "createdAt": -1 })
db.orders.createIndex({ "customerId": 1 })
db.orders.createIndex({ "status": 1 })
```

## Monitoring & Logging

### Logging Configuration

```yaml
# application-production.yml
logging:
  level:
    root: INFO
    com.example.poc: DEBUG
    org.springframework: WARN
  file:
    name: /var/log/app/spring-boot-poc.log
    max-size: 10MB
    max-history: 30
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    console: "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Health Endpoints

Spring Boot exposes health checks:

```bash
# Liveness probe (is app running?)
curl http://localhost:8080/health

# Readiness probe (is app ready to serve?)
curl http://localhost:8080/health/ready

# Detailed metrics
curl http://localhost:8080/actuator
curl http://localhost:8080/actuator/metrics
```

### Application Insights (Azure)

```yaml
# application-production.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      azuremonitor:
        enabled: true
```

### Prometheus Metrics

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

Scrape endpoint: `http://localhost:8080/actuator/prometheus`

## Scaling Considerations

### Stateless Design
Application is stateless - each instance is independent.

### Session Management
JWT tokens are stateless - no session state required.

### Database Connection Pooling
MongoDB connection pool automatically managed by Spring Data:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      max-pool-size: 50  # Adjust based on load
```

### Load Balancing
Use round-robin load balancer or Kubernetes service:

```yaml
# Kubernetes service automatically load balances
kind: Service
metadata:
  name: spring-boot-poc
spec:
  type: LoadBalancer
  selector:
    app: spring-boot-poc
```

## Security Checklist

- [ ] Use HTTPS/TLS in production
- [ ] Set strong JWT_SECRET (32+ characters)
- [ ] Configure CORS_ALLOWED_ORIGINS correctly
- [ ] Use Kubernetes Secrets for sensitive data
- [ ] Enable audit logging
- [ ] Configure rate limiting
- [ ] Use strong MongoDB authentication
- [ ] Enable MongoDB encryption at rest
- [ ] Regular security patches
- [ ] Container image scanning
- [ ] Network policies/security groups

## Rollback Procedures

### Docker Compose
```bash
# Rollback to previous version
docker-compose down
docker pull myregistry.azurecr.io/spring-boot-poc:previous-tag
docker-compose up -d
```

### Kubernetes
```bash
# View rollout history
kubectl rollout history deployment/spring-boot-poc

# Rollback to previous version
kubectl rollout undo deployment/spring-boot-poc

# Rollback to specific revision
kubectl rollout undo deployment/spring-boot-poc --to-revision=2
```

## Backup & Recovery

### MongoDB Backup
```bash
# Backup
mongodump --uri mongodb+srv://user:pass@cluster.mongodb.net/spring-boot-poc \
  --out /backups/spring-boot-poc-$(date +%Y%m%d)

# Restore
mongorestore --uri mongodb+srv://user:pass@cluster.mongodb.net/spring-boot-poc \
  --dir /backups/spring-boot-poc-20251209
```

### Kubernetes PVC Backup
```bash
# Use cloud provider backup solutions:
# - Azure Backup
# - AWS Backup
# - GCP Cloud Backup
```

## See Also

- [Architecture](architecture.md) - Application design
- [Security](security.md) - Security configuration
- [Troubleshooting](troubleshooting.md) - Deployment issues
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
