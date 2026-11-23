# Spring ticket booking

Maven module using Java 17+, Spring Framework, XML configuration, SLF4J/Logback,
and JUnit 5. No Spring Boot, database, or web server is required.

## Build and run

```powershell
mvn clean verify
mvn exec:java
```

`mvn test` runs unit tests. `mvn verify` also runs the `BookingFacadeIT`
integration tests. The demo creates a user and event, books a ticket, and cancels it.
All state is in memory and is lost when the application context closes.

## Structure and injection

- `model`: immutable `User`, `Event`, and `Ticket` records. Pass ID `0` when
  creating a new entity to generate an ID. Explicit positive IDs are supported.
- `storage`: the singleton `InMemoryStorage` bean stores all entities in one
  `Map<String, Entity>` under keys such as `user:1`, `event:1`, and `ticket:1`.
  The immutable records serve as the map-entry values. Namespace queries return
  immutable lists ordered by ID; generated IDs advance past seeded IDs.
- `dao`: one DAO per entity, sharing the storage bean through setter autowiring.
- `service`: `UserService`, `EventService`, and `TicketService` provide CRUD,
  searches, booking, and cancellation. DAO and storage dependencies use
  `@Autowired` setters.
- `facade`: `BookingFacadeImpl` delegates every operation to a service.
  Its three service dependencies are constructor-injected in
  `src/main/resources/application-context.xml`.
- `StorageInitializer`: a Spring `BeanPostProcessor` loads and validates the
  seed file when the storage bean is initialized, before it is injected into DAOs.
  Invalid or unreadable seed data fails application-context startup.

There is no component scanning or Java-based configuration. XML declares all
application beans; `context:annotation-config` enables setter autowiring.

## External configuration and prepared data

`src/main/resources/booking.properties` defines:

```properties
booking.data.location=classpath:seed-data.properties
```

To supply a different external property file, create, for example,
`D:\booking\booking.properties`:

```properties
booking.data.location=file:D:/booking/prepared-data.properties
```

Then run:

```powershell
mvn "-Dbooking.config=file:D:/booking/booking.properties" exec:java
```

The external properties override the bundled defaults. A JVM system property
can also override the data location directly:

```powershell
mvn "-Dbooking.data.location=file:D:/booking/prepared-data.properties" exec:java
```

Spring resource locations use URL syntax (`file:D:/...`), even on Windows.
Seed data is UTF-8 Java properties with this format:

```properties
user.10.name=Example User
user.10.email=example@example.com
event.20.title=Spring Workshop
event.20.date=2030-06-15T10:00:00Z
ticket.30.userId=10
ticket.30.eventId=20
ticket.30.place=5
ticket.30.category=STANDARD
```

IDs in seed keys must be positive. Dates use ISO-8601 instants.
Categories are `STANDARD`, `PREMIUM`, and `BAR`. Entities without all required
fields, unknown properties, duplicate emails, missing ticket references, and
duplicate event/place bookings are rejected. An empty file starts empty storage.

## API behavior

Retrieve the entry point with
`context.getBean("bookingFacade", BookingFacade.class)` from a
`ClassPathXmlApplicationContext("application-context.xml")`.

- User/event CRUD and ticket lookup throw `EntityNotFoundException` when a
  requested entity for lookup/update is missing.
- Email lookup returns `Optional<User>`. Name/title searches are exact,
  case-insensitive matches; event-date searches match the exact instant.
- Emails must be unique, ignoring case.
- Booking requires existing user/event IDs and a positive place. One event/place
  can have only one ticket, regardless of category.
- Cancelling frees the place; cancelling or deleting an absent entity returns
  `false`. Users/events with booked tickets cannot be deleted until cancellation.
- Service mutations share the storage monitor, so validation and writes are
  atomic with respect to other service mutations, including concurrent bookings.
- DAOs are persistence primitives; clients should use the facade/services to
  enforce booking rules.
- Successful mutations and startup are logged at INFO; booking conflicts at WARN
  and startup failures at ERROR. Other invalid operations propagate explicit
  exceptions to callers.

Unit tests exercise services and map-backed DAOs without Spring. Integration
tests directly instantiate XML contexts and exercise the facade, prepared data,
external configuration, startup failure, and storage isolation.
