package com.example.poc.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.poc.domain.event.DomainEvent;
import com.example.poc.domain.event.ProductCreatedEvent;
import com.example.poc.domain.vo.Money;
import com.example.poc.domain.vo.ProductImage;
import com.example.poc.domain.vo.ProductRatings;
import com.example.poc.domain.vo.Stock;

@Document(collection = "products")
public class Product {

    public enum Status {
        ACTIVE, INACTIVE, DISCONTINUED
    }

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private String name;
    private String description;

    @Indexed
    private String category;

    private Money price;
    private Stock stock;
    private Map<String, Object> specifications;
    private List<ProductImage> images;
    private ProductRatings ratings;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<DomainEvent> events = new ArrayList<>();

    private Product() {
        this.events = new ArrayList<>();
    }

    public static Product create(String sku, String name, String description, String category,
            Money price, Integer initialStock, List<ProductImage> images) {
        Objects.requireNonNull(sku, "SKU must not be null");
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(category, "Category must not be null");
        Objects.requireNonNull(price, "Price must not be null");
        Objects.requireNonNull(initialStock, "Initial stock must not be null");

        if (sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        if (category.isBlank()) {
            throw new IllegalArgumentException("Category must not be blank");
        }

        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock must be non-negative");
        }

        Product product = new Product();
        product.sku = sku.trim();
        product.name = name.trim();
        product.description = description != null ? description.trim() : null;
        product.category = category.trim();
        product.price = price;
        product.stock = Stock.initial(initialStock);
        product.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        product.specifications = Map.of();
        product.ratings = ProductRatings.initial();
        product.status = Status.ACTIVE;
        product.createdAt = LocalDateTime.now();
        product.updatedAt = LocalDateTime.now();

        product.events.add(new ProductCreatedEvent(
                product.sku,
                product.name,
                product.category,
                product.price.amount().toPlainString()));

        return product;
    }

    public void updatePrice(Money newPrice) {
        Objects.requireNonNull(newPrice, "New price must not be null");

        if (!this.status.equals(Status.ACTIVE)) {
            throw new IllegalStateException("Cannot update price for " + status + " product");
        }

        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDescription(String newDescription) {
        if (newDescription != null && newDescription.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank if provided");
        }

        this.description = newDescription != null ? newDescription.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSpecifications(Map<String, Object> newSpecifications) {
        Objects.requireNonNull(newSpecifications, "Specifications must not be null");

        this.specifications = Map.copyOf(newSpecifications);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateImages(List<ProductImage> newImages) {
        Objects.requireNonNull(newImages, "Images must not be null");

        if (newImages.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required");
        }

        this.images = new ArrayList<>(newImages);
        this.updatedAt = LocalDateTime.now();
    }

    public void reserveStock(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        this.stock = stock.reserve(quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public void confirmReservation(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        this.stock = stock.confirmReservation(quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelReservation(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        this.stock = stock.cancelReservation(quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public void replenishStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }

        this.stock = stock.replenish(quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRatings(ProductRatings newRatings) {
        Objects.requireNonNull(newRatings, "Ratings must not be null");

        this.ratings = newRatings;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStatus(Status newStatus) {
        Objects.requireNonNull(newStatus, "Status must not be null");

        if (this.status == newStatus) {
            return;
        }

        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> eventsCopy = new ArrayList<>(events);
        events.clear();
        return eventsCopy;
    }

    public String getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Money getPrice() {
        return price;
    }

    public Stock getStock() {
        return stock;
    }

    public Map<String, Object> getSpecifications() {
        return Map.copyOf(specifications);
    }

    public List<ProductImage> getImages() {
        return new ArrayList<>(images);
    }

    public ProductRatings getRatings() {
        return ratings;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) && Objects.equals(sku, product.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sku);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price.amount() + " " + price.currency() +
                ", status=" + status +
                '}';
    }
}

