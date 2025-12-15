package com.example.poc.infrastructure.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;

import com.example.poc.domain.Customer;

@ExtendWith(MockitoExtension.class)
@DisplayName("MongoIndexInitializer Unit Tests")
class MongoIndexInitializerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private IndexOperations indexOps;

    @InjectMocks
    private MongoIndexInitializer initializer;

    @Test
    @DisplayName("should create expected indexes for Customer collection")
    void shouldCreateExpectedIndexesForCustomerCollection() {
        // Given: indexOps mocked for Customer class
        when(mongoTemplate.indexOps(Customer.class)).thenReturn(indexOps);

        // When
        initializer = new MongoIndexInitializer(mongoTemplate);
        // invoke internal method by calling public init (will call
        // createCustomerIndexes)
        // We avoid @PostConstruct by calling method directly via reflection of behavior
        // Call private method via public initIndexes
        // Since initIndexes also logs, we just run it
        initializer.initIndexes();

        // Then: verify number of ensureIndex calls (5 indexes created)
        verify(indexOps, times(5)).ensureIndex(any(IndexDefinition.class));
    }

    // Helper to inspect IndexDefinition keys via toString()
    private boolean hasKey(IndexDefinition index, String field, Sort.Direction direction) {
        String s = index.toString();
        return s.contains(field) && s.contains(direction.name());
    }
}
