package com.example.poc.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.poc.domain.event.CustomerCreatedEvent;
import com.example.poc.domain.event.DomainEvent;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;

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

    private List<DomainEvent> events = new ArrayList<>();

    private Customer() {

        this.events = new ArrayList<>();
    }

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
        customer.availableCredit = creditLimit; // Initially all credit is available
        customer.status = Status.ACTIVE;
        customer.createdAt = LocalDateTime.now();
        customer.updatedAt = LocalDateTime.now();

        customer.events.add(new CustomerCreatedEvent(
                customer.email.toString(),
                customer.name,
                customer.createdAt));

        return customer;
    }

    public void useCredit(Money amount) {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("Cannot use credit for " + status + " customer");
        }

        if (amount.isGreaterThan(availableCredit)) {
            throw new IllegalArgumentException(
                    "Insufficient credit. Available: " + availableCredit.format() +
                            ", Requested: " + amount.format());
        }

        this.availableCredit = availableCredit.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void restoreCredit(Money amount) {
        Objects.requireNonNull(amount, "Amount must not be null");

        Money newAvailable = availableCredit.add(amount);

        if (newAvailable.isGreaterThan(creditLimit)) {
            throw new IllegalArgumentException(
                    "Cannot restore more credit than limit. Limit: " + creditLimit.format() +
                            ", Current: " + availableCredit.format() +
                            ", Restore: " + amount.format());
        }

        this.availableCredit = newAvailable;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseCreditLimit(Money increase) {
        Objects.requireNonNull(increase, "Increase must not be null");

        if (increase.isZero()) {
            throw new IllegalArgumentException("Increase must be greater than zero");
        }

        this.creditLimit = creditLimit.add(increase);
        this.availableCredit = availableCredit.add(increase);
        this.updatedAt = LocalDateTime.now();
    }

    public void decreaseCreditLimit(Money decrease) {
        Objects.requireNonNull(decrease, "Decrease must not be null");

        if (decrease.isGreaterThan(creditLimit)) {
            throw new IllegalArgumentException(
                    "Cannot decrease credit limit below zero. Limit: " + creditLimit.format());
        }

        Money newLimit = creditLimit.subtract(decrease);

        Money used = creditLimit.subtract(availableCredit);
        if (newLimit.isGreaterThanOrEqual(used)) {

            this.creditLimit = newLimit;
            // Adjust available credit proportionally
            this.availableCredit = newLimit.subtract(used);
        } else {
            throw new IllegalArgumentException(
                    "Cannot reduce limit below current usage: " + used.format());
        }

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

    public boolean canPurchase(Money amount) {
        return this.status == Status.ACTIVE && !amount.isGreaterThan(availableCredit);
    }

    public Money getUsedCredit() {
        return creditLimit.subtract(availableCredit);
    }

    public double getCreditUtilizationPercentage() {
        Money used = getUsedCredit();

        if (creditLimit.isZero()) {
            return 0.0;
        }
        return (used.amount().doubleValue() / creditLimit.amount().doubleValue()) * 100.0;
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> eventsCopy = new ArrayList<>(events);
        events.clear();
        return eventsCopy;
    }

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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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
