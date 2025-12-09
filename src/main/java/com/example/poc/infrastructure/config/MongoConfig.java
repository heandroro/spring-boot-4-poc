package com.example.poc.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // Mongo auditing enabled (createdAt/updatedAt) for @CreatedDate/@LastModifiedDate
}
