# Theatrely - Movie Booking System (Core Java + JavaFX + JDBC)

Course: UCS553 Enterprise Web Application. Everything (front end + back end) is in Java.

## How to run
Needs JDK 17+ and Maven (IntelliJ has both built in).

IntelliJ: File > Open > select this folder (pom.xml) > wait for Maven import > run `com.mbs.Launcher`.
Terminal: `mvn javafx:run`   (or `mvn clean package` to build)

Customers: open the Register tab (name, email, password), then use Login (email + password). Passwords are stored as salted SHA-256 hashes, never as plain text.
Admin: login with email `admin@theatrely.com` and password `admin123` (both set in app.properties).
Database (SQLite) is created automatically in `data/movies.db` with 20 sample movies (all-time favourites and recent releases) and their shows. No need to add movies yourself; if show dates pass, fresh shows are added on next start. Logs go to `logs/app.log`.

## Syllabus coverage (where to show it in the viva)

| Unit | Topic | Where |
|---|---|---|
| 1 | Packages / project structure | `com.mbs.{model,dao,service,util,ui,exception}` |
| 1 | Classes, objects | `model/*` (Movie, Show, Booking...) |
| 1 | Interfaces | `model/Payable` (impl. by Show), `dao/GenericDao<T>` (impl. by 4 DAOs) |
| 1 | Abstract classes, inheritance | `model/Person` (abstract) -> `Customer`, `Admin` |
| 1 | Static vs instance, access modifiers | `Person.instanceCount` (static), private/protected fields, `Session` static services |
| 1 | Coding conventions | camelCase methods, PascalCase classes, Javadoc comments, layered packages |
| 1 | Build tool | `pom.xml` (Maven) |
| 1 | JVM / JRE / JDK / CLASSPATH | see notes below |
| 2 | Custom exceptions + hierarchy | `exception/BookingException` -> `SeatUnavailableException`, `InvalidInputException` |
| 2 | try / catch / throws / finally | every service + DAO (`BookingService.bookSeats`) |
| 2 | ArrayList / HashMap | ArrayList: `BookingService.allSeats()`, all DAO result lists. HashMap: `CatalogService.genreCounts` (shown in the status bar) |
| 2 | LinkedList / HashSet | `BookingService.RECENT` (LinkedList queue), `findBookedSeats` and `CatalogService.genres` (HashSet) |
| 2 | Iterator | `CatalogService.upcomingShows`, `BookingService.availableSeats` (`it.remove()`) |
| 2 | Comparator / Comparable | `CatalogService.sortMovies` (Comparator), `Movie.compareTo` (Comparable) |
| 2 | Generics | `GenericDao<T>`, `CatalogService.filter(Collection<T>, Predicate<T>)` |
| 2 | Objects, Optional, Math | `Objects.requireNonNull/equals/hash`; `Optional` in all `findById`; `Math.round` in `calculateTotal` |
| 2 | LocalDate / LocalTime / LocalDateTime | `Show`, `Booking`, `CatalogService.addShow` |
| 3 | Character streams | `FileUtil.writeReceipt`, `exportCsv`, `readLastLines` (BufferedWriter/FileWriter/BufferedReader) |
| 3 | Byte streams | `FileUtil.copyPoster` (FileInputStream/FileOutputStream) - Admin > Choose poster |
| 3 | Serialization / deserialization | `FileUtil.backup` / `restore` - My Bookings > Backup / View backup file |
| 3 | Logging | `util/AppLogger` (java.util.logging) -> `logs/app.log` |
| 3 | Properties file | `resources/app.properties` read by `util/AppConfig` |
| 4 | JDBC API (Connection, DriverManager) | `util/DatabaseManager` |
| 4 | MySQL/SQLite | SQLite (`sqlite-jdbc` in pom) |
| 4 | CRUD | all DAOs: insert / findById / findAll / update / delete |
| 4 | PreparedStatement / ResultSet | every DAO method |
| 4 | Transactions | `BookingService.bookSeats` / `cancelBooking` (setAutoCommit(false), commit, rollback) |
| 4 | DAO pattern | `dao/*` + `GenericDao` |
| 5 | Application subclass, init/start/stop | `Main` |
| 5 | Stage, Scene, scene graph | `Main`, `Session`, `SeatDialog` (second Stage) |
| 5 | Extra layouts | FlowPane (movie grid, cast, showtimes) in `MoviesView`, `MovieDetailView` |
| 5 | Layouts | HBox, VBox, GridPane (`SeatDialog`, `AdminView`), BorderPane (`DashboardView`), StackPane (`Theme.poster`, page area) |
| 5 | Controls | Label, TextField, Button, TextArea, CheckBox, ComboBox, PasswordField, DatePicker, ToggleButton |
| 5 | TableView / ListView | `MyBookingsView` and `AdminView` (TableView); recent activity in `MyBookingsView` (ListView) |
| 5 | Events and listeners | `setOnAction` on buttons; `addListener` on search box, combo boxes, checkbox, table selection, seats |
| 5 | Validation, alerts, dialogs | `util/Validator`, `ui/Dialogs` (Alert), status bar notifications |

## Look and features
Clean look: off-white background, white rounded cards, black buttons, grey text, yellow rating star (`resources/style.css`, `ui/Theme`). Headings use the bundled Bebas Neue font (open source, see `resources/fonts/OFL.txt`).
Header: logo, search (filters the movie grid while you type), location (state > city, India) and a profile menu (bookings, admin panel, change location, logout).
Home: genre pills, sort/filter menu, grid of movie cards (poster, rating badge, name, genre, duration, year, Book Tickets).
Movie page: story, director, cast, trailer button (opens a YouTube search in the browser) and show times grouped by day.
Posters: copy images into the `posters/` folder using the file names listed in `posters/_READ_ME_FIRST.txt`.
Location: asked after the first login (state, then city), saved on the account, shown in the header, the page heading and the show times.

## Notes for viva
- JDK = compiler + tools + JRE. JRE = JVM + libraries. JVM runs bytecode (`.class`). Java is compiled to bytecode once, runs anywhere a JVM exists.
- CLASSPATH = where the JVM looks for `.class` files / jars. Maven builds it from pom dependencies. Manual run: `java -cp target/classes:sqlite-jdbc.jar com.mbs.Launcher`.
- Double booking is prevented twice: seat check inside the transaction, and the `PRIMARY KEY (show_id, seat)` in `booked_seats`. On any error the transaction rolls back, so no half-saved bookings.
- Loyalty rule: after 5 confirmed bookings a customer gets 10% off (values in app.properties). Price = tickets - discount + 18% GST.
- Mini-project mapping: close to the "Hospital/Clinic Appointment Scheduler" suggestion (booking and cancellation system) with database integration.
