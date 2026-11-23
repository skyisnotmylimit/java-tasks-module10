package com.epam.booking;

import com.epam.booking.exception.BookingConflictException;
import com.epam.booking.exception.EntityNotFoundException;
import com.epam.booking.facade.BookingFacade;
import com.epam.booking.model.Event;
import com.epam.booking.model.Ticket;
import com.epam.booking.model.User;
import com.epam.booking.storage.InMemoryStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookingFacadeIT {
    @TempDir
    Path directory;

    @Test
    void createsUserAndEventBooksAndCancelsTicketThenDeletesEntities() {
        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml")) {
            BookingFacade facade = context.getBean("bookingFacade", BookingFacade.class);
            User user = facade.createUser(new User(0, "Charlie", "charlie@example.com"));
            Event event = facade.createEvent(new Event(0, "Workshop", Instant.parse("2030-08-01T10:00:00Z")));
            Ticket ticket = facade.bookTicket(user.id(), event.id(), 42, Ticket.Category.PREMIUM);

            assertEquals(ticket, facade.getTicketById(ticket.id()));
            assertEquals(List.of(ticket), facade.getBookedTicketsByUser(user.id()));
            assertEquals(List.of(ticket), facade.getBookedTicketsByEvent(event.id()));
            assertThrows(BookingConflictException.class, () -> facade.deleteUser(user.id()));
            assertThrows(BookingConflictException.class, () -> facade.deleteEvent(event.id()));
            assertTrue(facade.cancelTicket(ticket.id()));
            assertFalse(facade.cancelTicket(ticket.id()));
            assertTrue(facade.getBookedTicketsByEvent(event.id()).isEmpty());
            assertThrows(EntityNotFoundException.class, () -> facade.getTicketById(ticket.id()));
            assertTrue(facade.deleteUser(user.id()));
            assertTrue(facade.deleteEvent(event.id()));
        }
    }

    @Test
    void seededDataCanBeLookedUpAndUpdatedThroughFacade() {
        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml")) {
            BookingFacade facade = context.getBean(BookingFacade.class);
            User alice = facade.getUserById(1);
            Event conference = facade.getEventById(1);
            assertEquals(Optional.of(alice), facade.getUserByEmail("alice@example.com"));
            assertEquals(List.of(alice), facade.getUsersByName("Alice"));
            assertEquals(List.of(conference), facade.getEventsByTitle("Java Conference"));
            assertEquals(List.of(conference), facade.getEventsByDate(conference.date()));
            assertEquals(1, facade.getBookedTicketsByUser(alice.id()).size());

            User updatedUser = new User(alice.id(), "Alice Updated", alice.email());
            Event updatedEvent = new Event(conference.id(), "Advanced Java", conference.date().plusSeconds(3600));
            assertEquals(updatedUser, facade.updateUser(updatedUser));
            assertEquals(updatedEvent, facade.updateEvent(updatedEvent));
            assertEquals(updatedUser, facade.getUserById(alice.id()));
            assertEquals(updatedEvent, facade.getEventById(conference.id()));
            assertEquals(5, context.getBean(InMemoryStorage.class).snapshot().size());
        }
    }

    @Test
    void preventsDoubleBookingAndReleasesCancelledPlaces() {
        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml")) {
            BookingFacade facade = context.getBean(BookingFacade.class);
            assertThrows(BookingConflictException.class,
                    () -> facade.bookTicket(2, 1, 10, Ticket.Category.PREMIUM));
            assertTrue(facade.cancelTicket(1));
            Ticket replacement = facade.bookTicket(2, 1, 10, Ticket.Category.PREMIUM);
            assertNotEquals(1, replacement.id());
            assertEquals(2, replacement.userId());
        }
    }

    @Test
    void loadsSeedLocationFromAnExternalPropertyFile() throws IOException {
        Path seed = directory.resolve("custom-data.properties");
        Files.writeString(seed, "user.100.name=External User\nuser.100.email=external@example.com\n");
        Path config = directory.resolve("external.properties");
        Files.writeString(config, "booking.data.location=" + seed.toUri() + "\n");

        try (ClassPathXmlApplicationContext context = contextWithConfig(config)) {
            context.refresh();
            BookingFacade facade = context.getBean(BookingFacade.class);
            assertEquals("External User", facade.getUserById(100).name());
            assertThrows(EntityNotFoundException.class, () -> facade.getUserById(1));
            assertEquals(101, facade.createUser(new User(0, "Next", "next@example.com")).id());
        }
    }

    @Test
    void invalidStartupDataPreventsTheContextFromStarting() throws IOException {
        Path seed = directory.resolve("invalid-data.properties");
        Files.writeString(seed, "user.1.name=Missing email\n");
        Path config = directory.resolve("invalid.properties");
        Files.writeString(config, "booking.data.location=" + seed.toUri() + "\n");
        try (ClassPathXmlApplicationContext context = contextWithConfig(config)) {
            assertThrows(BeansException.class, context::refresh);
            assertFalse(context.isActive());
        }
    }

    @Test
    void contextsHaveIndependentStorage() {
        try (ClassPathXmlApplicationContext first = new ClassPathXmlApplicationContext("application-context.xml");
             ClassPathXmlApplicationContext second = new ClassPathXmlApplicationContext("application-context.xml")) {
            first.getBean(BookingFacade.class).cancelTicket(1);
            assertEquals(1, second.getBean(BookingFacade.class).getTicketById(1).id());
            assertNotSame(first.getBean(InMemoryStorage.class), second.getBean(InMemoryStorage.class));
        }
    }

    private ClassPathXmlApplicationContext contextWithConfig(Path config) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("external-test-config", Map.of("booking.config", config.toUri().toString())));
        context.setConfigLocation("application-context.xml");
        return context;
    }
}
