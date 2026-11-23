package com.epam.booking.model;

import java.time.Instant;
import java.util.Objects;

public record Event(long id, String title, Instant date) implements Entity {
    public Event {
        if (id < 0) {
            throw new IllegalArgumentException("Event ID must not be negative");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Event title must not be blank");
        }
        Objects.requireNonNull(date, "Event date is required");
    }

    @Override
    public Event withId(long id) {
        return new Event(id, title, date);
    }
}
