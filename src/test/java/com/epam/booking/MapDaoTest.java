package com.epam.booking;

import com.epam.booking.exception.EntityNotFoundException;
import com.epam.booking.model.Event;
import com.epam.booking.model.Ticket;
import com.epam.booking.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MapDaoTest {
    private final BookingFixture fixture = new BookingFixture();

    @Test
    void sharesOneMapButSeparatesEntityNamespaces() {
        Ticket ticket = fixture.ticketDao.create(new Ticket(0, fixture.user.id(), fixture.event.id(),
                7, Ticket.Category.PREMIUM));

        assertEquals(Set.of("user:1", "event:1", "ticket:1"), fixture.storage.snapshot().keySet());
        assertEquals(List.of(fixture.user), fixture.userDao.findAll());
        assertEquals(List.of(fixture.event), fixture.eventDao.findAll());
        assertEquals(List.of(ticket), fixture.ticketDao.findAll());
        assertEquals(ticket, fixture.ticketDao.getById(ticket.id()));
    }

    @Test
    void updatesAndDeletesWithoutAffectingOtherNamespaces() {
        User updated = new User(fixture.user.id(), "Updated", "updated@example.com");
        assertEquals(updated, fixture.userDao.update(updated));
        assertEquals(updated, fixture.userDao.getById(updated.id()));
        assertTrue(fixture.userDao.delete(updated.id()));
        assertFalse(fixture.userDao.delete(updated.id()));
        assertTrue(fixture.userDao.findById(updated.id()).isEmpty());
        assertEquals(fixture.event, fixture.eventDao.getById(fixture.event.id()));
        assertThrows(EntityNotFoundException.class, () -> fixture.userDao.update(updated));
    }

    @Test
    void explicitIdsAdvanceSequenceAndDeletedIdsAreNotReused() {
        fixture.userDao.create(new User(50, "Seed", "seed@example.com"));
        User created = fixture.userDao.create(new User(0, "Next", "next@example.com"));
        assertEquals(51, created.id());
        fixture.userDao.delete(created.id());
        assertEquals(52, fixture.userDao.create(new User(0, "Later", "later@example.com")).id());
        assertThrows(IllegalArgumentException.class,
                () -> fixture.userDao.create(new User(50, "Duplicate", "duplicate@example.com")));
    }

    @Test
    void eventAndTicketDaoSupportUpdatesAndDeletes() {
        Event updated = new Event(fixture.event.id(), "Updated", fixture.event.date());
        assertEquals(updated, fixture.eventDao.update(updated));
        Ticket ticket = fixture.ticketDao.create(new Ticket(0, fixture.user.id(), updated.id(),
                3, Ticket.Category.STANDARD));
        Ticket moved = new Ticket(ticket.id(), ticket.userId(), ticket.eventId(), 4, ticket.category());
        assertEquals(moved, fixture.ticketDao.update(moved));
        assertEquals(List.of(moved), fixture.ticketDao.findByEventId(updated.id()));
        assertTrue(fixture.ticketDao.delete(moved.id()));
        assertTrue(fixture.eventDao.delete(updated.id()));
        assertTrue(fixture.eventDao.findAll().isEmpty());
    }
}
