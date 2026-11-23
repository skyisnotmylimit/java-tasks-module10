package com.epam.booking;

import com.epam.booking.model.User;
import com.epam.booking.storage.InMemoryStorage;
import com.epam.booking.storage.StorageInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StorageInitializerTest {
    @Test
    void loadsAllEntitiesAndAdvancesIdSequence() {
        StorageInitializer initializer = new StorageInitializer();
        initializer.setDataFile(new ClassPathResource("seed-data.properties"));
        InMemoryStorage storage = new InMemoryStorage();
        assertSame(storage, initializer.postProcessAfterInitialization(storage, "storage"));
        assertEquals(5, storage.snapshot().size());
        assertEquals(3, storage.create("user:", new User(0, "New", "new@example.com"), User.class).id());
    }

    @Test
    void leavesUnrelatedBeansUntouched() {
        Object bean = new Object();
        assertSame(bean, new StorageInitializer().postProcessAfterInitialization(bean, "other"));
    }

    @Test
    void failsForMissingFileOrMissingConfiguration() {
        StorageInitializer initializer = new StorageInitializer();
        assertThrows(BeanInitializationException.class,
                () -> initializer.postProcessAfterInitialization(new InMemoryStorage(), "storage"));
        initializer.setDataFile(new ClassPathResource("nonexistent-data.properties"));
        assertThrows(BeanInitializationException.class,
                () -> initializer.postProcessAfterInitialization(new InMemoryStorage(), "storage"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user.1.name=Alice",
            "event.1.title=Invalid date\nevent.1.date=not-a-date",
            "ticket.1.userId=1\nticket.1.eventId=1\nticket.1.place=3\nticket.1.category=STANDARD",
            "user.1.name=Alice\nuser.1.email=alice@example.com\nuser.2.name=Bob\nuser.2.email=ALICE@example.com",
            "event.1.title=Event\nevent.1.date=2030-01-01T00:00:00Z\n"
                    + "ticket.1.userId=1\nticket.1.eventId=1\nticket.1.place=1\nticket.1.category=UNKNOWN",
            "user.1.nickname=Typo",
            "user.0.name=Invalid ID"
    })
    void rejectsMalformedSeedDataWithoutPartiallyPopulatingStorage(String contents) {
        StorageInitializer initializer = new StorageInitializer();
        initializer.setDataFile(new ByteArrayResource(contents.getBytes(StandardCharsets.UTF_8)));
        InMemoryStorage storage = new InMemoryStorage();
        assertThrows(BeanInitializationException.class,
                () -> initializer.postProcessAfterInitialization(storage, "storage"));
        assertTrue(storage.snapshot().isEmpty());
    }

    @Test
    void rejectsDuplicateSeedBookings() {
        String contents = """
                user.1.name=Alice
                user.1.email=alice@example.com
                event.1.title=Event
                event.1.date=2030-01-01T00:00:00Z
                ticket.1.userId=1
                ticket.1.eventId=1
                ticket.1.place=1
                ticket.1.category=STANDARD
                ticket.2.userId=1
                ticket.2.eventId=1
                ticket.2.place=1
                ticket.2.category=PREMIUM
                """;
        StorageInitializer initializer = new StorageInitializer();
        initializer.setDataFile(new ByteArrayResource(contents.getBytes(StandardCharsets.UTF_8)));
        InMemoryStorage storage = new InMemoryStorage();
        assertThrows(BeanInitializationException.class,
                () -> initializer.postProcessAfterInitialization(storage, "storage"));
        assertTrue(storage.snapshot().isEmpty());
    }
}
