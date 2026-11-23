package com.epam.booking;

import com.epam.booking.exception.BookingConflictException;
import com.epam.booking.exception.EntityNotFoundException;
import com.epam.booking.model.Ticket;
import com.epam.booking.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private final BookingFixture fixture = new BookingFixture();

    @Test
    void createsFindsAndUpdatesUser() {
        User created = fixture.users.createUser(new User(0, "Bob", "bob@example.com"));
        assertEquals(created, fixture.users.getUserById(created.id()));
        assertEquals(Optional.of(created), fixture.users.getUserByEmail("BOB@example.com"));
        assertEquals(List.of(created), fixture.users.getUsersByName("bob"));
        assertTrue(fixture.users.getUserByEmail("absent@example.com").isEmpty());

        User updated = new User(created.id(), "Robert", created.email());
        assertEquals(updated, fixture.users.updateUser(updated));
        assertEquals(List.of(updated), fixture.users.getUsersByName("Robert"));
        assertTrue(fixture.users.getUsersByName("Bob").isEmpty());
    }

    @Test
    void rejectsDuplicateEmailForCreateAndUpdateWithoutChangingStoredUser() {
        assertThrows(BookingConflictException.class,
                () -> fixture.users.createUser(new User(0, "Duplicate", "ALICE@example.com")));
        User other = fixture.users.createUser(new User(0, "Bob", "bob@example.com"));
        assertThrows(BookingConflictException.class,
                () -> fixture.users.updateUser(new User(other.id(), "Bob", "alice@example.com")));
        assertEquals(other, fixture.users.getUserById(other.id()));
    }

    @Test
    void requiresAnExistingUserForLookupAndUpdate() {
        assertThrows(EntityNotFoundException.class, () -> fixture.users.getUserById(999));
        assertThrows(EntityNotFoundException.class,
                () -> fixture.users.updateUser(new User(999, "Missing", "missing@example.com")));
    }

    @Test
    void protectsBookedUsersUntilTheirTicketsAreCancelled() {
        Ticket ticket = fixture.tickets.bookTicket(fixture.user.id(), fixture.event.id(),
                1, Ticket.Category.STANDARD);
        assertThrows(BookingConflictException.class, () -> fixture.users.deleteUser(fixture.user.id()));
        assertTrue(fixture.tickets.cancelTicket(ticket.id()));
        assertTrue(fixture.users.deleteUser(fixture.user.id()));
        assertFalse(fixture.users.deleteUser(fixture.user.id()));
    }

    @Test
    void rejectsInvalidUserData() {
        assertThrows(IllegalArgumentException.class, () -> new User(0, " ", "valid@example.com"));
        assertThrows(IllegalArgumentException.class, () -> new User(0, "Alice", "invalid"));
        assertThrows(IllegalArgumentException.class, () -> new User(-1, "Alice", "alice@example.com"));
    }
}
