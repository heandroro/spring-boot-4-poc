package com.example.poc.infrastructure.persistence;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.stereotype.Component;

import com.example.poc.domain.Customer;

import jakarta.annotation.PostConstruct;

/**
 * MongoDB Index Initializer
 * 
 * Creates database indexes on application startup for optimal query
 * performance.
 * 
 * Indexes Created:
 * 1. email (unique) - Fast lookups, uniqueness constraint
 * 2. status - Filter by customer status
 * 3. createdAt (descending) - Pagination, recent customers
 * 4. creditLimit - Range queries on credit
 * 
 * Index Strategy:
 * - Single field indexes for common queries
 * - Compound indexes for multi-field queries
 * - Unique indexes for business constraints
 * - TTL indexes for automatic document expiration (if needed)
 * 
 * Production Note:
 * In production, indexes should be created via:
 * - Database migration scripts
 * - MongoDB Ops Manager
 * - Manual DBA commands
 * 
 * This class is useful for:
 * - Development environment
 * - Testing with Testcontainers
 * - Documentation of required indexes
 * 
 * References:
 * - mongodb.md: Index strategy and performance tuning
 * - docs/deployment.md: Production index management
 */
@Component
public class MongoIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexInitializer.class);

    private final MongoTemplate mongoTemplate;

    public MongoIndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Create indexes on application startup
     * 
     * This runs once when the application starts.
     * Existing indexes are not recreated.
     */
    @PostConstruct
    public void initIndexes() {
        log.info("Initializing MongoDB indexes...");

        try {
            createCustomerIndexes();
            logExistingIndexes();
            log.info("MongoDB indexes initialized successfully");
        } catch (Exception e) {
            log.error("Failed to create MongoDB indexes", e);
            // Don't fail application startup if indexes fail
            // They can be created manually later
        }
    }

    /**
     * Create indexes for Customer collection
     */
    @SuppressWarnings("removal")
    private void createCustomerIndexes() {
        String collectionName = "customers";

        // 1. Unique index on email
        // Ensures no duplicate emails in system
        // Used by: findByEmail(), registration validation
        mongoTemplate.indexOps(Customer.class)
                .ensureIndex(new Index()
                        .on("email.value", Sort.Direction.ASC)
                        .unique()
                        .named("idx_customer_email_unique"));
        log.debug("Created unique index on email");

        // 2. Index on status
        // Used by: findByStatus(), findAllActive(), dashboard queries
        mongoTemplate.indexOps(Customer.class)
                .ensureIndex(new Index()
                        .on("status", Sort.Direction.ASC)
                        .named("idx_customer_status"));
        log.debug("Created index on status");

        // 3. Index on createdAt (descending)
        // Used by: recent customers query, pagination, analytics
        mongoTemplate.indexOps(Customer.class)
                .ensureIndex(new Index()
                        .on("createdAt", Sort.Direction.DESC)
                        .named("idx_customer_created_at"));
        log.debug("Created index on createdAt");

        // 4. Compound index for active customers sorted by creation
        // Used by: active customer list with pagination
        mongoTemplate.indexOps(Customer.class)
                .ensureIndex(new Index()
                        .on("status", Sort.Direction.ASC)
                        .on("createdAt", Sort.Direction.DESC)
                        .named("idx_customer_status_created"));
        log.debug("Created compound index on status + createdAt");

        // 5. Index on creditLimit for range queries
        // Used by: high-value customer queries, credit analysis
        mongoTemplate.indexOps(Customer.class)
                .ensureIndex(new Index()
                        .on("creditLimit.amount", Sort.Direction.DESC)
                        .named("idx_customer_credit_limit"));
        log.debug("Created index on creditLimit");
    }

    /**
     * Log existing indexes for debugging
     */
    private void logExistingIndexes() {
        List<IndexInfo> indexes = mongoTemplate.indexOps(Customer.class).getIndexInfo();
        log.info("Customer collection has {} indexes:", indexes.size());
        indexes.forEach(index -> log.info("  - {} (unique: {})", index.getName(), index.isUnique()));
    }
}
