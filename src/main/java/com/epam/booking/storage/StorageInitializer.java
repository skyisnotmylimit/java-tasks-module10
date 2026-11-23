package com.epam.booking.storage;

import com.epam.booking.model.Event;
import com.epam.booking.model.Ticket;
import com.epam.booking.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class StorageInitializer implements BeanPostProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(StorageInitializer.class);
    private Resource dataFile;

    public void setDataFile(Resource dataFile) {
        this.dataFile = dataFile;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof InMemoryStorage storage)) {
            return bean;
        }
        if (dataFile == null) {
            throw new BeanInitializationException("Storage data file must be configured");
        }
        Properties data = new Properties();
        try (Reader reader = new InputStreamReader(dataFile.getInputStream(), StandardCharsets.UTF_8)) {
            data.load(reader);
            initialize(storage, data);
            LOG.info("Initialized storage from {} with {} entities", dataFile, storage.snapshot().size());
        } catch (IOException | IllegalArgumentException | DateTimeParseException ex) {
            LOG.error("Cannot initialize storage from {}", dataFile, ex);
            throw new BeanInitializationException("Cannot initialize storage from " + dataFile, ex);
        }
        return bean;
    }

    private void initialize(InMemoryStorage storage, Properties data) {
        for (String key : data.stringPropertyNames()) {
            if (!key.matches("(user\\.[1-9][0-9]*\\.(name|email))"
                    + "|(event\\.[1-9][0-9]*\\.(title|date))"
                    + "|(ticket\\.[1-9][0-9]*\\.(userId|eventId|place|category))")) {
                throw new IllegalArgumentException("Unknown seed property: " + key);
            }
        }
        // Validate the entire file before exposing any seed data in the shared bean.
        InMemoryStorage prepared = new InMemoryStorage();
        Set<String> emails = new HashSet<>();
        for (long id : ids(data, "user")) {
            User user = new User(id, required(data, "user." + id + ".name"),
                    required(data, "user." + id + ".email"));
            if (!emails.add(user.email().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Duplicate seed user email at user " + id);
            }
            prepared.create("user:", user, User.class);
        }
        for (long id : ids(data, "event")) {
            prepared.create("event:", new Event(id, required(data, "event." + id + ".title"),
                    Instant.parse(required(data, "event." + id + ".date"))), Event.class);
        }
        Set<String> places = new HashSet<>();
        for (long id : ids(data, "ticket")) {
            String prefix = "ticket." + id + ".";
            Ticket ticket = new Ticket(id, Long.parseLong(required(data, prefix + "userId")),
                    Long.parseLong(required(data, prefix + "eventId")),
                    Integer.parseInt(required(data, prefix + "place")),
                    Ticket.Category.valueOf(required(data, prefix + "category")));
            if (prepared.find("user:", ticket.userId(), User.class).isEmpty()
                    || prepared.find("event:", ticket.eventId(), Event.class).isEmpty()) {
                throw new IllegalArgumentException("Seed ticket " + id + " refers to an unknown user or event");
            }
            if (!places.add(ticket.eventId() + ":" + ticket.place())) {
                throw new IllegalArgumentException("Duplicate seed ticket place at ticket " + id);
            }
            prepared.create("ticket:", ticket, Ticket.class);
        }
        for (User user : prepared.findAll("user:", User.class)) {
            storage.create("user:", user, User.class);
        }
        for (Event event : prepared.findAll("event:", Event.class)) {
            storage.create("event:", event, Event.class);
        }
        for (Ticket ticket : prepared.findAll("ticket:", Ticket.class)) {
            storage.create("ticket:", ticket, Ticket.class);
        }
    }

    private Set<Long> ids(Properties data, String type) {
        Set<Long> ids = new TreeSet<>();
        for (String key : data.stringPropertyNames()) {
            if (key.startsWith(type + ".")) {
                ids.add(Long.parseLong(key.split("\\.")[1]));
            }
        }
        return ids;
    }

    private String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing seed property: " + key);
        }
        return value.trim();
    }
}
