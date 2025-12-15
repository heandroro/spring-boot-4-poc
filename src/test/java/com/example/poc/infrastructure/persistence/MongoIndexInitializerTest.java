package com.example.poc.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    @SuppressWarnings("removal")
    void shouldCreateExpectedIndexesForCustomerCollection() {
        // Given: indexOps mocked for Customer class
        when(mongoTemplate.indexOps(Customer.class)).thenReturn(indexOps);

        // When
        initializer = new MongoIndexInitializer(mongoTemplate);
        initializer.initIndexes();

        // Then: Capture all IndexDefinition objects
        ArgumentCaptor<IndexDefinition> indexCaptor = ArgumentCaptor.forClass(IndexDefinition.class);
        verify(indexOps, times(5)).ensureIndex(indexCaptor.capture());
        
        List<IndexDefinition> capturedIndexes = indexCaptor.getAllValues();
        assertEquals(5, capturedIndexes.size(), "Should create exactly 5 indexes");

        // Find indexes by their field keys (order-independent)
        IndexDefinition emailIndex = findIndexByField(capturedIndexes, "email.value");
        assertNotNull(emailIndex, "Should have email index");
        Document emailIndexDoc = emailIndex.getIndexKeys();
        assertEquals(1, emailIndexDoc.get("email.value"), "Email index should be ASC (1)");
        assertTrue(emailIndex.getIndexOptions().containsKey("unique"), "Email index should be unique");
        assertEquals(true, emailIndex.getIndexOptions().get("unique"), "Email index unique flag should be true");

        // Find status index (single field)
        IndexDefinition statusIndex = findIndexByField(capturedIndexes, "status");
        assertNotNull(statusIndex, "Should have status index");
        Document statusIndexDoc = statusIndex.getIndexKeys();
        assertEquals(1, statusIndexDoc.size(), "Status index should have 1 field");
        assertEquals(1, statusIndexDoc.get("status"), "Status index should be ASC (1)");

        // Find createdAt index (single field)
        IndexDefinition createdAtIndex = findIndexByField(capturedIndexes, "createdAt");
        assertNotNull(createdAtIndex, "Should have createdAt index");
        Document createdAtIndexDoc = createdAtIndex.getIndexKeys();
        assertEquals(1, createdAtIndexDoc.size(), "CreatedAt index should have 1 field");
        assertEquals(-1, createdAtIndexDoc.get("createdAt"), "CreatedAt index should be DESC (-1)");

        // Find compound index (status + createdAt)
        IndexDefinition compoundIndex = findCompoundIndex(capturedIndexes, "status", "createdAt");
        assertNotNull(compoundIndex, "Should have compound index with status and createdAt");
        Document compoundIndexDoc = compoundIndex.getIndexKeys();
        assertEquals(2, compoundIndexDoc.size(), "Compound index should have 2 fields");
        assertEquals(1, compoundIndexDoc.get("status"), "Compound index status should be ASC (1)");
        assertEquals(-1, compoundIndexDoc.get("createdAt"), "Compound index createdAt should be DESC (-1)");

        // Find creditLimit.amount index
        IndexDefinition creditLimitIndex = findIndexByField(capturedIndexes, "creditLimit.amount");
        assertNotNull(creditLimitIndex, "Should have creditLimit.amount index");
        Document creditLimitIndexDoc = creditLimitIndex.getIndexKeys();
        assertEquals(1, creditLimitIndexDoc.size(), "CreditLimit index should have 1 field");
        assertEquals(-1, creditLimitIndexDoc.get("creditLimit.amount"), "CreditLimit index should be DESC (-1)");
    }

    /**
     * Helper method to find a single-field index by field name (order-independent)
     */
    private IndexDefinition findIndexByField(List<IndexDefinition> indexes, String fieldName) {
        return indexes.stream()
                .filter(idx -> {
                    Document keys = idx.getIndexKeys();
                    return keys.containsKey(fieldName) && keys.size() == 1;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper method to find a compound index by two field names (order-independent)
     */
    private IndexDefinition findCompoundIndex(List<IndexDefinition> indexes, String field1, String field2) {
        return indexes.stream()
                .filter(idx -> {
                    Document keys = idx.getIndexKeys();
                    return keys.containsKey(field1) && keys.containsKey(field2) && keys.size() == 2;
                })
                .findFirst()
                .orElse(null);
    }
}
