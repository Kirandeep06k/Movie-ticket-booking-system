package com.mbs.service;

import com.mbs.dao.BookingDao;
import com.mbs.exception.BookingException;
import com.mbs.exception.InvalidInputException;
import com.mbs.exception.SeatUnavailableException;
import com.mbs.model.Booking;
import com.mbs.model.Customer;
import com.mbs.model.Show;
import com.mbs.util.AppConfig;
import com.mbs.util.AppLogger;
import com.mbs.util.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/** Booking logic + transaction management (commit / rollback). */
public class BookingService {
    public static final char[] ROWS = {'A', 'B', 'C', 'D', 'E'};
    public static final int COLS = 8;
    private static final int RECENT_LIMIT = 8;

    private final BookingDao dao = new BookingDao();
    /** LinkedList used as a queue of recent activity (newest first). */
    private static final LinkedList<String> RECENT = new LinkedList<>();

    public static List<String> allSeats() {
        List<String> seats = new ArrayList<>();
        for (char r : ROWS) {
            for (int c = 1; c <= COLS; c++) {
                seats.add(r + String.valueOf(c));
            }
        }
        return seats;
    }

    public static List<String> recentActivity() { return new ArrayList<>(RECENT); }

    private static void remember(String text) {
        RECENT.addFirst(text);
        while (RECENT.size() > RECENT_LIMIT) {
            RECENT.removeLast();
        }
    }

    /** Free seats: starts with all seats and removes booked ones through an Iterator. */
    public List<String> availableSeats(Show show) throws BookingException {
        try {
            Set<String> booked = dao.findBookedSeats(show.getId());
            List<String> seats = allSeats();
            Iterator<String> it = seats.iterator();
            while (it.hasNext()) {
                if (booked.contains(it.next())) {
                    it.remove();
                }
            }
            return seats;
        } catch (SQLException e) {
            throw new BookingException("Could not load seats: " + e.getMessage(), e);
        }
    }

    public Set<String> bookedSeats(Show show) throws BookingException {
        try {
            return dao.findBookedSeats(show.getId());
        } catch (SQLException e) {
            throw new BookingException("Could not load seats: " + e.getMessage(), e);
        }
    }

    public int discountPercent(Customer c) throws BookingException {
        try {
            int confirmed = dao.countConfirmed(c.getId());
            c.setActiveBookings(confirmed);
            boolean loyal = confirmed >= AppConfig.getInt("loyal.after.bookings", 5);
            return loyal ? AppConfig.getInt("loyal.discount.percent", 10) : 0;
        } catch (SQLException e) {
            throw new BookingException("Could not check discount: " + e.getMessage(), e);
        }
    }

    /** Price = (tickets - discount) + GST, rounded to 2 decimals using Math. */
    public double calculateTotal(Show show, int seatCount, int discountPercent) {
        double subtotal = show.priceFor(seatCount);
        double discount = subtotal * discountPercent / 100.0;
        double gst = (subtotal - discount) * AppConfig.getDouble("gst.percent", 18) / 100.0;
        return Math.round((subtotal - discount + gst) * 100.0) / 100.0;
    }

    public String priceSummary(Show show, int seatCount, int discountPercent) {
        double subtotal = show.priceFor(seatCount);
        double discount = subtotal * discountPercent / 100.0;
        double gstPercent = AppConfig.getDouble("gst.percent", 18);
        double gst = (subtotal - discount) * gstPercent / 100.0;
        return String.format("Tickets (%d x %.2f) : %9.2f%nDiscount (%d%%)       : %9.2f%nGST (%.0f%%)           : %9.2f%n----------------------------------%nTOTAL                : %9.2f",
                seatCount, show.getTicketPrice(), subtotal, discountPercent, -discount, gstPercent, gst,
                calculateTotal(show, seatCount, discountPercent));
    }

    /** Books seats inside one transaction: everything is saved, or nothing is. */
    public Booking bookSeats(Customer customer, Show show, Collection<String> seats) throws BookingException {
        Objects.requireNonNull(customer, "customer must not be null");
        Objects.requireNonNull(show, "show must not be null");
        if (seats == null || seats.isEmpty()) {
            throw new InvalidInputException("Select at least one seat.");
        }
        int max = AppConfig.getInt("max.seats.per.booking", 6);
        if (seats.size() > max) {
            throw new InvalidInputException("You can book at most " + max + " seats at a time.");
        }
        int discount = discountPercent(customer);
        double total = calculateTotal(show, seats.size(), discount);
        String seatText = String.join(",", new TreeSet<>(seats));

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);                       // start transaction
            Set<String> taken = dao.findBookedSeats(conn, show.getId());
            for (String seat : seats) {
                if (taken.contains(seat)) {
                    throw new SeatUnavailableException(seat);
                }
            }
            Booking b = new Booking(0, customer.getId(), show.getId(), show.getMovieTitle(), show.getDateTime(),
                    seatText, total, LocalDateTime.now().withNano(0), "CONFIRMED");
            int id = dao.insert(conn, b);
            b.setId(id);
            for (String seat : seats) {
                dao.insertSeat(conn, show.getId(), seat, id);
            }
            conn.commit();                                   // all good
            remember("Booked #" + id + " " + show.getMovieTitle() + " [" + seatText + "]");
            AppLogger.get().info("Booking #" + id + " confirmed for " + customer.getEmail() + " seats " + seatText);
            return b;
        } catch (SQLException e) {
            rollback(conn);
            AppLogger.get().severe("Booking failed: " + e.getMessage());
            throw new BookingException("Booking failed, nothing was saved: " + e.getMessage(), e);
        } catch (BookingException e) {
            rollback(conn);
            AppLogger.get().warning("Booking rejected: " + e.getMessage());
            throw e;
        } finally {
            close(conn);
        }
    }

    public void cancelBooking(Booking b) throws BookingException {
        Objects.requireNonNull(b, "booking must not be null");
        if (!"CONFIRMED".equals(b.getStatus())) {
            throw new InvalidInputException("This booking is already cancelled.");
        }
        if (b.getShowTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Past shows cannot be cancelled.");
        }
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            dao.cancel(conn, b.getId());
            conn.commit();
            b.setStatus("CANCELLED");
            remember("Cancelled #" + b.getId() + " " + b.getMovieTitle());
            AppLogger.get().info("Booking #" + b.getId() + " cancelled");
        } catch (SQLException e) {
            rollback(conn);
            throw new BookingException("Cancel failed: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    public List<Booking> bookingsOf(Customer c) throws BookingException {
        try {
            return dao.findByCustomer(c.getId());
        } catch (SQLException e) {
            throw new BookingException("Could not load bookings: " + e.getMessage(), e);
        }
    }

    public List<Booking> allBookings() throws BookingException {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            throw new BookingException("Could not load bookings: " + e.getMessage(), e);
        }
    }

    private static void rollback(Connection c) {
        if (c != null) {
            try {
                c.rollback();
            } catch (SQLException ex) {
                AppLogger.get().severe("Rollback failed: " + ex.getMessage());
            }
        }
    }

    private static void close(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException ex) {
                AppLogger.get().severe("Close failed: " + ex.getMessage());
            }
        }
    }
}
