package com.epam.booking.model;

public sealed interface Entity permits User, Event, Ticket {
    long id();

    Entity withId(long id);
}
