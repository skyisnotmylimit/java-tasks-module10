package com.epam.booking.model;

public record User(long id, String name, String email) implements Entity {
    public User {
        if (id < 0) {
            throw new IllegalArgumentException("User ID must not be negative");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name must not be blank");
        }
        if (email == null || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("A valid email address is required");
        }
    }

    @Override
    public User withId(long id) {
        return new User(id, name, email);
    }
}
