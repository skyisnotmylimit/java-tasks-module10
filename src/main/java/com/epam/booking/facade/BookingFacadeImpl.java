package com.epam.booking.facade;

import com.epam.booking.model.Event;
import com.epam.booking.model.Ticket;
import com.epam.booking.model.User;
import com.epam.booking.service.EventService;
import com.epam.booking.service.TicketService;
import com.epam.booking.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BookingFacadeImpl implements BookingFacade {
    private final UserService userService;
    private final EventService eventService;
    private final TicketService ticketService;

    public BookingFacadeImpl(UserService userService, EventService eventService, TicketService ticketService) {
        this.userService = Objects.requireNonNull(userService);
        this.eventService = Objects.requireNonNull(eventService);
        this.ticketService = Objects.requireNonNull(ticketService);
    }

    @Override
    public User getUserById(long id) {
        return userService.getUserById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userService.getUserByEmail(email);
    }

    @Override
    public List<User> getUsersByName(String name) {
        return userService.getUsersByName(name);
    }

    @Override
    public User createUser(User user) {
        return userService.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        return userService.updateUser(user);
    }

    @Override
    public boolean deleteUser(long id) {
        return userService.deleteUser(id);
    }

    @Override
    public Event getEventById(long id) {
        return eventService.getEventById(id);
    }

    @Override
    public List<Event> getEventsByTitle(String title) {
        return eventService.getEventsByTitle(title);
    }

    @Override
    public List<Event> getEventsByDate(Instant date) {
        return eventService.getEventsByDate(date);
    }

    @Override
    public Event createEvent(Event event) {
        return eventService.createEvent(event);
    }

    @Override
    public Event updateEvent(Event event) {
        return eventService.updateEvent(event);
    }

    @Override
    public boolean deleteEvent(long id) {
        return eventService.deleteEvent(id);
    }

    @Override
    public Ticket bookTicket(long userId, long eventId, int place, Ticket.Category category) {
        return ticketService.bookTicket(userId, eventId, place, category);
    }

    @Override
    public Ticket getTicketById(long id) {
        return ticketService.getTicketById(id);
    }

    @Override
    public List<Ticket> getBookedTicketsByUser(long userId) {
        return ticketService.getBookedTicketsByUser(userId);
    }

    @Override
    public List<Ticket> getBookedTicketsByEvent(long eventId) {
        return ticketService.getBookedTicketsByEvent(eventId);
    }

    @Override
    public boolean cancelTicket(long id) {
        return ticketService.cancelTicket(id);
    }
}
