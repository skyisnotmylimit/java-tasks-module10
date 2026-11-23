package com.epam.booking.facade;

import com.epam.booking.model.Event;
import com.epam.booking.model.Ticket;
import com.epam.booking.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingFacade {
    User getUserById(long id);

    Optional<User> getUserByEmail(String email);

    List<User> getUsersByName(String name);

    User createUser(User user);

    User updateUser(User user);

    boolean deleteUser(long id);

    Event getEventById(long id);

    List<Event> getEventsByTitle(String title);

    List<Event> getEventsByDate(Instant date);

    Event createEvent(Event event);

    Event updateEvent(Event event);

    boolean deleteEvent(long id);

    Ticket bookTicket(long userId, long eventId, int place, Ticket.Category category);

    Ticket getTicketById(long id);

    List<Ticket> getBookedTicketsByUser(long userId);

    List<Ticket> getBookedTicketsByEvent(long eventId);

    boolean cancelTicket(long id);
}
