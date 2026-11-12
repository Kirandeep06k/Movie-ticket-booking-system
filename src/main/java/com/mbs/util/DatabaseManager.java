package com.mbs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/** JDBC connection + schema creation + sample data. */
public final class DatabaseManager {
    private static final String URL = AppConfig.get("db.url", "jdbc:sqlite:data/movies.db");

    static {
        try {
            Class.forName("org.sqlite.JDBC");   // load the JDBC driver
        } catch (ClassNotFoundException e) {
            AppLogger.get().severe("SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    private DatabaseManager() { }

    public static Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    /** Id of the row inserted last on this connection (SQLite). */
    public static int lastInsertId(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public static void init() throws SQLException {
        String prefix = "jdbc:sqlite:";
        if (URL.startsWith(prefix)) {
            Path parent = Paths.get(URL.substring(prefix.length())).toAbsolutePath().getParent();
            try {
                if (parent != null) {
                    Files.createDirectories(parent);
                }
            } catch (IOException e) {
                throw new SQLException("Cannot create database folder: " + e.getMessage(), e);
            }
        }
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, "
                    + "password_hash TEXT, salt TEXT, state TEXT, city TEXT)");
            addColumnIfMissing(c, "users", "password_hash", "TEXT");
            addColumnIfMissing(c, "users", "salt", "TEXT");
            addColumnIfMissing(c, "users", "state", "TEXT");
            addColumnIfMissing(c, "users", "city", "TEXT");
            st.execute("CREATE TABLE IF NOT EXISTS movies ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, genre TEXT NOT NULL, "
                    + "duration INTEGER NOT NULL, rating REAL NOT NULL, poster TEXT, synopsis TEXT, director TEXT, "
                    + "cast_list TEXT, trailer_url TEXT, release_year INTEGER)");
            st.execute("CREATE TABLE IF NOT EXISTS app_meta (k TEXT PRIMARY KEY, v TEXT)");
            addColumnIfMissing(c, "movies", "synopsis", "TEXT");
            addColumnIfMissing(c, "movies", "director", "TEXT");
            addColumnIfMissing(c, "movies", "cast_list", "TEXT");
            addColumnIfMissing(c, "movies", "trailer_url", "TEXT");
            addColumnIfMissing(c, "movies", "release_year", "INTEGER");
            st.execute("CREATE TABLE IF NOT EXISTS shows ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE, "
                    + "show_date TEXT NOT NULL, show_time TEXT NOT NULL, hall TEXT NOT NULL, price REAL NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS bookings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER NOT NULL REFERENCES users(id), "
                    + "show_id INTEGER NOT NULL REFERENCES shows(id) ON DELETE CASCADE, "
                    + "seats TEXT NOT NULL, total REAL NOT NULL, booked_at TEXT NOT NULL, status TEXT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS booked_seats ("
                    + "show_id INTEGER NOT NULL REFERENCES shows(id) ON DELETE CASCADE, "
                    + "seat TEXT NOT NULL, "
                    + "booking_id INTEGER NOT NULL REFERENCES bookings(id) ON DELETE CASCADE, "
                    + "PRIMARY KEY (show_id, seat))");
            seedCatalog(c);
            backfillMovieDetails(c);
            backfillYears(c);
            topUpShows(c);
        }
        AppLogger.get().info("Database ready: " + URL);
    }

    /** title, genre, minutes, rating, synopsis, director, cast, release year */
    private static final Object[][] SAMPLE_MOVIES = {
        {"Inception", "Sci-Fi", 148, 8.8,
            "A thief who steals secrets through dream-sharing is offered a chance to erase his criminal past, by planting an idea inside a target's mind.",
            "Christopher Nolan",
            "Leonardo DiCaprio (Cobb); Joseph Gordon-Levitt (Arthur); Elliot Page (Ariadne); Tom Hardy (Eames)", 2010},
        {"The Dark Knight", "Action", 152, 9.0,
            "Batman faces the Joker, a criminal mastermind who pushes Gotham into chaos and tests the limits of the Dark Knight's code.",
            "Christopher Nolan",
            "Christian Bale (Batman); Heath Ledger (Joker); Aaron Eckhart (Harvey Dent); Gary Oldman (Gordon)", 2008},
        {"Interstellar", "Sci-Fi", 169, 8.7,
            "With Earth running out of time, a former pilot leads a crew through a wormhole to find humanity a new home.",
            "Christopher Nolan",
            "Matthew McConaughey (Cooper); Anne Hathaway (Brand); Jessica Chastain (Murph); Michael Caine (Prof. Brand)", 2014},
        {"3 Idiots", "Comedy", 170, 8.4,
            "Two friends search for their long-lost college buddy and remember how his unusual ideas changed the way they saw education and life.",
            "Rajkumar Hirani",
            "Aamir Khan (Rancho); R. Madhavan (Farhan); Sharman Joshi (Raju); Kareena Kapoor (Pia)", 2009},
        {"Dangal", "Drama", 161, 8.3,
            "A former wrestler trains his daughters to become world-class wrestlers, breaking social barriers along the way.",
            "Nitesh Tiwari",
            "Aamir Khan (Mahavir Phogat); Fatima Sana Shaikh (Geeta); Sanya Malhotra (Babita); Sakshi Tanwar (Daya)", 2016},
        {"Spider-Man: No Way Home", "Action", 148, 8.2,
            "After his identity is revealed, Peter asks Doctor Strange for help, but a spell goes wrong and opens the multiverse.",
            "Jon Watts",
            "Tom Holland (Peter Parker); Zendaya (MJ); Benedict Cumberbatch (Doctor Strange); Jacob Batalon (Ned)", 2021},
        {"The Shawshank Redemption", "Drama", 142, 9.3,
            "A banker jailed for a crime he says he did not commit befriends a fellow inmate and holds on to hope through decades behind bars.",
            "Frank Darabont",
            "Tim Robbins (Andy Dufresne); Morgan Freeman (Red); Bob Gunton (Warden Norton); Clancy Brown (Captain Hadley)", 1994},
        {"The Godfather", "Drama", 175, 9.2,
            "The aging head of a powerful crime family hands his empire to his reluctant youngest son, who is pulled deeper into the family business.",
            "Francis Ford Coppola",
            "Marlon Brando (Vito Corleone); Al Pacino (Michael Corleone); James Caan (Sonny); Robert Duvall (Tom Hagen)", 1972},
        {"Titanic", "Romance", 194, 7.9,
            "A young artist and a first-class passenger fall in love aboard the doomed ocean liner on its maiden voyage.",
            "James Cameron",
            "Leonardo DiCaprio (Jack); Kate Winslet (Rose); Billy Zane (Cal); Kathy Bates (Molly Brown)", 1997},
        {"Avatar", "Sci-Fi", 162, 7.9,
            "A paralysed marine joins a mission on the moon Pandora, where he comes to side with the Na'vi people against his own.",
            "James Cameron",
            "Sam Worthington (Jake Sully); Zoe Saldana (Neytiri); Sigourney Weaver (Dr. Grace Augustine); Stephen Lang (Col. Quaritch)", 2009},
        {"Avengers: Endgame", "Action", 181, 8.4,
            "After half of all life is wiped out, the remaining Avengers team up for one last mission to undo the damage.",
            "Anthony Russo, Joe Russo",
            "Robert Downey Jr. (Tony Stark); Chris Evans (Steve Rogers); Mark Ruffalo (Bruce Banner); Scarlett Johansson (Natasha)", 2019},
        {"Jurassic Park", "Adventure", 127, 8.2,
            "A theme park of cloned dinosaurs turns deadly when the security system fails and visitors are stranded on the island.",
            "Steven Spielberg",
            "Sam Neill (Dr. Grant); Laura Dern (Dr. Sattler); Jeff Goldblum (Dr. Malcolm); Richard Attenborough (John Hammond)", 1993},
        {"Forrest Gump", "Drama", 142, 8.8,
            "A kind-hearted man with a low IQ drifts through decades of American history while holding on to his love for his childhood friend.",
            "Robert Zemeckis",
            "Tom Hanks (Forrest); Robin Wright (Jenny); Gary Sinise (Lt. Dan); Sally Field (Mrs. Gump)", 1994},
        {"Sholay", "Action", 204, 8.1,
            "Two small-time crooks are hired by a former police officer to catch Gabbar Singh, a feared dacoit terrorising a village.",
            "Ramesh Sippy",
            "Amitabh Bachchan (Jai); Dharmendra (Veeru); Hema Malini (Basanti); Amjad Khan (Gabbar Singh)", 1975},
        {"Dilwale Dulhania Le Jayenge", "Romance", 189, 8.0,
            "Two young Indians fall in love on a trip across Europe, but she is already promised to someone back home in Punjab.",
            "Aditya Chopra",
            "Shah Rukh Khan (Raj); Kajol (Simran); Amrish Puri (Baldev Singh); Farida Jalal (Lajwanti)", 1995},
        {"Oppenheimer", "Drama", 180, 8.3,
            "The story of physicist J. Robert Oppenheimer and the race to build the atomic bomb during World War II, and its aftermath.",
            "Christopher Nolan",
            "Cillian Murphy (J. Robert Oppenheimer); Emily Blunt (Kitty); Matt Damon (Gen. Groves); Robert Downey Jr. (Lewis Strauss)", 2023},
        {"Dune: Part Two", "Sci-Fi", 166, 8.5,
            "Paul Atreides joins the Fremen on Arrakis to take revenge on those who destroyed his family, while facing a terrible vision of the future.",
            "Denis Villeneuve",
            "Timothee Chalamet (Paul Atreides); Zendaya (Chani); Rebecca Ferguson (Lady Jessica); Austin Butler (Feyd-Rautha)", 2024},
        {"Jawan", "Action", 169, 7.0,
            "A man with a mysterious past sets out to fix society's wrongs and take on a powerful arms dealer.",
            "Atlee",
            "Shah Rukh Khan (Vikram Rathore / Azad); Nayanthara (Narmada); Vijay Sethupathi (Kaali); Deepika Padukone (Aishwarya)", 2023},
        {"RRR", "Action", 182, 7.8,
            "Two fearless revolutionaries become friends and later face a painful choice in 1920s India under British rule.",
            "S. S. Rajamouli",
            "N. T. Rama Rao Jr. (Komaram Bheem); Ram Charan (Alluri Sitarama Raju); Alia Bhatt (Sita); Ajay Devgn (Venkata Rama Raju)", 2022},
        {"Barbie", "Comedy", 114, 6.8,
            "Barbie and Ken leave Barbieland for the real world and discover what it means to be human.",
            "Greta Gerwig",
            "Margot Robbie (Barbie); Ryan Gosling (Ken); America Ferrera (Gloria); Kate McKinnon (Weird Barbie)", 2023}
    };

    /** Fills synopsis / director / cast for the sample movies if they are empty (also upgrades old databases). */
    private static void backfillMovieDetails(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE movies SET synopsis = ?, director = ?, cast_list = ? "
                + "WHERE title = ? AND (synopsis IS NULL OR synopsis = '')")) {
            for (Object[] m : SAMPLE_MOVIES) {
                ps.setString(1, (String) m[4]);
                ps.setString(2, (String) m[5]);
                ps.setString(3, (String) m[6]);
                ps.setString(4, (String) m[0]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void addColumnIfMissing(Connection c, String table, String column, String type) throws SQLException {
        boolean found = false;
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    found = true;
                }
            }
        }
        if (!found) {
            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            }
        }
    }

    /** If a movie has no upcoming show left (dates passed), add fresh shows so the demo never looks empty. */
    private static void topUpShows(Connection c) throws SQLException {
        String[] times = {"10:00", "14:30", "18:30"};
        LocalDate today = LocalDate.now();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT m.id FROM movies m WHERE NOT EXISTS "
                     + "(SELECT 1 FROM shows s WHERE s.movie_id = m.id AND s.show_date >= date('now','localtime'))");
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO shows(movie_id, show_date, show_time, hall, price) VALUES (?,?,?,?,?)")) {
            int added = 0;
            while (rs.next()) {
                int movieId = rs.getInt(1);
                for (int day = 0; day < 4; day++) {
                    for (int t = 0; t < times.length; t++) {
                        ps.setInt(1, movieId);
                        ps.setString(2, today.plusDays(day).toString());
                        ps.setString(3, times[t]);
                        ps.setString(4, "Hall " + (t % 2 + 1));
                        ps.setDouble(5, t == 2 ? 300.0 : 220.0);
                        ps.addBatch();
                        added++;
                    }
                }
            }
            ps.executeBatch();
            if (added > 0) {
                AppLogger.get().info("Added " + added + " fresh shows for movies without upcoming shows.");
            }
        }
    }

    /** Adds any sample movie that is missing. Runs once per catalog version, so deleted movies stay deleted. */
    private static void seedCatalog(Connection c) throws SQLException {
        final String version = "3";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(
                "SELECT v FROM app_meta WHERE k = 'catalog_version'")) {
            if (rs.next() && version.equals(rs.getString(1))) {
                return;
            }
        }
        int added = 0;
        try (PreparedStatement exists = c.prepareStatement("SELECT 1 FROM movies WHERE title = ?");
             PreparedStatement pm = c.prepareStatement(
                     "INSERT INTO movies(title, genre, duration, rating, synopsis, director, cast_list, release_year) "
                     + "VALUES (?,?,?,?,?,?,?,?)")) {
            for (Object[] m : SAMPLE_MOVIES) {
                exists.setString(1, (String) m[0]);
                try (ResultSet rs = exists.executeQuery()) {
                    if (rs.next()) {
                        continue;
                    }
                }
                pm.setString(1, (String) m[0]);
                pm.setString(2, (String) m[1]);
                pm.setInt(3, (Integer) m[2]);
                pm.setDouble(4, (Double) m[3]);
                pm.setString(5, (String) m[4]);
                pm.setString(6, (String) m[5]);
                pm.setString(7, (String) m[6]);
                pm.setInt(8, (Integer) m[7]);
                pm.executeUpdate();
                added++;
            }
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO app_meta(k, v) VALUES ('catalog_version', ?)")) {
            ps.setString(1, version);
            ps.executeUpdate();
        }
        AppLogger.get().info("Sample catalog ready, " + added + " movies added.");
    }

    /** Release years for sample movies that were created before the year column existed. */
    private static void backfillYears(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE movies SET release_year = ? WHERE title = ? AND (release_year IS NULL OR release_year = 0)")) {
            for (Object[] m : SAMPLE_MOVIES) {
                ps.setInt(1, (Integer) m[7]);
                ps.setString(2, (String) m[0]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
