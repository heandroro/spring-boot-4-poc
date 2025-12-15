package com.example.poc.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

        // Index 1: email (unique, ASC)
        IndexDefinition emailIndex = capturedIndexes.get(0);
        Document emailIndexDoc = emailIndex.getIndexKeys();
        assertNotNull(emailIndexDoc, "Email index should have keys");
        assertTrue(emailIndexDoc.containsKey("email.value"), "Should index email.value field");
        assertEquals(1, emailIndexDoc.get("email.value"), "Email index should be ASC (1)");
        assertTrue(emailIndex.getIndexOptions().containsKey("unique"), "Email index should be unique");
        assertEquals(true, emailIndex.getIndexOptions().get("unique"), "Email index unique flag should be true");

        // Index 2: status (ASC)
        IndexDefinition statusIndex = capturedIndexes.get(1);
        Document statusIndexDoc = statusIndex.getIndexKeys();
        assertNotNull(statusIndexDoc, "Status index should have keys");
        assertTrue(statusIndexDoc.containsKey("status"), "Should index status field");
        assertEquals(1, statusIndexDoc.get("status"), "Status index should be ASC (1)");

        // Index 3: createdAt (DESC)
        IndexDefinition createdAtIndex = capturedIndexes.get(2);
        Document createdAtIndexDoc = createdAtIndex.getIndexKeys();
        assertNotNull(createdAtIndexDoc, "CreatedAt index should have keys");
        assertTrue(createdAtIndexDoc.containsKey("createdAt"), "Should index createdAt field");
        assertEquals(-1, createdAtIndexDoc.get("createdAt"), "CreatedAt index should be DESC (-1)");

        // Index 4: compound index (status ASC + createdAt DESC)
        IndexDefinition compoundIndex = capturedIndexes.get(3);
        Document compoundIndexDoc = compoundIndex.getIndexKeys();
        assertNotNull(compoundIndexDoc, "Compound index should have keys");
        assertTrue(compoundIndexDoc.containsKey("status"), "Compound index should include status");
        assertTrue(compoundIndexDoc.containsKey("createdAt"), "Compound index should include createdAt");
        assertEquals(1, compoundIndexDoc.get("status"), "Compound index status should be ASC (1)");
        assertEquals(-1, compoundIndexDoc.get("createdAt"), "Compound index createdAt should be DESC (-1)");

        // Index 5: creditLimit.amount (DESC)
        IndexDefinition creditLimitIndex = capturedIndexes.get(4);
        Document creditLimitIndexDoc = creditLimitIndex.getIndexKeys();
        assertNotNull(creditLimitIndexDoc, "CreditLimit index should have keys");
        assertTrue(creditLimitIndexDoc.containsKey("creditLimit.amount"), "Should index creditLimit.amount field");
        assertEquals(-1, creditLimitIndexDoc.get("creditLimit.amount"), "CreditLimit index should be DESC (-1)");
    }
}
