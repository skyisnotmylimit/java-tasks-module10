package com.epam.booking;

import com.epam.booking.facade.BookingFacade;
import com.epam.booking.model.Event;
import com.epam.booking.model.Ticket;
import com.epam.booking.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.time.Instant;

public final class BookingApplication {
    private static final Logger LOG = LoggerFactory.getLogger(BookingApplication.class);

    private BookingApplication() {
    }

    public static void main(String[] args) {
        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml")) {
            BookingFacade facade = context.getBean("bookingFacade", BookingFacade.class);
            User user = facade.createUser(new User(0, "Demo User", "demo@example.com"));
            Event event = facade.createEvent(new Event(0, "Spring Workshop", Instant.parse("2030-06-15T10:00:00Z")));
            Ticket ticket = facade.bookTicket(user.id(), event.id(), 12, Ticket.Category.STANDARD);
            LOG.info("Demo booking created: {}", ticket);
            LOG.info("Demo booking cancelled: {}", facade.cancelTicket(ticket.id()));
        }
    }
}
