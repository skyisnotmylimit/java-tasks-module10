package com.epam.booking.service;

import com.epam.booking.dao.EventDao;
import com.epam.booking.dao.TicketDao;
import com.epam.booking.dao.UserDao;
import com.epam.booking.exception.BookingConflictException;
import com.epam.booking.model.Ticket;
import com.epam.booking.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TicketService {
    private static final Logger LOG = LoggerFactory.getLogger(TicketService.class);
    private TicketDao ticketDao;
    private UserDao userDao;
    private EventDao eventDao;
    private InMemoryStorage storage;

    @Autowired
    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public Ticket bookTicket(long userId, long eventId, int place, Ticket.Category category) {
        Ticket requested = new Ticket(0, userId, eventId, place, category);
        synchronized (storage) {
            userDao.getById(userId);
            eventDao.getById(eventId);
            if (ticketDao.isPlaceBooked(eventId, place)) {
                LOG.warn("Booking rejected: event id={}, place={} is already booked", eventId, place);
                throw new BookingConflictException("Place " + place + " is already booked for event " + eventId);
            }
            Ticket saved = ticketDao.create(requested);
            LOG.info("Booked ticket id={}, user id={}, event id={}, place={}",
                    saved.id(), userId, eventId, place);
            return saved;
        }
    }

    public Ticket getTicketById(long id) {
        return ticketDao.getById(id);
    }

    public List<Ticket> getBookedTicketsByUser(long userId) {
        userDao.getById(userId);
        return ticketDao.findByUserId(userId);
    }

    public List<Ticket> getBookedTicketsByEvent(long eventId) {
        eventDao.getById(eventId);
        return ticketDao.findByEventId(eventId);
    }

    public boolean cancelTicket(long id) {
        synchronized (storage) {
            boolean cancelled = ticketDao.delete(id);
            LOG.info("Cancel ticket id={}, cancelled={}", id, cancelled);
            return cancelled;
        }
    }
}
