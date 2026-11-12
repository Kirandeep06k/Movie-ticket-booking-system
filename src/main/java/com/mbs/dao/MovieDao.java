package com.mbs.dao;

import com.mbs.model.Movie;
import com.mbs.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieDao implements GenericDao<Movie> {

    private Movie map(ResultSet rs) throws SQLException {
        return new Movie(rs.getInt("id"), rs.getString("title"), rs.getString("genre"),
                rs.getInt("duration"), rs.getDouble("rating"), rs.getString("poster"),
                rs.getString("synopsis"), rs.getString("director"), rs.getString("cast_list"), rs.getString("trailer_url"),
                rs.getInt("release_year"));
    }

    @Override
    public Optional<Movie> findById(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM movies WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Movie> findAll() throws SQLException {
        List<Movie> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM movies ORDER BY title")) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public int insert(Movie m) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO movies(title, genre, duration, rating, poster, synopsis, director, cast_list, trailer_url, release_year) "
                     + "VALUES (?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getGenre());
            ps.setInt(3, m.getDuration());
            ps.setDouble(4, m.getRating());
            ps.setString(5, m.getPoster());
            ps.setString(6, m.getSynopsis());
            ps.setString(7, m.getDirector());
            ps.setString(8, m.getCast());
            ps.setString(9, m.getTrailerUrl());
            ps.setInt(10, m.getYear());
            ps.executeUpdate();
            return DatabaseManager.lastInsertId(c);
        }
    }

    @Override
    public boolean update(Movie m) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE movies SET title = ?, genre = ?, duration = ?, rating = ?, poster = ?, synopsis = ?, "
                     + "director = ?, cast_list = ?, trailer_url = ?, release_year = ? WHERE id = ?")) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getGenre());
            ps.setInt(3, m.getDuration());
            ps.setDouble(4, m.getRating());
            ps.setString(5, m.getPoster());
            ps.setString(6, m.getSynopsis());
            ps.setString(7, m.getDirector());
            ps.setString(8, m.getCast());
            ps.setString(9, m.getTrailerUrl());
            ps.setInt(10, m.getYear());
            ps.setInt(11, m.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM movies WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
