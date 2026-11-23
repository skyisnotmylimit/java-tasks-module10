package com.epam.booking.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityType, long id) {
        super(entityType + " with ID " + id + " was not found");
    }
}
