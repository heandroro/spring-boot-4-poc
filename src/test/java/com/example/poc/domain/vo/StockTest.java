package com.example.poc.domain.vo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Stock Value Object")
class StockTest {

    @Test
    @DisplayName("deve criar stock inicial")
    void shouldCreateInitialStock() {
        var stock = Stock.initial(10);

        assertEquals(10, stock.available());
        assertEquals(0, stock.reserved());
        assertEquals(10, stock.total());
    }

    @Test
    @DisplayName("deve lançar exceção ao criar stock com quantidade negativa")
    void shouldThrowExceptionWithNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> Stock.initial(-1));
    }

    @Test
    @DisplayName("deve reservar stock")
    void shouldReserveStock() {
        var stock = Stock.initial(10);
        var reserved = stock.reserve(5);

        assertEquals(5, reserved.available());
        assertEquals(5, reserved.reserved());
        assertEquals(10, reserved.total());
    }

    @Test
    @DisplayName("deve lançar exceção ao reservar mais do que disponível")
    void shouldThrowExceptionWhenReservingMoreThanAvailable() {
        var stock = Stock.initial(10);

        assertThrows(IllegalArgumentException.class, () -> stock.reserve(15));
    }

    @Test
    @DisplayName("deve confirmar reserva")
    void shouldConfirmReservation() {
        var stock = Stock.initial(10).reserve(5);
        var confirmed = stock.confirmReservation(5);

        assertEquals(5, confirmed.available());
        assertEquals(0, confirmed.reserved());
        assertEquals(5, confirmed.total());
    }

    @Test
    @DisplayName("deve lançar exceção ao confirmar mais do que reservado")
    void shouldThrowExceptionWhenConfirmingMoreThanReserved() {
        var stock = Stock.initial(10).reserve(5);

        assertThrows(IllegalArgumentException.class, () -> stock.confirmReservation(10));
    }

    @Test
    @DisplayName("deve cancelar reserva")
    void shouldCancelReservation() {
        var stock = Stock.initial(10).reserve(5);
        var cancelled = stock.cancelReservation(3);

        assertEquals(8, cancelled.available());
        assertEquals(2, cancelled.reserved());
        assertEquals(10, cancelled.total());
    }

    @Test
    @DisplayName("deve lançar exceção ao cancelar mais do que reservado")
    void shouldThrowExceptionWhenCancellingMoreThanReserved() {
        var stock = Stock.initial(10).reserve(5);

        assertThrows(IllegalArgumentException.class, () -> stock.cancelReservation(10));
    }

    @Test
    @DisplayName("deve repor stock")
    void shouldReplenishStock() {
        var stock = Stock.initial(10);
        var replenished = stock.replenish(5);

        assertEquals(15, replenished.available());
        assertEquals(0, replenished.reserved());
        assertEquals(15, replenished.total());
    }

    @Test
    @DisplayName("deve lançar exceção ao repor com quantidade não positiva")
    void shouldThrowExceptionWhenReplenishingWithNonPositiveQuantity() {
        var stock = Stock.initial(10);

        assertThrows(IllegalArgumentException.class, () -> stock.replenish(0));
        assertThrows(IllegalArgumentException.class, () -> stock.replenish(-1));
    }

    @Test
    @DisplayName("deve validar igualdade de stocks")
    void shouldValidateStockEquality() {
        var stock1 = Stock.initial(10);
        var stock2 = Stock.initial(10);
        var stock3 = Stock.initial(5);

        assertEquals(stock1, stock2);
        assertNotEquals(stock1, stock3);
    }

    @Test
    @DisplayName("deve validar hashCode de stocks")
    void shouldValidateStockHashCode() {
        var stock1 = Stock.initial(10);
        var stock2 = Stock.initial(10);

        assertEquals(stock1.hashCode(), stock2.hashCode());
    }

    @Test
    @DisplayName("deve retornar string representation")
    void shouldReturnStringRepresentation() {
        var stock = Stock.initial(10).reserve(3);
        var str = stock.toString();

        assertNotNull(str);
        assertTrue(str.contains("available"));
        assertTrue(str.contains("reserved"));
        assertTrue(str.contains("total"));
    }

    @Test
    @DisplayName("deve verificar se tem quantidade disponível")
    void shouldCheckIfHasAvailable() {
        var stock = Stock.initial(10);

        assertTrue(stock.hasAvailable(5));
        assertTrue(stock.hasAvailable(10));
        assertFalse(stock.hasAvailable(15));
        assertFalse(stock.hasAvailable(-1));
    }

    @Test
    @DisplayName("deve verificar se está sem estoque")
    void shouldCheckIfOutOfStock() {
        var stock = Stock.initial(10);
        var emptyStock = Stock.empty();

        assertFalse(stock.isOutOfStock());
        assertTrue(emptyStock.isOutOfStock());
    }

    @Test
    @DisplayName("deve verificar se está com estoque baixo")
    void shouldCheckIfLowStock() {
        var stock = Stock.initial(10);

        assertTrue(stock.isLowStock(15)); // 10 <= 15
        assertTrue(stock.isLowStock(10)); // 10 <= 10
        assertFalse(stock.isLowStock(5)); // 10 > 5
    }
}
