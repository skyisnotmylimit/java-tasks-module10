package com.epam.booking.dao;

import com.epam.booking.model.Event;

import java.time.Instant;
import java.util.List;

public class EventDao extends AbstractMapDao<Event> {
    public EventDao() {
        super("event:", Event.class);
    }

    public List<Event> findByTitle(String title) {
        return findAll().stream().filter(event -> event.title().equalsIgnoreCase(title)).toList();
    }

    public List<Event> findByDate(Instant date) {
        return findAll().stream().filter(event -> event.date().equals(date)).toList();
    }
}
