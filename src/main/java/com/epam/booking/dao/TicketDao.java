package com.epam.booking.dao;

import com.epam.booking.model.Ticket;

import java.util.List;

public class TicketDao extends AbstractMapDao<Ticket> {
    public TicketDao() {
        super("ticket:", Ticket.class);
    }

    public List<Ticket> findByUserId(long userId) {
        return findAll().stream().filter(ticket -> ticket.userId() == userId).toList();
    }

    public List<Ticket> findByEventId(long eventId) {
        return findAll().stream().filter(ticket -> ticket.eventId() == eventId).toList();
    }

    public boolean isPlaceBooked(long eventId, int place) {
        return findByEventId(eventId).stream().anyMatch(ticket -> ticket.place() == place);
    }
}
