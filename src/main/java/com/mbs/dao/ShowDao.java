package com.mbs.dao;

import com.mbs.model.Show;
import com.mbs.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShowDao implements GenericDao<Show> {
    private static final String BASE =
            "SELECT s.id, s.movie_id, m.title, s.show_date, s.show_time, s.hall, s.price "
            + "FROM shows s JOIN movies m ON m.id = s.movie_id ";

    private Show map(ResultSet rs) throws SQLException {
        return new Show(rs.getInt("id"), rs.getInt("movie_id"), rs.getString("title"),
                LocalDate.parse(rs.getString("show_date")), LocalTime.parse(rs.getString("show_time")),
                rs.getString("hall"), rs.getDouble("price"));
    }

    @Override
    public Optional<Show> findById(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE s.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Show> findByMovie(int movieId) throws SQLException {
        List<Show> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     BASE + "WHERE s.movie_id = ? ORDER BY s.show_date, s.show_time")) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Show> findAll() throws SQLException {
        List<Show> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "ORDER BY s.show_date, s.show_time");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public int insert(Show s) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO shows(movie_id, show_date, show_time, hall, price) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, s.getMovieId());
            ps.setString(2, s.getDate().toString());
            ps.setString(3, s.getTime().toString());
            ps.setString(4, s.getHall());
            ps.setDouble(5, s.getTicketPrice());
            ps.executeUpdate();
            return DatabaseManager.lastInsertId(c);
        }
    }

    @Override
    public boolean update(Show s) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE shows SET show_date = ?, show_time = ?, hall = ?, price = ? WHERE id = ?")) {
            ps.setString(1, s.getDate().toString());
            ps.setString(2, s.getTime().toString());
            ps.setString(3, s.getHall());
            ps.setDouble(4, s.getTicketPrice());
            ps.setInt(5, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM shows WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
