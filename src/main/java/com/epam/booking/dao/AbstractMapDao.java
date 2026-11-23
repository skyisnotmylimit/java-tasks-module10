package com.epam.booking.dao;

import com.epam.booking.exception.EntityNotFoundException;
import com.epam.booking.model.Entity;
import com.epam.booking.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public abstract class AbstractMapDao<T extends Entity> {
    private final String namespace;
    private final Class<T> type;
    protected InMemoryStorage storage;

    protected AbstractMapDao(String namespace, Class<T> type) {
        this.namespace = namespace;
        this.type = type;
    }

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public T create(T entity) {
        return storage.create(namespace, entity, type);
    }

    public T update(T entity) {
        return storage.update(namespace, entity, type);
    }

    public Optional<T> findById(long id) {
        return storage.find(namespace, id, type);
    }

    public T getById(long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(type.getSimpleName(), id));
    }

    public List<T> findAll() {
        return storage.findAll(namespace, type);
    }

    public boolean delete(long id) {
        return storage.delete(namespace, id);
    }
}
