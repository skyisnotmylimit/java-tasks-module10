package com.epam.booking.storage;

import com.epam.booking.exception.EntityNotFoundException;
import com.epam.booking.model.Entity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryStorage {
    private final Map<String, Entity> entities = new HashMap<>();
    private final Map<String, Long> sequences = new HashMap<>();

    public synchronized <T extends Entity> T create(String namespace, T entity, Class<T> type) {
        long id = entity.id() == 0 ? Math.addExact(sequences.getOrDefault(namespace, 0L), 1) : entity.id();
        String key = namespace + id;
        if (entities.containsKey(key)) {
            throw new IllegalArgumentException("Entity already exists: " + key);
        }
        T saved = type.cast(entity.withId(id));
        entities.put(key, saved);
        sequences.merge(namespace, id, Math::max);
        return saved;
    }

    public synchronized <T extends Entity> T update(String namespace, T entity, Class<T> type) {
        String key = namespace + entity.id();
        if (!entities.containsKey(key)) {
            throw new EntityNotFoundException(type.getSimpleName(), entity.id());
        }
        entities.put(key, entity);
        return entity;
    }

    public synchronized <T extends Entity> Optional<T> find(String namespace, long id, Class<T> type) {
        return Optional.ofNullable(entities.get(namespace + id)).map(type::cast);
    }

    public synchronized <T extends Entity> List<T> findAll(String namespace, Class<T> type) {
        return entities.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(namespace))
                .map(entry -> type.cast(entry.getValue()))
                .sorted(Comparator.comparingLong(Entity::id))
                .toList();
    }

    public synchronized boolean delete(String namespace, long id) {
        return entities.remove(namespace + id) != null;
    }

    public synchronized Map<String, Entity> snapshot() {
        return Map.copyOf(entities);
    }
}
