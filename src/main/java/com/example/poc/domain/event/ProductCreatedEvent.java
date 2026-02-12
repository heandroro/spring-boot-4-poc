package com.example.poc.domain.event;

import java.util.Objects;

public class ProductCreatedEvent extends DomainEvent {

    private static final long serialVersionUID = 1L;

    private final String sku;
    private final String name;
    private final String category;
    private final String price;

    public ProductCreatedEvent(String sku, String name, String category, String price) {
        super(sku);
        this.sku = Objects.requireNonNull(sku, "SKU must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.category = Objects.requireNonNull(category, "Category must not be null");
        this.price = Objects.requireNonNull(price, "Price must not be null");
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }
}
