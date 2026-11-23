package com.epam.booking.dao;

import com.epam.booking.model.User;

import java.util.List;
import java.util.Optional;

public class UserDao extends AbstractMapDao<User> {
    public UserDao() {
        super("user:", User.class);
    }

    public Optional<User> findByEmail(String email) {
        return findAll().stream().filter(user -> user.email().equalsIgnoreCase(email)).findFirst();
    }

    public List<User> findByName(String name) {
        return findAll().stream().filter(user -> user.name().equalsIgnoreCase(name)).toList();
    }
}
