package com.mbs.dao;

import com.mbs.model.Booking;
import com.mbs.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Booking DAO. Methods that take a Connection are used inside a transaction by BookingService. */
public class BookingDao implements GenericDao<Booking> {
    private static final String BASE =
            "SELECT b.id, b.user_id, b.show_id, m.title, s.show_date, s.show_time, "
            + "b.seats, b.total, b.booked_at, b.status "
            + "FROM bookings b JOIN shows s ON s.id = b.show_id JOIN movies m ON m.id = s.movie_id ";

    private Booking map(ResultSet rs) throws SQLException {
        LocalDateTime showTime = LocalDateTime.of(LocalDate.parse(rs.getString("show_date")),
                LocalTime.parse(rs.getString("show_time")));
        return new Booking(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("show_id"), rs.getString("title"),
                showTime, rs.getString("seats"), rs.getDouble("total"),
                LocalDateTime.parse(rs.getString("booked_at")), rs.getString("status"));
    }

    @Override
    public Optional<Booking> findById(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE b.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "ORDER BY b.id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Booking> findByCustomer(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE b.user_id = ? ORDER BY b.id DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public int countConfirmed(int userId) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM bookings WHERE user_id = ? AND status = 'CONFIRMED'")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Seats already taken for a show (HashSet). */
    public Set<String> findBookedSeats(int showId) throws SQLException {
        try (Connection c = DatabaseManager.getConnection()) {
            return findBookedSeats(c, showId);
        }
    }

    public Set<String> findBookedSeats(Connection c, int showId) throws SQLException {
        Set<String> seats = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT seat FROM booked_seats WHERE show_id = ?")) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(rs.getString(1));
                }
            }
        }
        return seats;
    }

    public int insert(Connection c, Booking b) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO bookings(user_id, show_id, seats, total, booked_at, status) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, b.getCustomerId());
            ps.setInt(2, b.getShowId());
            ps.setString(3, b.getSeats());
            ps.setDouble(4, b.getTotal());
            ps.setString(5, b.getBookedAt().toString());
            ps.setString(6, b.getStatus());
            ps.executeUpdate();
            return DatabaseManager.lastInsertId(c);
        }
    }

    public void insertSeat(Connection c, int showId, String seat, int bookingId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO booked_seats(show_id, seat, booking_id) VALUES (?,?,?)")) {
            ps.setInt(1, showId);
            ps.setString(2, seat);
            ps.setInt(3, bookingId);
            ps.executeUpdate();
        }
    }

    public void cancel(Connection c, int bookingId) throws SQLException {
        try (PreparedStatement del = c.prepareStatement("DELETE FROM booked_seats WHERE booking_id = ?");
             PreparedStatement upd = c.prepareStatement("UPDATE bookings SET status = 'CANCELLED' WHERE id = ?")) {
            del.setInt(1, bookingId);
            del.executeUpdate();
            upd.setInt(1, bookingId);
            upd.executeUpdate();
        }
    }

    @Override
    public int insert(Booking b) throws SQLException {
        try (Connection c = DatabaseManager.getConnection()) {
            return insert(c, b);
        }
    }

    @Override
    public boolean update(Booking b) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE bookings SET status = ? WHERE id = ?")) {
            ps.setString(1, b.getStatus());
            ps.setInt(2, b.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM bookings WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
