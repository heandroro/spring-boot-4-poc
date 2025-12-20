package com.example.poc.infrastructure.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.example.poc.domain.vo.Money;

/**
 * MongoDB Configuration
 * 
 * Configures Spring Data MongoDB with:
 * - Custom converters for Value Objects (Money, Email, Address)
 * - Repository scanning
 * - Auditing (createdAt/updatedAt)
 * - Index creation
 * 
 * Database Schema Strategy:
 * - Collections: customers, orders, products
 * - Indexes: email (unique), status, createdAt
 * - Value Objects: embedded documents with custom converters
 * 
 * References:
 * - mongodb.md: MongoDB configuration and best practices
 * - architecture.md: Infrastructure layer configuration
 * - application.yml: Connection properties
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.example.poc.domain")
public class MongoConfig {

    /**
     * Register custom converters for Value Objects
     * 
     * These converters handle serialization/deserialization of:
     * - Money → { amount: BigDecimal, currency: String }
     * - Email → String (normalized lowercase)
     * - Address → Nested document
     */
    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new MoneyToDocumentConverter());
        converters.add(new DocumentToMoneyConverter());
        return new MongoCustomConversions(converters);
    }

    /**
     * Convert Money (Value Object) to MongoDB document
     * 
     * Stores Money as nested document: { amount: BigDecimal, currency: String }
     */
    @WritingConverter
    static class MoneyToDocumentConverter implements Converter<Money, Document> {
        @Override
        public Document convert(Money source) {
            Document doc = new Document();
            doc.put("amount", source.amount());
            doc.put("currency", source.currency());
            return doc;
        }
    }

    /**
     * Convert MongoDB document to Money (Value Object)
     */
    @ReadingConverter
    static class DocumentToMoneyConverter implements Converter<Document, Money> {
        @Override
        public Money convert(Document source) {
            Object amountObj = source.get("amount");
            BigDecimal amount;
            if (amountObj instanceof BigDecimal bd) {
                amount = bd;
            } else if (amountObj instanceof Decimal128 d128) {
                amount = d128.bigDecimalValue();
            } else if (amountObj != null) {
                amount = new BigDecimal(amountObj.toString());
            } else {
                amount = BigDecimal.ZERO;
            }
            String currency = source.getString("currency");
            return new Money(amount, currency);
        }
    }
}
