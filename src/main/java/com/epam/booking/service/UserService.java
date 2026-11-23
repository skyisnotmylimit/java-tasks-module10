package com.epam.booking.service;

import com.epam.booking.dao.TicketDao;
import com.epam.booking.dao.UserDao;
import com.epam.booking.exception.BookingConflictException;
import com.epam.booking.model.User;
import com.epam.booking.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private UserDao userDao;
    private TicketDao ticketDao;
    private InMemoryStorage storage;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public User getUserById(long id) {
        return userDao.getById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userDao.findByEmail(Objects.requireNonNull(email, "Email is required"));
    }

    public List<User> getUsersByName(String name) {
        return userDao.findByName(Objects.requireNonNull(name, "Name is required"));
    }

    public User createUser(User user) {
        synchronized (storage) {
            requireUniqueEmail(user);
            User saved = userDao.create(user);
            LOG.info("Created user id={}", saved.id());
            return saved;
        }
    }

    public User updateUser(User user) {
        synchronized (storage) {
            userDao.getById(user.id());
            requireUniqueEmail(user);
            User saved = userDao.update(user);
            LOG.info("Updated user id={}", saved.id());
            return saved;
        }
    }

    public boolean deleteUser(long id) {
        synchronized (storage) {
            if (!ticketDao.findByUserId(id).isEmpty()) {
                throw new BookingConflictException("Cannot delete user " + id + " with booked tickets");
            }
            boolean deleted = userDao.delete(id);
            LOG.info("Delete user id={}, deleted={}", id, deleted);
            return deleted;
        }
    }

    private void requireUniqueEmail(User user) {
        userDao.findByEmail(user.email()).ifPresent(existing -> {
            if (user.id() == 0 || existing.id() != user.id()) {
                throw new BookingConflictException("Email is already registered");
            }
        });
    }
}
