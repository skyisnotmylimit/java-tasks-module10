package com.epam.booking.service;

import com.epam.booking.dao.EventDao;
import com.epam.booking.dao.TicketDao;
import com.epam.booking.exception.BookingConflictException;
import com.epam.booking.model.Event;
import com.epam.booking.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class EventService {
    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);
    private EventDao eventDao;
    private TicketDao ticketDao;
    private InMemoryStorage storage;

    @Autowired
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Autowired
    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public Event getEventById(long id) {
        return eventDao.getById(id);
    }

    public List<Event> getEventsByTitle(String title) {
        return eventDao.findByTitle(Objects.requireNonNull(title, "Title is required"));
    }

    public List<Event> getEventsByDate(Instant date) {
        return eventDao.findByDate(Objects.requireNonNull(date, "Date is required"));
    }

    public Event createEvent(Event event) {
        synchronized (storage) {
            Event saved = eventDao.create(event);
            LOG.info("Created event id={}", saved.id());
            return saved;
        }
    }

    public Event updateEvent(Event event) {
        synchronized (storage) {
            Event saved = eventDao.update(event);
            LOG.info("Updated event id={}", saved.id());
            return saved;
        }
    }

    public boolean deleteEvent(long id) {
        synchronized (storage) {
            if (!ticketDao.findByEventId(id).isEmpty()) {
                throw new BookingConflictException("Cannot delete event " + id + " with booked tickets");
            }
            boolean deleted = eventDao.delete(id);
            LOG.info("Delete event id={}, deleted={}", id, deleted);
            return deleted;
        }
    }
}
