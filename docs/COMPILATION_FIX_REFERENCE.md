# Compilation Fix Reference

## Problem
When implementing credit limit functionality in the Customer domain entity using a Money value object, the method `isGreaterThanOrEqual(Money)` does not exist in the Money class.

## Incorrect Implementation (Causes Compilation Error)

```java
// ❌ WRONG - This will cause compilation error
if (newLimit.isGreaterThanOrEqual(used)) {
    // isGreaterThanOrEqual method doesn't exist
}
```

## Correct Implementation

```java
// ✅ CORRECT - Use logical negation of isGreaterThan
if (!used.isGreaterThan(newLimit)) {
    // This is equivalent to: newLimit >= used
    // Business logic: new limit must be greater than or equal to amount already used
}
```

## Explanation

The Money class provides an `isGreaterThan(Money)` method but not `isGreaterThanOrEqual(Money)`.

To check if `a >= b`, we can use the logical equivalent: `!(b > a)`

- `newLimit >= used` is equivalent to `!(used > newLimit)`
- Therefore use: `!used.isGreaterThan(newLimit)`

## Example Money Class (Minimal)

```java
public record Money(BigDecimal amount, Currency currency) {
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money with different currencies");
        }
    }
}
```

## Example Customer Usage

```java
@Document(collection = "customers")
public class Customer {
    @Id
    private String id;
    private Money creditLimit;
    private Money creditUsed;
    
    public void updateCreditLimit(Money newLimit) {
        // ✅ CORRECT: Check if new limit >= used amount
        if (!creditUsed.isGreaterThan(newLimit)) {
            this.creditLimit = newLimit;
        } else {
            throw new IllegalArgumentException(
                "New credit limit cannot be less than currently used amount"
            );
        }
    }
}
```

## Reference
- Issue: Customer credit limit validation compilation error
- File: `src/main/java/com/example/poc/domain/Customer.java`
- Context: Credit limit must be validated to ensure it's not less than the amount already used
