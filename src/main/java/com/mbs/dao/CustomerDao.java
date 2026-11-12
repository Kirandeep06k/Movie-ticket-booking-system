package com.mbs.dao;

import com.mbs.model.Customer;
import com.mbs.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDao implements GenericDao<Customer> {

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
    }

    @Override
    public Optional<Customer> findById(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Customer> findByEmail(String email) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY name")) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public int insert(Customer cu) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO users(name, email) VALUES (?, ?)")) {
            ps.setString(1, cu.getName());
            ps.setString(2, cu.getEmail());
            ps.executeUpdate();
            return DatabaseManager.lastInsertId(c);
        }
    }

    /** Inserts a customer together with salt + password hash. */
    public int insert(Customer cu, String hash, String salt) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(name, email, password_hash, salt) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, cu.getName());
            ps.setString(2, cu.getEmail());
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.executeUpdate();
            return DatabaseManager.lastInsertId(c);
        }
    }

    /** Returns {state, city} (values can be null if never chosen). */
    public String[] getLocation(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT state, city FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new String[]{rs.getString(1), rs.getString(2)} : new String[]{null, null};
            }
        }
    }

    public void setLocation(int id, String state, String city) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET state = ?, city = ? WHERE id = ?")) {
            ps.setString(1, state);
            ps.setString(2, city);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /** Returns {hash, salt} for a user (values can be null for very old accounts). */
    public String[] getCredentials(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT password_hash, salt FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new String[]{rs.getString(1), rs.getString(2)} : new String[]{null, null};
            }
        }
    }

    public void setCredentials(int id, String name, String hash, String salt) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE users SET name = ?, password_hash = ?, salt = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean update(Customer cu) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET name = ?, email = ? WHERE id = ?")) {
            ps.setString(1, cu.getName());
            ps.setString(2, cu.getEmail());
            ps.setInt(3, cu.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
