package com.epam.booking.model;

import java.util.Objects;

public record Ticket(long id, long userId, long eventId, int place, Category category) implements Entity {
    public enum Category {
        STANDARD, PREMIUM, BAR
    }

    public Ticket {
        if (id < 0 || userId <= 0 || eventId <= 0) {
            throw new IllegalArgumentException("Ticket ID must not be negative; user and event IDs must be positive");
        }
        if (place <= 0) {
            throw new IllegalArgumentException("Ticket place must be positive");
        }
        Objects.requireNonNull(category, "Ticket category is required");
    }

    @Override
    public Ticket withId(long id) {
        return new Ticket(id, userId, eventId, place, category);
    }
}
