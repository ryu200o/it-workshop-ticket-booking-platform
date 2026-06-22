# IT Workshop Ticket Booking Platform

## Project Overview

The IT Workshop Ticket Booking Platform is a robust and scalable application designed to manage and facilitate the booking of IT workshops. Built with modern Java technologies, it provides a comprehensive solution for workshop organizers and attendees, ensuring a smooth registration and management experience.

## Architecture

This project adopts a **Modular Monolith** architecture, leveraging **Spring Modulith** to enforce strong module boundaries and promote independent development within a single deployable unit. This approach combines the benefits of monolith simplicity with the organizational advantages of a modular design.

Logical modules include:
*   **Identity**: Handles user authentication and authorization.
*   **Room**: Manages workshop rooms and their availability.
*   **Workshop**: Defines workshop details, schedules, and capacity.
*   **Registration**: Manages user registrations for workshops.

Cross-module communication is primarily achieved through **asynchronous event publications** using Spring Modulith's Event Publication Registry. This ensures loose coupling between modules. There are **zero physical cross-module foreign keys** in the database, with relationships managed logically at the application level.

### Domain Highlights
*   **Offline workshops only**: The platform is designed for physical, in-person workshops.
*   **One workshop = one session**: Each workshop entity represents a single session.
*   **Registration aggregate**: The `Registration` is an aggregate root, ensuring consistency of related entities.
*   **AttendanceRecord value object**: `AttendanceRecord` is modeled as a value object within the `Registration` aggregate.

## Tech Stack

*   **Language**: Java 25
*   **Framework**: Spring Boot 4.1.0
*   **Modularity**: Spring Modulith
*   **Database**: PostgreSQL 17 (production/development), H2 Database (testing)
*   **Database Migration**: Flyway v11

## Local Setup

To set up the project locally, follow these steps:

1.  **Prerequisites**:
    *   Java 25 JDK
    *   Docker and Docker Compose
    *   Maven 3.8+

2.  **Start PostgreSQL with Docker Compose**:
    The project includes a `docker-compose.yml` file to quickly spin up a PostgreSQL database.
    ```bash
    docker-compose up -d postgres
    ```

3.  **Local Environment Secrets**:
    Create an `application-local.properties` (or `application-local.yml`) file in `src/main/resources` or configure environment variables for your database connection and other sensitive information. Example for `application-local.properties`:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/workshop_db
    spring.datasource.username=workshop_user
    spring.datasource.password=workshop_password
    # Other local specific properties
    ```

4.  **Run the Application**:
    You can run the Spring Boot application using Maven:
    ```bash
    ./mvnw spring-boot:run
    ```
    Or from your IDE.

## Profiles

The application uses Spring Profiles to manage different environments:

*   **`local`**: Active by default when running locally with PostgreSQL. Configured for development and uses seed data.
*   **`test`**: Automatically activated during `mvn test`. Uses an in-memory H2 database for fast and isolated testing.

## Database Migration

**Flyway v11** is used for database schema migration. Migration scripts are located at `classpath:db/migration`.

## Seed Data

Seed data scripts are located at `classpath:db/seed`. This data is **only loaded for `local` and `test` profiles** to populate development and test databases with initial data, ensuring that production environments remain clean.

## Test Command

To run all unit and integration tests:

```bash
./mvnw clean test
```

Alternatively, a convenience script is provided:

```bash
scripts/run-tests.sh
```

## Project Structure

```
.  
├── src/main/java/com/example/workshop/platform/ # Java source code (modular structure)  
│   ├── identity/                            # Identity module  
│   ├── room/                                # Room module  
│   ├── workshop/                            # Workshop module  
│   └── registration/                        # Registration module  
├── src/main/resources/                      # Application resources  
│   ├── application.properties             # Main configuration  
│   ├── application-local.properties       # Local profile specific configuration (ignored by Git)  
│   ├── db/                                  # Database related scripts  
│   │   ├── migration/                       # Flyway migration scripts  
│   │   └── seed/                            # Seed data scripts (for local/test profiles)  
│   └── static/                              # Static web content (if any)  
├── src/test/java/                           # Test source code  
├── scripts/                                 # Helper scripts  
│   └── run-tests.sh                       # Script to run tests  
├── docs/                                    # Project documentation  
│   ├── architecture.md                    # Architecture overview  
│   ├── database.md                        # Database design details  
│   └── adr/                               # Architecture Decision Records  
│       ├── ADR-001-h2-database-testing.md  
│       ├── ADR-002-attendance-record-inline.md  
│       └── ADR-003-workshop-room-snapshot.md  
├── .gitignore                               # Git ignore file  
├── pom.xml                                  # Maven Project Object Model  
├── docker-compose.yml                       # Docker Compose for local services  
├── README.md                                # Project README  
└── LICENSE                                  # Project License  
```
