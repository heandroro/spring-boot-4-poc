package com.example.poc.domain;

import com.example.poc.domain.event.DomainEvent;
import com.example.poc.domain.event.CustomerCreatedEvent;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Customer Aggregate Root
 * 
 * Represents a customer in the system. This is an aggregate root that manages
 * customer-related business logic and domain events.
 * 
 * Design Decision: Uses traditional class (NOT Record) because:
 * 1. Aggregate roots manage mutable state (credit, status changes)
 * 2. Rich domain logic requires multiple methods (useCredit, restoreCredit, etc.)
 * 3. Domain events must be collected and cleared in transient field
 * 4. Constructor must be private to enforce factory method pattern
 * 
 * Value Objects (Money, Email, Address) ARE Records - they're immutable and stateless.
 * 
 * Aggregate Invariants:
 * - A customer must have a unique email
 * - Customer status must be one of valid states (ACTIVE, INACTIVE, SUSPENDED)
 * - Credit limit must be non-negative
 * - Available credit never exceeds credit limit
 * 
 * References:
 * - architecture.md: Aggregate Root pattern, Entity vs Value Object
 * - code-examples.md: Section 22 - Rich Classes, Section 13 - Aggregate Root
 * - testing.md: Unit testing rich domain models
 */
@Document(collection = "customers")
public class Customer {

    public enum Status {
        ACTIVE, INACTIVE, SUSPENDED
    }

    @Id
    private String id;
    
    private String name;
    private Email email;
    private Address address;
    private Money creditLimit;
    private Money availableCredit;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Domain events (not persisted, only used for notification)
    private transient List<DomainEvent> events = new ArrayList<>();

    /**
     * Private constructor - enforces factory method pattern
     * MongoDB and reflection-based frameworks can still instantiate via no-arg constructor
     */
    private Customer() {
        // Initialize transient field to avoid NullPointerException after MongoDB deserialization
        this.events = new ArrayList<>();
    }

    /**
     * Factory method to create a new customer
     * 
     * This is the ONLY way to create a valid Customer instance.
     * It ensures all invariants are met and the CustomerCreatedEvent is raised.
     * 
     * @param name customer name (required, non-blank)
     * @param email customer email (required, valid format)
     * @param address customer address (required, valid)
     * @param creditLimit initial credit limit (required, non-negative)
     * @return newly created Customer with ACTIVE status
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static Customer create(String name, Email email, Address address, Money creditLimit) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(address, "Address must not be null");
        Objects.requireNonNull(creditLimit, "Credit limit must not be null");
        
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        
        Customer customer = new Customer();
        customer.name = name.trim();
        customer.email = email;
        customer.address = address;
        customer.creditLimit = creditLimit;
        customer.availableCredit = creditLimit;  // Initially all credit is available
        customer.status = Status.ACTIVE;
        customer.createdAt = LocalDateTime.now();
        customer.updatedAt = LocalDateTime.now();
        
        // Raise domain event
        customer.events.add(new CustomerCreatedEvent(
            customer.email.toString(),
            customer.name,
            customer.createdAt
        ));
        
        return customer;
    }

    // ==================== Business Logic Methods ====================

    /**
     * Use credit for a purchase
     * 
     * @param amount amount to deduct from available credit
     * @throws IllegalStateException if customer is not ACTIVE
     * @throws IllegalArgumentException if amount exceeds available credit
     */
    public void useCredit(Money amount) {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("Cannot use credit for " + status + " customer");
        }
        
        if (amount.isGreaterThan(availableCredit)) {
            throw new IllegalArgumentException(
                "Insufficient credit. Available: " + availableCredit.format() + 
                ", Requested: " + amount.format()
            );
        }
        
        this.availableCredit = availableCredit.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Restore credit (e.g., after returning a purchase)
     * 
     * @param amount amount to add back to available credit
     * @throws IllegalArgumentException if restored credit would exceed limit
     */
    public void restoreCredit(Money amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        
        Money newAvailable = availableCredit.add(amount);
        
        if (newAvailable.isGreaterThan(creditLimit)) {
            throw new IllegalArgumentException(
                "Cannot restore more credit than limit. Limit: " + creditLimit.format() +
                ", Current: " + availableCredit.format() +
                ", Restore: " + amount.format()
            );
        }
        
        this.availableCredit = newAvailable;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Increase credit limit (business operation, e.g., after paying on time)
     */
    public void increaseCreditLimit(Money increase) {
        Objects.requireNonNull(increase, "Increase must not be null");
        
        if (increase.isZero()) {
            throw new IllegalArgumentException("Increase must be greater than zero");
        }
        
        this.creditLimit = creditLimit.add(increase);
        this.availableCredit = availableCredit.add(increase);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Decrease credit limit
     */
    public void decreaseCreditLimit(Money decrease) {
        Objects.requireNonNull(decrease, "Decrease must not be null");
        
        if (decrease.isGreaterThan(creditLimit)) {
            throw new IllegalArgumentException(
                "Cannot decrease credit limit below zero. Limit: " + creditLimit.format()
            );
        }
        
        Money newLimit = creditLimit.subtract(decrease);
        
        // Can't reduce limit below current usage
        Money used = creditLimit.subtract(availableCredit);
        if (newLimit.isGreaterThanOrEqual(used)) {
            // It's ok to reduce (even if newLimit equals used, resulting in zero available credit)
            this.creditLimit = newLimit;
            // Adjust available credit proportionally
            this.availableCredit = newLimit.subtract(used);
        } else {
            throw new IllegalArgumentException(
                "Cannot reduce limit below current usage: " + used.format()
            );
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update customer status
     */
    public void setStatus(Status newStatus) {
        Objects.requireNonNull(newStatus, "Status must not be null");
        
        if (this.status == newStatus) {
            return; // No change needed
        }
        
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if customer can make a purchase of given amount
     */
    public boolean canPurchase(Money amount) {
        return this.status == Status.ACTIVE && !amount.isGreaterThan(availableCredit);
    }

    /**
     * Get used credit (total - available)
     */
    public Money getUsedCredit() {
        return creditLimit.subtract(availableCredit);
    }

    /**
     * Get credit utilization percentage
     */
    public double getCreditUtilizationPercentage() {
        Money used = getUsedCredit();
        // Avoid division by zero
        if (creditLimit.isZero()) {
            return 0.0;
        }
        return (used.amount().doubleValue() / creditLimit.amount().doubleValue()) * 100.0;
    }

    // ==================== Event Management ====================

    /**
     * Get and clear domain events
     * This is called after the aggregate is persisted
     */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> eventsCopy = new ArrayList<>(events);
        events.clear();
        return eventsCopy;
    }

    // ==================== Getters ====================

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public Money getCreditLimit() {
        return creditLimit;
    }

    public Money getAvailableCredit() {
        return availableCredit;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) && Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email=" + email +
                ", status=" + status +
                ", availableCredit=" + availableCredit.format() +
                ", creditLimit=" + creditLimit.format() +
                '}';
    }
}
