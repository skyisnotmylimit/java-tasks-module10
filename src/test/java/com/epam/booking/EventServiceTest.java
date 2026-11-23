package com.epam.booking;

import com.epam.booking.exception.BookingConflictException;
import com.epam.booking.exception.EntityNotFoundException;
import com.epam.booking.model.Event;
import com.epam.booking.model.Ticket;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {
    private final BookingFixture fixture = new BookingFixture();

    @Test
    void createsSearchesAndUpdatesEvent() {
        Event created = fixture.events.createEvent(new Event(0, "Spring", Instant.parse("2030-06-01T10:00:00Z")));
        assertEquals(created, fixture.events.getEventById(created.id()));
        assertEquals(List.of(created), fixture.events.getEventsByTitle("spring"));
        assertEquals(List.of(created), fixture.events.getEventsByDate(created.date()));

        Event updated = new Event(created.id(), "Spring Core", created.date().plusSeconds(3600));
        assertEquals(updated, fixture.events.updateEvent(updated));
        assertTrue(fixture.events.getEventsByTitle("Spring").isEmpty());
        assertTrue(fixture.events.getEventsByDate(created.date()).isEmpty());
        assertEquals(List.of(updated), fixture.events.getEventsByDate(updated.date()));
    }

    @Test
    void rejectsLookupAndUpdateOfMissingEvents() {
        assertThrows(EntityNotFoundException.class, () -> fixture.events.getEventById(999));
        assertThrows(EntityNotFoundException.class,
                () -> fixture.events.updateEvent(new Event(999, "Missing", fixture.event.date())));
    }

    @Test
    void protectsBookedEventsUntilTheirTicketsAreCancelled() {
        Ticket ticket = fixture.tickets.bookTicket(fixture.user.id(), fixture.event.id(),
                1, Ticket.Category.STANDARD);
        assertThrows(BookingConflictException.class, () -> fixture.events.deleteEvent(fixture.event.id()));
        fixture.tickets.cancelTicket(ticket.id());
        assertTrue(fixture.events.deleteEvent(fixture.event.id()));
        assertFalse(fixture.events.deleteEvent(fixture.event.id()));
    }

    @Test
    void rejectsInvalidEventData() {
        assertThrows(IllegalArgumentException.class, () -> new Event(0, "", Instant.now()));
        assertThrows(NullPointerException.class, () -> new Event(0, "Missing date", null));
        assertThrows(IllegalArgumentException.class, () -> new Event(-1, "Negative ID", Instant.now()));
    }
}
