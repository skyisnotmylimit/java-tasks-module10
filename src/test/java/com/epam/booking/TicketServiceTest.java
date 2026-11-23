package com.epam.booking;

import com.epam.booking.exception.BookingConflictException;
import com.epam.booking.exception.EntityNotFoundException;
import com.epam.booking.model.Event;
import com.epam.booking.model.Ticket;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {
    private final BookingFixture fixture = new BookingFixture();

    @Test
    void booksListsCancelsAndRebooksAPlace() {
        Ticket ticket = book(5);
        assertEquals(ticket, fixture.tickets.getTicketById(ticket.id()));
        assertEquals(List.of(ticket), fixture.tickets.getBookedTicketsByUser(fixture.user.id()));
        assertEquals(List.of(ticket), fixture.tickets.getBookedTicketsByEvent(fixture.event.id()));
        assertTrue(fixture.tickets.cancelTicket(ticket.id()));
        assertFalse(fixture.tickets.cancelTicket(ticket.id()));
        assertTrue(fixture.tickets.getBookedTicketsByUser(fixture.user.id()).isEmpty());
        assertThrows(EntityNotFoundException.class, () -> fixture.tickets.getTicketById(ticket.id()));
        assertNotEquals(ticket.id(), book(5).id());
    }

    @Test
    void rejectsDoubleBookingEvenForDifferentCategories() {
        book(5);
        assertThrows(BookingConflictException.class, () -> fixture.tickets.bookTicket(
                fixture.user.id(), fixture.event.id(), 5, Ticket.Category.BAR));
        assertEquals(1, fixture.ticketDao.findAll().size());
    }

    @Test
    void permitsTheSamePlaceAtDifferentEvents() {
        book(5);
        Event other = fixture.events.createEvent(new Event(0, "Other", fixture.event.date()));
        fixture.tickets.bookTicket(fixture.user.id(), other.id(), 5, Ticket.Category.PREMIUM);
        assertEquals(2, fixture.ticketDao.findAll().size());
    }

    @Test
    void rejectsUnknownReferencesWithoutSavingTickets() {
        assertThrows(EntityNotFoundException.class,
                () -> fixture.tickets.bookTicket(999, fixture.event.id(), 1, Ticket.Category.STANDARD));
        assertThrows(EntityNotFoundException.class,
                () -> fixture.tickets.bookTicket(fixture.user.id(), 999, 1, Ticket.Category.STANDARD));
        assertThrows(EntityNotFoundException.class, () -> fixture.tickets.getBookedTicketsByUser(999));
        assertThrows(EntityNotFoundException.class, () -> fixture.tickets.getBookedTicketsByEvent(999));
        assertTrue(fixture.ticketDao.findAll().isEmpty());
    }

    @Test
    void rejectsInvalidBookingData() {
        assertThrows(IllegalArgumentException.class, () -> book(0));
        assertThrows(IllegalArgumentException.class, () -> book(-1));
        assertThrows(IllegalArgumentException.class,
                () -> fixture.tickets.bookTicket(0, fixture.event.id(), 1, Ticket.Category.STANDARD));
        assertThrows(NullPointerException.class,
                () -> fixture.tickets.bookTicket(fixture.user.id(), fixture.event.id(), 1, null));
        assertTrue(fixture.ticketDao.findAll().isEmpty());
    }

    @Test
    void concurrentRequestsCannotBookTheSamePlaceTwice() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> first = executor.submit(() -> tryBook(start));
            Future<Boolean> second = executor.submit(() -> tryBook(start));
            start.countDown();
            int successes = (first.get(5, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(5, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successes);
            assertEquals(1, fixture.ticketDao.findAll().size());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private Ticket book(int place) {
        return fixture.tickets.bookTicket(fixture.user.id(), fixture.event.id(), place, Ticket.Category.STANDARD);
    }

    private boolean tryBook(CountDownLatch start) throws InterruptedException {
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent booking test did not start");
        }
        try {
            book(8);
            return true;
        } catch (BookingConflictException expected) {
            return false;
        }
    }
}
