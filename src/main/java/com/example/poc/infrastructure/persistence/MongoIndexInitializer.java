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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;

@Component
public class MongoIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexInitializer.class);

    private final MongoTemplate mongoTemplate;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "MongoTemplate is injected and thread-safe; storing the reference is intentional and safe")
    public MongoIndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

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

    @SuppressWarnings("removal")
    private void createCustomerIndexes() {

        // 1. Unique index on email
        // Ensures no duplicate emails in system
        // Email is stored as a nested document (value) when no converter is present,
        // so index on the nested `email.value` field to ensure uniqueness.
        mongoTemplate.indexOps(Customer.class)
                .createIndex(new Index()
                        .on("email.value", Sort.Direction.ASC)
                        .unique()
                        .named("idx_customer_email_unique"));
        log.debug("Created unique index on email.value");

        // 2. Index on status
        // Used by: findByStatus(), findAllActive(), dashboard queries
        mongoTemplate.indexOps(Customer.class)
                .createIndex(new Index()
                        .on("status", Sort.Direction.ASC)
                        .named("idx_customer_status"));
        log.debug("Created index on status");

        // 3. Index on createdAt (descending)
        // Used by: recent customers query, pagination, analytics
        mongoTemplate.indexOps(Customer.class)
                .createIndex(new Index()
                        .on("createdAt", Sort.Direction.DESC)
                        .named("idx_customer_created_at"));
        log.debug("Created index on createdAt");

        // 4. Compound index for active customers sorted by creation
        // Used by: active customer list with pagination
        mongoTemplate.indexOps(Customer.class)
                .createIndex(new Index()
                        .on("status", Sort.Direction.ASC)
                        .on("createdAt", Sort.Direction.DESC)
                        .named("idx_customer_status_created"));
        log.debug("Created compound index on status + createdAt");

        // 5. Index on creditLimit for range queries
        // Used by: high-value customer queries, credit analysis
        mongoTemplate.indexOps(Customer.class)
                .createIndex(new Index()
                        .on("creditLimit.amount", Sort.Direction.DESC)
                        .named("idx_customer_credit_limit"));
        log.debug("Created index on creditLimit");
    }

    private void logExistingIndexes() {
        List<IndexInfo> indexes = mongoTemplate.indexOps(Customer.class).getIndexInfo();
        log.info("Customer collection has {} indexes:", indexes.size());
        indexes.forEach(index -> log.info("  - {} (unique: {})", index.getName(), index.isUnique()));
    }
}
