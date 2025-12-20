package com.example.poc.infrastructure.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexInfo;
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
    @SuppressWarnings("removal")
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

        // Then: verify number of createIndex calls (5 indexes created)
        verify(indexOps, times(5)).createIndex(any(IndexDefinition.class));
    }

    // Helper to inspect IndexDefinition keys via toString()
    private boolean hasKey(IndexDefinition index, String field, Sort.Direction direction) {
        String s = index.toString();
        return s.contains(field) && s.contains(direction.name());
    }

    @Test
    @DisplayName("should log existing indexes when present")
    void shouldLogExistingIndexesWhenPresent() {
        when(mongoTemplate.indexOps(Customer.class)).thenReturn(indexOps);

        IndexInfo idx1 = mock(IndexInfo.class);
        when(idx1.getName()).thenReturn("idx_customer_email_unique");
        when(idx1.isUnique()).thenReturn(true);

        IndexInfo idx2 = mock(IndexInfo.class);
        when(idx2.getName()).thenReturn("idx_customer_created_at");
        when(idx2.isUnique()).thenReturn(false);

        when(indexOps.getIndexInfo()).thenReturn(List.of(idx1, idx2));

        initializer = new MongoIndexInitializer(mongoTemplate);
        initializer.initIndexes();

        verify(indexOps, times(1)).getIndexInfo();
        verify(indexOps, times(5)).createIndex(any(IndexDefinition.class));
    }

    @Test
    @DisplayName("initIndexes swallows exceptions during index creation")
    void shouldSwallowExceptionsDuringIndexCreation() {
        when(mongoTemplate.indexOps(Customer.class)).thenReturn(indexOps);

        doThrow(new RuntimeException("boom")).when(indexOps).createIndex(any(IndexDefinition.class));

        initializer = new MongoIndexInitializer(mongoTemplate);
        initializer.initIndexes();

        verify(indexOps, times(1)).createIndex(any(IndexDefinition.class));
        verify(indexOps, never()).getIndexInfo();
    }
}
