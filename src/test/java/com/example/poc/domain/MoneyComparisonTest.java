package com.example.poc.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Demonstrates the correct pattern for comparing Money values.
 * 
 * This test addresses the compilation error mentioned in job 57984872962:
 * - WRONG: newLimit.isGreaterThanOrEqual(used) - method doesn't exist
 * - CORRECT: !used.isGreaterThan(newLimit) - equivalent to newLimit >= used
 */
@DisplayName("Money Comparison - Correct Usage Pattern")
class MoneyComparisonTest {
    
    private static final Currency USD = Currency.getInstance("USD");
    
    @Test
    @DisplayName("Should correctly check if new limit >= used amount using negation of isGreaterThan")
    void shouldCheckGreaterThanOrEqualUsingNegation() {
        var used = new Money(new BigDecimal("100.00"), USD);
        var newLimitHigher = new Money(new BigDecimal("150.00"), USD);
        var newLimitEqual = new Money(new BigDecimal("100.00"), USD);
        var newLimitLower = new Money(new BigDecimal("50.00"), USD);
        
        // CORRECT PATTERN: To check if newLimit >= used, use !used.isGreaterThan(newLimit)
        
        // Case 1: newLimit (150) >= used (100) should be true
        assertThat(!used.isGreaterThan(newLimitHigher))
            .as("New limit 150 should be >= used 100")
            .isTrue();
        
        // Case 2: newLimit (100) >= used (100) should be true
        assertThat(!used.isGreaterThan(newLimitEqual))
            .as("New limit 100 should be >= used 100")
            .isTrue();
        
        // Case 3: newLimit (50) >= used (100) should be false
        assertThat(!used.isGreaterThan(newLimitLower))
            .as("New limit 50 should NOT be >= used 100")
            .isFalse();
    }
    
    @Test
    @DisplayName("Should demonstrate credit limit validation logic")
    void shouldValidateCreditLimitUpdate() {
        var creditUsed = new Money(new BigDecimal("500.00"), USD);
        
        // Scenario 1: Increasing limit (valid)
        var newLimitHigher = new Money(new BigDecimal("1000.00"), USD);
        assertThat(canUpdateCreditLimit(creditUsed, newLimitHigher))
            .as("Should allow increasing credit limit above used amount")
            .isTrue();
        
        // Scenario 2: Setting limit equal to used (valid)
        var newLimitEqual = new Money(new BigDecimal("500.00"), USD);
        assertThat(canUpdateCreditLimit(creditUsed, newLimitEqual))
            .as("Should allow setting credit limit equal to used amount")
            .isTrue();
        
        // Scenario 3: Decreasing limit below used (invalid)
        var newLimitLower = new Money(new BigDecimal("300.00"), USD);
        assertThat(canUpdateCreditLimit(creditUsed, newLimitLower))
            .as("Should NOT allow decreasing credit limit below used amount")
            .isFalse();
    }
    
    @Test
    @DisplayName("Should work with isGreaterThan for strict comparison")
    void shouldUseIsGreaterThanForStrictComparison() {
        var money1 = new Money(new BigDecimal("100.00"), USD);
        var money2 = new Money(new BigDecimal("50.00"), USD);
        var money3 = new Money(new BigDecimal("100.00"), USD);
        
        // Strict greater than
        assertThat(money1.isGreaterThan(money2)).isTrue();
        assertThat(money2.isGreaterThan(money1)).isFalse();
        assertThat(money1.isGreaterThan(money3)).isFalse(); // Equal values
    }
    
    @Test
    @DisplayName("Should throw exception when comparing different currencies")
    void shouldThrowExceptionForDifferentCurrencies() {
        var usd = new Money(new BigDecimal("100.00"), USD);
        var eur = new Money(new BigDecimal("100.00"), Currency.getInstance("EUR"));
        
        assertThatThrownBy(() -> usd.isGreaterThan(eur))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot operate on different currencies");
    }
    
    /**
     * Helper method demonstrating the correct pattern for credit limit validation.
     * This is the pattern that should be used at line 193 in Customer.java
     * to avoid the compilation error.
     *
     * @param used the amount of credit currently used
     * @param newLimit the proposed new credit limit
     * @return true if newLimit >= used (valid), false otherwise
     */
    private boolean canUpdateCreditLimit(Money used, Money newLimit) {
        // CORRECT: Use !used.isGreaterThan(newLimit) to check if newLimit >= used
        // This is equivalent to: newLimit >= used
        // WRONG would be: newLimit.isGreaterThanOrEqual(used) - method doesn't exist!
        return !used.isGreaterThan(newLimit);
    }
}
