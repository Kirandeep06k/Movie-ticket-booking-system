package com.mbs.service;

import com.mbs.dao.MovieDao;
import com.mbs.dao.ShowDao;
import com.mbs.exception.BookingException;
import com.mbs.exception.InvalidInputException;
import com.mbs.model.Movie;
import com.mbs.model.Show;
import com.mbs.util.AppLogger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/** Movies and shows: collections, generics, comparators, iterators, Optional. */
public class CatalogService {
    public static final String SORT_TITLE = "Title (A-Z)";
    public static final String SORT_RATING = "Rating (high to low)";
    public static final String SORT_DURATION = "Duration (short to long)";
    public static final String SORT_YEAR = "Newest first";

    private final MovieDao movieDao = new MovieDao();
    private final ShowDao showDao = new ShowDao();

    public List<Movie> allMovies() throws BookingException {
        try {
            return movieDao.findAll();
        } catch (SQLException e) {
            throw new BookingException("Could not load movies: " + e.getMessage(), e);
        }
    }

    public Optional<Movie> findMovie(int id) throws BookingException {
        try {
            return movieDao.findById(id);
        } catch (SQLException e) {
            throw new BookingException("Could not load movie: " + e.getMessage(), e);
        }
    }

    /** Distinct genres using a HashSet, returned as a sorted list. */
    public List<String> genres(Collection<Movie> movies) {
        Set<String> set = new HashSet<>();
        for (Movie m : movies) {
            set.add(m.getGenre());
        }
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    /** HashMap: genre -> number of movies. */
    public Map<String, Integer> genreCounts(Collection<Movie> movies) {
        Map<String, Integer> counts = new HashMap<>();
        for (Movie m : movies) {
            counts.put(m.getGenre(), counts.getOrDefault(m.getGenre(), 0) + 1);
        }
        return counts;
    }

    /** Generic method: works for any type T. */
    public static <T> List<T> filter(Collection<T> items, Predicate<T> rule) {
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (rule.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /** Sorting with Comparator objects. */
    public static void sortMovies(List<Movie> movies, String criterion) {
        Comparator<Movie> cmp;
        if (SORT_RATING.equals(criterion)) {
            cmp = Comparator.comparingDouble(Movie::getRating).reversed();
        } else if (SORT_YEAR.equals(criterion)) {
            cmp = Comparator.comparingInt(Movie::getYear).reversed();
        } else if (SORT_DURATION.equals(criterion)) {
            cmp = Comparator.comparingInt(Movie::getDuration);
        } else {
            cmp = Comparator.naturalOrder();     // uses Movie.compareTo (Comparable)
        }
        movies.sort(cmp);
    }

    /** Upcoming shows only: removes past shows using an Iterator. */
    public List<Show> upcomingShows(Movie movie) throws BookingException {
        Objects.requireNonNull(movie, "movie must not be null");
        try {
            List<Show> shows = showDao.findByMovie(movie.getId());
            LocalDateTime now = LocalDateTime.now();
            Iterator<Show> it = shows.iterator();
            while (it.hasNext()) {
                if (it.next().getDateTime().isBefore(now)) {
                    it.remove();
                }
            }
            return shows;
        } catch (SQLException e) {
            throw new BookingException("Could not load shows: " + e.getMessage(), e);
        }
    }

    public int addMovie(String title, String genre, int duration, double rating, String poster,
                        String synopsis, String director, String cast, String trailer, int year) throws BookingException {
        try {
            int id = movieDao.insert(new Movie(0, title, genre, duration, rating, poster,
                    synopsis, director, cast, trailer, year));
            AppLogger.get().info("Movie added: " + title);
            return id;
        } catch (SQLException e) {
            throw new BookingException("Could not add movie: " + e.getMessage(), e);
        }
    }

    public void deleteMovie(Movie m) throws BookingException {
        try {
            movieDao.delete(m.getId());
            AppLogger.get().info("Movie deleted: " + m.getTitle());
        } catch (SQLException e) {
            throw new BookingException("Could not delete movie: " + e.getMessage(), e);
        }
    }

    public int addShow(Movie movie, LocalDate date, LocalTime time, String hall, double price)
            throws BookingException {
        if (movie == null) {
            throw new InvalidInputException("Select a movie first.");
        }
        if (date == null) {
            throw new InvalidInputException("Select a show date.");
        }
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Show time must be in the future.");
        }
        try {
            int id = showDao.insert(new Show(0, movie.getId(), movie.getTitle(), date, time, hall, price));
            AppLogger.get().info("Show added for " + movie.getTitle() + " on " + date + " " + time);
            return id;
        } catch (SQLException e) {
            throw new BookingException("Could not add show: " + e.getMessage(), e);
        }
    }
}
