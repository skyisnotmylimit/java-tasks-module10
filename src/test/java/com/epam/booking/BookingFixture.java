package com.epam.booking;

import com.epam.booking.dao.EventDao;
import com.epam.booking.dao.TicketDao;
import com.epam.booking.dao.UserDao;
import com.epam.booking.model.Event;
import com.epam.booking.model.User;
import com.epam.booking.service.EventService;
import com.epam.booking.service.TicketService;
import com.epam.booking.service.UserService;
import com.epam.booking.storage.InMemoryStorage;

import java.time.Instant;

final class BookingFixture {
    final InMemoryStorage storage = new InMemoryStorage();
    final UserDao userDao = new UserDao();
    final EventDao eventDao = new EventDao();
    final TicketDao ticketDao = new TicketDao();
    final UserService users = new UserService();
    final EventService events = new EventService();
    final TicketService tickets = new TicketService();
    final User user;
    final Event event;

    BookingFixture() {
        userDao.setStorage(storage);
        eventDao.setStorage(storage);
        ticketDao.setStorage(storage);
        users.setUserDao(userDao);
        users.setTicketDao(ticketDao);
        users.setStorage(storage);
        events.setEventDao(eventDao);
        events.setTicketDao(ticketDao);
        events.setStorage(storage);
        tickets.setTicketDao(ticketDao);
        tickets.setUserDao(userDao);
        tickets.setEventDao(eventDao);
        tickets.setStorage(storage);
        user = userDao.create(new User(0, "Alice", "alice@example.com"));
        event = eventDao.create(new Event(0, "Java Conference", Instant.parse("2030-05-20T09:00:00Z")));
    }
}
