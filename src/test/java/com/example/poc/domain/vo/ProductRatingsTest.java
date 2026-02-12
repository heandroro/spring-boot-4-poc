package com.example.poc.domain.vo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductRatings Value Object")
class ProductRatingsTest {

    @Test
    @DisplayName("deve criar ratings inicial")
    void shouldCreateInitialRatings() {
        var ratings = ProductRatings.initial();

        assertEquals(0.0, ratings.average());
        assertEquals(0, ratings.count());
    }

    @Test
    @DisplayName("deve criar ratings com valores válidos")
    void shouldCreateRatingsWithValidValues() {
        var ratings = new ProductRatings(4.5, 10);

        assertEquals(4.5, ratings.average());
        assertEquals(10, ratings.count());
    }

    @Test
    @DisplayName("deve lançar exceção ao criar ratings com average null")
    void shouldThrowExceptionWithNullAverage() {
        assertThrows(NullPointerException.class, () -> new ProductRatings(null, 10));
    }

    @Test
    @DisplayName("deve lançar exceção ao criar ratings com count null")
    void shouldThrowExceptionWithNullCount() {
        assertThrows(NullPointerException.class, () -> new ProductRatings(4.5, null));
    }

    @Test
    @DisplayName("deve lançar exceção ao criar ratings com average negativa")
    void shouldThrowExceptionWithNegativeAverage() {
        assertThrows(IllegalArgumentException.class, () -> new ProductRatings(-1.0, 10));
    }

    @Test
    @DisplayName("deve lançar exceção ao criar ratings com average maior que 5")
    void shouldThrowExceptionWithAverageGreaterThan5() {
        assertThrows(IllegalArgumentException.class, () -> new ProductRatings(5.1, 10));
    }

    @Test
    @DisplayName("deve lançar exceção ao criar ratings com count negativo")
    void shouldThrowExceptionWithNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> new ProductRatings(4.5, -1));
    }

    @Test
    @DisplayName("deve adicionar novo rating")
    void shouldAddNewRating() {
        var ratings = new ProductRatings(4.0, 5);
        var updated = ratings.addRating(5.0);

        assertEquals(4.166666666666667, updated.average(), 0.001);
        assertEquals(6, updated.count());
    }

    @Test
    @DisplayName("deve lançar exceção ao adicionar rating inválido")
    void shouldThrowExceptionWhenAddingInvalidRating() {
        var ratings = ProductRatings.initial();

        assertThrows(IllegalArgumentException.class, () -> ratings.addRating(-1.0));
        assertThrows(IllegalArgumentException.class, () -> ratings.addRating(5.1));
    }

    @Test
    @DisplayName("deve adicionar primeiro rating")
    void shouldAddFirstRating() {
        var ratings = ProductRatings.initial();
        var updated = ratings.addRating(4.5);

        assertEquals(4.5, updated.average());
        assertEquals(1, updated.count());
    }

    @Test
    @DisplayName("deve validar igualdade de ratings")
    void shouldValidateRatingsEquality() {
        var ratings1 = new ProductRatings(4.5, 10);
        var ratings2 = new ProductRatings(4.5, 10);
        var ratings3 = new ProductRatings(3.5, 10);

        assertEquals(ratings1, ratings2);
        assertNotEquals(ratings1, ratings3);
    }

    @Test
    @DisplayName("deve validar hashCode de ratings")
    void shouldValidateRatingsHashCode() {
        var ratings1 = new ProductRatings(4.5, 10);
        var ratings2 = new ProductRatings(4.5, 10);

        assertEquals(ratings1.hashCode(), ratings2.hashCode());
    }

    @Test
    @DisplayName("deve retornar string representation")
    void shouldReturnStringRepresentation() {
        var ratings = new ProductRatings(4.5, 10);
        var str = ratings.toString();

        assertNotNull(str);
        assertTrue(str.contains("4.5"));
        assertTrue(str.contains("10"));
    }

    @Test
    @DisplayName("deve aceitar ratings nos limites 1.0 e 5.0 ao adicionar")
    void shouldAcceptBoundaryRatings() {
        var r = ProductRatings.initial();
        var low = r.addRating(1.0);
        var high = low.addRating(5.0);

        assertEquals(1, low.count());
        assertEquals(2, high.count());
        assertTrue(low.average() >= 1.0 && low.average() <= 1.0);
        assertTrue(high.average() >= 3.0 && high.average() <= 3.0);
    }

    @Test
    @DisplayName("deve lançar exceção ao remover rating inválido mesmo quando existem ratings")
    void shouldThrowWhenRemovingInvalidRating() {
        var r = new ProductRatings(4.0, 2);
        assertThrows(IllegalArgumentException.class, () -> r.removeRating(0.5));
        assertThrows(IllegalArgumentException.class, () -> r.removeRating(6.0));
    }

    @Test
    @DisplayName("deve remover rating válido e calcular nova média não zero")
    void shouldRemoveValidRatingAndComputeAverage() {
        var r = new ProductRatings(3.5, 2); // total 7
        var removed = r.removeRating(3.0); // newTotal 4 /1 = 4.0
        assertEquals(1, removed.count());
        assertEquals(4.0, removed.average());
    }

    @Test
    @DisplayName("deve cobrir hasNoRatings true e false")
    void shouldCoverHasNoRatingsTrueAndFalse() {
        var r0 = ProductRatings.initial();
        assertTrue(r0.hasNoRatings());

        var r1 = new ProductRatings(5.0, 1);
        assertFalse(r1.hasNoRatings());
    }
}
