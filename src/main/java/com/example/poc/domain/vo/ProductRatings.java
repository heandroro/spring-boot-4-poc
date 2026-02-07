package com.example.poc.domain.vo;

import java.util.Objects;

public record ProductRatings(
        Double average,
        Integer count) {

    public ProductRatings {
        Objects.requireNonNull(average, "Average must not be null");
        Objects.requireNonNull(count, "Count must not be null");

        if (average < 0.0 || average > 5.0) {
            throw new IllegalArgumentException(
                    "Average rating must be between 0.0 and 5.0, got: " + average);
        }

        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative, got: " + count);
        }

        if (count == 0 && average != 0.0) {
            throw new IllegalArgumentException(
                    "Average must be 0.0 when count is 0");
        }
    }

    public static ProductRatings initial() {
        return new ProductRatings(0.0, 0);
    }

    public ProductRatings addRating(Double rating) {
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException(
                    "Rating must be between 1.0 and 5.0, got: " + rating);
        }

        int newCount = count + 1;
        double newAverage = ((average * count) + rating) / newCount;

        return new ProductRatings(newAverage, newCount);
    }

    public ProductRatings removeRating(Double rating) {
        if (count == 0) {
            throw new IllegalStateException("Cannot remove rating from product with no ratings");
        }

        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException(
                    "Rating must be between 1.0 and 5.0, got: " + rating);
        }

        int newCount = count - 1;
        double newAverage = newCount == 0 ? 0.0 : ((average * count) - rating) / newCount;

        return new ProductRatings(newAverage, newCount);
    }

    public boolean hasNoRatings() {
        return count == 0;
    }
}

