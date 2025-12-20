package com.example.poc.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.poc.domain.event.DomainEvent;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;

@DisplayName("Customer Aggregate Root Tests")
class CustomerTest {

    private Email validEmail;
    private Address validAddress;
    private Money validCreditLimit;

    @BeforeEach
    void setUp() {
        validEmail = new Email("customer@example.com");
        validAddress = Address.of("123 Main St", "Springfield", "IL", "62701");
        validCreditLimit = Money.of(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("should create customer with factory method")
    void testCreateCustomer() {
        // When
        Customer customer = Customer.create(
                "John Doe",
                validEmail,
                validAddress,
                validCreditLimit);

        // Then
        assertEquals("John Doe", customer.getName());
        assertEquals(validEmail, customer.getEmail());
        assertEquals(validAddress, customer.getAddress());
        assertEquals(validCreditLimit, customer.getCreditLimit());
        assertEquals(Customer.Status.ACTIVE, customer.getStatus());
        assertEquals(validCreditLimit, customer.getAvailableCredit());
    }

    @Test
    @DisplayName("should raise CustomerCreatedEvent on creation")
    void testRaisesCustomerCreatedEvent() {
        // When
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);

        // Then
        List<DomainEvent> events = customer.pullEvents();
        assertEquals(1, events.size());
        assertEquals("CustomerCreatedEvent", events.get(0).getEventType());
    }

    @Test
    @DisplayName("should reject null name")
    void testRejectNullName() {
        // Expect
        assertThrows(
                NullPointerException.class,
                () -> Customer.create(null, validEmail, validAddress, validCreditLimit));
    }

    @Test
    @DisplayName("should reject blank name")
    void testRejectBlankName() {
        // Expect
        assertThrows(
                IllegalArgumentException.class,
                () -> Customer.create("   ", validEmail, validAddress, validCreditLimit));
    }

    @Test
    @DisplayName("should reject null email")
    void testRejectNullEmail() {
        // Expect
        assertThrows(
                NullPointerException.class,
                () -> Customer.create("John Doe", null, validAddress, validCreditLimit));
    }

    @Test
    @DisplayName("should reject null address")
    void testRejectNullAddress() {
        // Expect
        assertThrows(
                NullPointerException.class,
                () -> Customer.create("John Doe", validEmail, null, validCreditLimit));
    }

    @Test
    @DisplayName("should reject null credit limit")
    void testRejectNullCreditLimit() {
        // Expect
        assertThrows(
                NullPointerException.class,
                () -> Customer.create("John Doe", validEmail, validAddress, null));
    }

    @Test
    @DisplayName("should use credit successfully")
    void testUseCreditSuccessfully() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        Money purchaseAmount = Money.of(new BigDecimal("200.00"));

        // When
        customer.useCredit(purchaseAmount);

        // Then
        assertEquals(new BigDecimal("800.00"), customer.getAvailableCredit().amount());
    }

    @Test
    @DisplayName("should reject using credit for inactive customer")
    void testRejectCreditForInactiveCustomer() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        customer.setStatus(Customer.Status.INACTIVE);
        Money purchaseAmount = Money.of(new BigDecimal("100.00"));

        // Expect
        assertThrows(IllegalStateException.class, () -> customer.useCredit(purchaseAmount));
    }

    @Test
    @DisplayName("should reject using credit for suspended customer")
    void testRejectCreditForSuspendedCustomer() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        customer.setStatus(Customer.Status.SUSPENDED);
        Money purchaseAmount = Money.of(new BigDecimal("100.00"));

        // Expect
        assertThrows(IllegalStateException.class, () -> customer.useCredit(purchaseAmount));
    }

    @Test
    @DisplayName("should reject using more credit than available")
    void testRejectExcessiveCredit() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        Money excessiveAmount = Money.of(new BigDecimal("2000.00"));

        // Expect
        assertThrows(IllegalArgumentException.class, () -> customer.useCredit(excessiveAmount));
    }

    @Test
    @DisplayName("should restore credit successfully")
    void testRestoreCreditSuccessfully() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        customer.useCredit(Money.of(new BigDecimal("300.00")));

        // When
        customer.restoreCredit(Money.of(new BigDecimal("100.00")));

        // Then
        assertEquals(new BigDecimal("800.00"), customer.getAvailableCredit().amount());
    }

    @Test
    @DisplayName("should reject restoring credit beyond limit")
    void testRejectExcessiveRestore() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);

        // Expect
        assertThrows(
                IllegalArgumentException.class,
                () -> customer.restoreCredit(Money.of(new BigDecimal("500.00"))));
    }

    @Test
    @DisplayName("should increase credit limit")
    void testIncreaseCreditLimit() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        Money increase = Money.of(new BigDecimal("500.00"));

        // When
        customer.increaseCreditLimit(increase);

        // Then
        assertEquals(new BigDecimal("1500.00"), customer.getCreditLimit().amount());
        assertEquals(new BigDecimal("1500.00"), customer.getAvailableCredit().amount());
    }

    @Test
    @DisplayName("should reject zero increase for credit limit")
    void testRejectZeroIncreaseForCreditLimit() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);

        // Expect
        assertThrows(
                IllegalArgumentException.class,
                () -> customer.increaseCreditLimit(Money.of(BigDecimal.ZERO)));
    }

    @Test
    @DisplayName("should decrease credit limit")
    void testDecreaseCreditLimit() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        Money decrease = Money.of(new BigDecimal("300.00"));

        // When
        customer.decreaseCreditLimit(decrease);

        // Then
        assertEquals(new BigDecimal("700.00"), customer.getCreditLimit().amount());
    }

    @Test
    @DisplayName("should not decrease credit limit below zero")
    void testRejectDecreaseBelowZero() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        Money excessiveDecrease = Money.of(new BigDecimal("2000.00"));

        // Expect
        assertThrows(IllegalArgumentException.class, () -> customer.decreaseCreditLimit(excessiveDecrease));
    }

    @Test
    @DisplayName("should change customer status")
    void testChangeStatus() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);

        // When
        customer.setStatus(Customer.Status.SUSPENDED);

        // Then
        assertEquals(Customer.Status.SUSPENDED, customer.getStatus());
    }

    @Test
    @DisplayName("should check if customer can purchase")
    void testCanPurchase() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        Money purchaseAmount = Money.of(new BigDecimal("500.00"));

        // Then
        assertTrue(customer.canPurchase(purchaseAmount));
    }

    @Test
    @DisplayName("should reject purchase when inactive")
    void testCannotPurchaseWhenInactive() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        customer.setStatus(Customer.Status.INACTIVE);
        Money purchaseAmount = Money.of(new BigDecimal("500.00"));

        // Then
        assertFalse(customer.canPurchase(purchaseAmount));
    }

    @Test
    @DisplayName("should reject purchase exceeding credit")
    void testCannotPurchaseExceedingCredit() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        Money excessiveAmount = Money.of(new BigDecimal("2000.00"));

        // Then
        assertFalse(customer.canPurchase(excessiveAmount));
    }

    @Test
    @DisplayName("should calculate used credit")
    void testGetUsedCredit() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        customer.useCredit(Money.of(new BigDecimal("400.00")));

        // When
        Money usedCredit = customer.getUsedCredit();

        // Then
        assertEquals(new BigDecimal("400.00"), usedCredit.amount());
    }

    @Test
    @DisplayName("should calculate credit utilization percentage")
    void testGetCreditUtilizationPercentage() {
        // Given
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        customer.useCredit(Money.of(new BigDecimal("250.00")));

        // When
        double utilization = customer.getCreditUtilizationPercentage();

        // Then
        assertEquals(25.0, utilization, 0.01);
    }

    @ParameterizedTest
    @ValueSource(strings = { "100.00", "500.00", "1000.00" })
    @DisplayName("should support various credit limits")
    void testVariousCreditLimits(String limitStr) {
        // When
        Customer customer = Customer.create(
                "John Doe",
                validEmail,
                validAddress,
                Money.of(limitStr));

        // Then
        assertNotNull(customer.getCreditLimit());
        assertEquals(Customer.Status.ACTIVE, customer.getStatus());
    }

    @Test
    @DisplayName("should be identified by email")
    void testCustomerIdentity() {
        // Given
        Email email = new Email("unique@example.com");
        Customer customer1 = Customer.create("John Doe", email, validAddress, validCreditLimit);
        Customer customer2 = Customer.create("John Doe", email, validAddress, validCreditLimit);

        // Then
        assertEquals(customer1, customer2); // Same email = same customer
    }

    @Test
    @DisplayName("equals returns true for same instance and false for null and different types")
    void testEqualsEdgeCases() {
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);

        assertTrue(customer.equals(customer)); // same reference
        assertFalse(customer.equals(null)); // null
        assertFalse(customer.equals("not a customer")); // different class
    }

    @Test
    @DisplayName("equals is sensitive to id when present")
    void testEqualsRespectsIdWhenPresent() throws Exception {
        Email email = new Email("unique2@example.com");
        Customer c1 = Customer.create("John Doe", email, validAddress, validCreditLimit);
        Customer c2 = Customer.create("John Doe", email, validAddress, validCreditLimit);

        Field idField = Customer.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(c1, "id1");
        idField.set(c2, "id2");

        assertFalse(c1.equals(c2));

        idField.set(c2, "id1");
        assertTrue(c1.equals(c2));
    }

    @Test
    @DisplayName("equals returns false for different emails")
    void testEqualsDifferentEmail() {
        Customer c1 = Customer.create("John Doe", new Email("a@example.com"), validAddress, validCreditLimit);
        Customer c2 = Customer.create("John Doe", new Email("b@example.com"), validAddress, validCreditLimit);

        assertFalse(c1.equals(c2));
    }

    @Test
    @DisplayName("should clear events after pull")
    void shouldClearEventsAfterPull() {
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);

        List<DomainEvent> firstPull = customer.pullEvents();
        List<DomainEvent> secondPull = customer.pullEvents();

        assertEquals(1, firstPull.size());
        assertTrue(secondPull.isEmpty());
    }

    @Test
    @DisplayName("should keep status unchanged when same status is set")
    void shouldKeepStatusWhenSame() {
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        Customer.Status initial = customer.getStatus();
        var initialUpdatedAt = customer.getUpdatedAt();

        customer.setStatus(Customer.Status.ACTIVE);

        assertEquals(initial, customer.getStatus());
        assertEquals(initialUpdatedAt, customer.getUpdatedAt());
    }

    @Test
    @DisplayName("should reject decrease credit below used amount")
    void shouldRejectDecreaseBelowUsedAmount() {
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        customer.useCredit(Money.of(new BigDecimal("400.00")));

        Money excessiveDecrease = Money.of(new BigDecimal("700.00"));

        assertThrows(IllegalArgumentException.class, () -> customer.decreaseCreditLimit(excessiveDecrease));
    }

    @Test
    @DisplayName("should return zero utilization when credit limit is zero")
    void shouldReturnZeroUtilizationWhenLimitZero() {
        Customer zeroLimitCustomer = Customer.create(
                "Zero Limit",
                validEmail,
                validAddress,
                Money.of(BigDecimal.ZERO));

        assertEquals(0.0, zeroLimitCustomer.getCreditUtilizationPercentage());
    }

    @Test
    @DisplayName("should reject restore credit when amount is null")
    void shouldRejectRestoreCreditWhenNull() {
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);

        assertThrows(NullPointerException.class, () -> customer.restoreCredit(null));
    }

    @Test
    @DisplayName("getCreatedAt should be set on creation")
    void testGetCreatedAtIsSet() {
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    @DisplayName("hashCode should change when id is set")
    void testHashCodeReflectsIdAndEmail() throws Exception {
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        int before = customer.hashCode();

        Field idField = Customer.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(customer, "id-123");

        int after = customer.hashCode();
        assertNotEquals(before, after);
        assertEquals(Objects.hash("id-123", customer.getEmail()), after);
    }

    @Test
    @DisplayName("toString contains key fields")
    void testToStringContainsFields() {
        Customer customer = Customer.create("John Doe", validEmail, validAddress, validCreditLimit);
        String s = customer.toString();

        assertTrue(s.contains("John Doe"));
        assertTrue(s.contains(validEmail.toString()));
        assertTrue(s.contains(validCreditLimit.format()));
    }

    @Test
    @DisplayName("events field is not transient")
    void shouldNotMarkEventsFieldAsTransient() throws Exception {
        Field eventsField = Customer.class.getDeclaredField("events");
        assertFalse(Modifier.isTransient(eventsField.getModifiers()));
    }
}
