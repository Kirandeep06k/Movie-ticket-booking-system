# Theatrely – Movie Booking System

A desktop-based movie ticket booking application developed using **Core Java, JavaFX, JDBC, and SQLite**.

## Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java 17+ |
| Frontend | JavaFX |
| Backend | Core Java |
| Database | SQLite |
| Database Connectivity | JDBC |
| Build Tool | Maven |
| Logging | Java Util Logging |

## Getting Started

### Prerequisites

- JDK 17 or later
- Apache Maven
- IntelliJ IDEA or another Java-compatible IDE

### Running the Application

**Using IntelliJ IDEA**

1. Open IntelliJ IDEA.
2. Select **File → Open** and choose the project folder containing `pom.xml`.
3. Wait for Maven dependencies to finish importing.
4. Run `com.mbs.Launcher`.

**Using the Terminal**

Navigate to the project directory and execute:

```bash
mvn javafx:run
```

To build the project:

```bash
mvn clean package
```

## Administrator Configuration

The administrator logs in using the following email:

```text
admin@theatrely.com
```

The administrator password must be configured through the `THEATRELY_ADMIN_PASSWORD` environment variable before launching the application.

**Windows CMD:**

```cmd
set THEATRELY_ADMIN_PASSWORD=your_secure_password
mvn javafx:run
```

Replace `your_secure_password` with your chosen password. Run both commands in the same terminal session. The password is not stored in the tracked application properties file.

## Database and Configuration

- The application uses SQLite for persistent data storage.
- The database is created automatically at `data/movies.db`.
- The initial database includes 20 sample movies and their associated shows.
- Fresh shows are generated on application startup when existing show dates have passed.
- Application logs are written to `logs/app.log`.
- General application settings are maintained in `src/main/resources/app.properties`.

## Application Features

### Customer Features

- Customer registration and login.
- Password storage using salted SHA-256 hashes.
- Movie browsing with posters, ratings, genres, release years, and durations.
- Movie search and genre-based filtering.
- Movie sorting and filtering.
- Movie details, including synopsis, director, cast, and trailer links.
- Showtime selection and interactive seat booking.
- Booking history and cancellation.
- Digital booking receipts and CSV export.
- Booking backup and restoration.
- Customer location management using state and city selection.
- About Theatrely dialog displaying the TMDB logo and required API attribution.

### Administrator Features

- Dedicated administrator login.
- Movie and show management.
- Booking and customer management.
- Administrative data views and booking records.
- Movie poster upload and management.

### Booking and Pricing Logic

- Seat availability is checked before booking.
- Database transactions ensure that incomplete bookings are rolled back.
- A composite primary key on `(show_id, seat)` prevents duplicate seat bookings.
- Customers receive a 10% loyalty discount after five confirmed bookings.
- GST is calculated at 18%.
- Maximum seats per booking, loyalty thresholds, discount percentages, and GST rates are configurable through `app.properties`.

## Project Architecture

The application follows a layered architecture to separate user interface components, business logic, database operations, and utility functionality.

| Layer | Responsibility |
|---|---|
| Model | Represents entities such as movies, shows, bookings, customers, and administrators. |
| DAO | Handles database operations using JDBC. |
| Service | Implements business logic, authentication, booking operations, and validation. |
| UI | Implements JavaFX screens and user interactions. |
| Util | Provides configuration, database connectivity, logging, password handling, and file operations. |
| Exception | Contains custom application exceptions. |

### Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/mbs/
    │       ├── model/
    │       ├── dao/
    │       ├── service/
    │       ├── util/
    │       ├── ui/
    │       └── exception/
    └resources/
     ├── app.properties
     ├── style.css
     ├── fonts/
     └── images/
         └── tmdb-logo.svg
```

## Syllabus Coverage

The project incorporates Java programming, object-oriented programming, exception handling, collections, file handling, database connectivity, and JavaFX concepts covered in the course.

### Unit 1 – Java Fundamentals and Object-Oriented Programming

| Topic | Implementation |
|---|---|
| Packages and Project Structure | Organized packages: `model`, `dao`, `service`, `util`, `ui`, and `exception`. |
| Classes and Objects | Application entities such as `Movie`, `Show`, `Booking`, and `Customer`. |
| Interfaces | `Payable` interface implemented by `Show`; generic `GenericDao<T>` interface implemented by DAO classes. |
| Abstract Classes and Inheritance | Abstract `Person` class extended by `Customer` and `Admin`. |
| Static and Instance Members | Static members in `Person` and shared application services in `Session`. |
| Access Modifiers | Encapsulation using private and protected members. |
| Coding Conventions | camelCase methods, PascalCase classes, Javadoc comments, and organized packages. |
| Build Tools | Maven dependency and build management through `pom.xml`. |
| JVM, JRE, JDK, and CLASSPATH | Java compilation, bytecode execution, runtime environment, and dependency management. |

### Unit 2 – Exception Handling, Collections, and Java Utilities

| Topic | Implementation |
|---|---|
| Exception Handling | `try`, `catch`, `throws`, and custom exceptions. |
| Custom Exceptions | `BookingException`, `SeatUnavailableException`, and `InvalidInputException`. |
| ArrayList | Managing seat lists and DAO query results. |
| HashMap | Genre-based movie counts in `CatalogService`. |
| LinkedList | Maintaining recent booking activity in `BookingService`. |
| HashSet | Managing unique booked seats and movie genres. |
| Iterator | Traversal and removal in catalogue and booking operations. |
| Comparator and Comparable | `Comparator` for movie sorting and `Comparable` implemented by `Movie`. |
| Generics | `GenericDao<T>` and generic collection-processing methods in `CatalogService`. |
| Objects, Optional, and Math | `Objects` utility methods, `Optional` for database lookups, and `Math.round()` for price calculations. |
| Date and Time API | `LocalDate`, `LocalTime`, and `LocalDateTime` for show schedules and booking timestamps. |

### Unit 3 – File Handling, Serialization, and Logging

| Topic | Implementation |
|---|---|
| Character Streams | `BufferedReader`, `BufferedWriter`, `FileWriter`, and related utilities for receipts, CSV exports, and log reading. |
| Byte Streams | `FileInputStream` and `FileOutputStream` for copying movie posters. |
| Serialization | Object serialization through `FileUtil.backup()`. |
| Deserialization | Restoring serialized booking data through `FileUtil.restore()`. |
| Logging | Centralized application logging using `java.util.logging` through `AppLogger`. |
| Properties File | Application configuration loaded from `app.properties` using `AppConfig`. |

### Unit 4 – JDBC and Database Management

| Topic | Implementation |
|---|---|
| JDBC API | Database connectivity using `Connection`, `DriverManager`, `PreparedStatement`, and `ResultSet`. |
| Database Integration | SQLite integration using the SQLite JDBC driver. |
| CRUD Operations | Insert, retrieve, update, and delete operations implemented through DAO classes. |
| Prepared Statements | Parameterized SQL queries for database operations. |
| Transactions | Booking and cancellation operations using `setAutoCommit(false)`, `commit()`, and `rollback()`. |
| DAO Pattern | Separation of database access logic through DAO classes and `GenericDao<T>`. |
| Data Integrity | Primary key and database constraints to prevent duplicate seat bookings. |

### Unit 5 – JavaFX Application Development

| Topic | Implementation |
|---|---|
| JavaFX Application Lifecycle | Application initialization and lifecycle management through `Main`. |
| Stage and Scene | Application windows and scene management using JavaFX `Stage` and `Scene`. |
| Scene Graph | JavaFX nodes and containers used to organize application screens. |
| Layouts | `HBox`, `VBox`, `GridPane`, `BorderPane`, `StackPane`, and `FlowPane`. |
| JavaFX Controls | `Label`, `TextField`, `PasswordField`, `Button`, `TextArea`, `CheckBox`, `ComboBox`, `DatePicker`, and `ToggleButton`. |
| TableView | Administrative and booking information displayed in tabular form. |
| ListView | Recent activity displayed in `MyBookingsView`. |
| Event Handling | Button actions, property listeners, search input changes, and table selection events. |
| Validation and Alerts | Input validation, alerts, confirmation dialogs, and application notifications. |

## Additional Application Concepts

- **Layered Architecture:** Separates presentation, business logic, data access, and utility functionality.
- **Authentication:** Implements customer password hashing with salts and environment-based administrator password configuration.
- **Data Integrity:** Uses database constraints and transactions to prevent duplicate seat bookings and incomplete operations.
- **Configuration Management:** Keeps application settings separate from application logic.
- **Input Validation:** Validates user inputs before processing registration, login, booking, and administrative operations.

## User Interface

The application uses an off-white background, white rounded cards, black buttons, grey text, and yellow rating indicators. Styling is maintained through `resources/style.css` and the JavaFX `Theme` utility.

- **Header:** Application logo, movie search, location selector, and profile menu.
- **Home:** Genre filters, sorting options, and movie cards displaying posters, ratings, names, genres, durations, and release years.
- **Movie Details:** Movie synopsis, director, cast, trailer link, and available showtimes grouped by day.
- **Seat Selection:** Interactive seat selection for booking.
- **My Bookings:** Booking history, cancellation, receipts, and backup options.
- **Admin Panel:** Administrative views and management controls.

## Movie Posters

Movie posters are stored in the `posters/` directory.

To display posters correctly, place the corresponding image files in this directory using the filenames specified in:

```text
posters/_READ_ME_FIRST.txt
```

The application supports poster selection and copying through the administrator interface.

## Configuration Reference

The main application settings are maintained in:

```text
src/main/resources/app.properties
```

These include:

| Property | Purpose |
|---|---|
| `app.title` | Application title |
| `db.url` | SQLite database connection URL |
| `log.file` | Application log file location |
| `admin.email` | Administrator login email |
| `max.seats.per.booking` | Maximum seats allowed per booking |
| `gst.percent` | GST percentage |
| `loyal.after.bookings` | Confirmed bookings required for loyalty benefits |
| `loyal.discount.percent` | Loyalty discount percentage |
| `poster.dir` | Movie poster directory |

The administrator password is configured separately through the `THEATRELY_ADMIN_PASSWORD` environment variable.