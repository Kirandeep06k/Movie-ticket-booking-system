package com.mbs.service;

import com.mbs.dao.CustomerDao;
import com.mbs.exception.BookingException;
import com.mbs.exception.InvalidInputException;
import com.mbs.model.Admin;
import com.mbs.model.Customer;
import com.mbs.model.Person;
import com.mbs.util.AppConfig;
import com.mbs.util.AppLogger;
import com.mbs.util.PasswordUtil;
import com.mbs.util.Validator;

import java.sql.SQLException;
import java.util.Optional;

public class UserService {
    private final CustomerDao dao = new CustomerDao();

    private boolean isAdminEmail(String email) {
        return AppConfig.get("admin.email", "admin@theatrely.com").equalsIgnoreCase(email);
    }

    /** Register a new customer account (name + email + password). */
    public Person register(String name, String email, String password) throws BookingException {
        String n = Validator.name(name);
        String e = Validator.email(email);
        String p = Validator.password(password);
        if (isAdminEmail(e)) {
            throw new InvalidInputException("This email is reserved. Please use another one.");
        }
        try {
            String salt = PasswordUtil.newSalt();
            String hash = PasswordUtil.hash(p, salt);
            Optional<Customer> existing = dao.findByEmail(e);
            if (existing.isPresent()) {
                String[] cred = dao.getCredentials(existing.get().getId());
                if (cred[0] != null) {
                    throw new InvalidInputException("An account with this email already exists. Please login.");
                }
                dao.setCredentials(existing.get().getId(), n, hash, salt);   // very old account without password
                AppLogger.get().info("Password set for existing account: " + e);
                return new Customer(existing.get().getId(), n, e);
            }
            int id = dao.insert(new Customer(0, n, e), hash, salt);
            AppLogger.get().info("New customer registered: " + e);
            return new Customer(id, n, e);
        } catch (SQLException ex) {
            throw new BookingException("Registration failed: " + ex.getMessage(), ex);
        }
    }

    /** Login with email + password. The admin account comes from app.properties. */
    public Person login(String email, String password) throws BookingException {
        String e = Validator.email(email);
        if (password == null || password.isEmpty()) {
            throw new InvalidInputException("Please enter your password.");
        }
        if (isAdminEmail(e)) {
            if (AppConfig.get("admin.password", "admin123").equals(password)) {
                AppLogger.get().info("Admin logged in");
                return new Admin(0, "Admin", e);
            }
            AppLogger.get().warning("Failed admin login");
            throw new InvalidInputException("Wrong email or password.");
        }
        try {
            Optional<Customer> found = dao.findByEmail(e);
            if (found.isPresent()) {
                String[] cred = dao.getCredentials(found.get().getId());
                if (PasswordUtil.matches(password, cred[1], cred[0])) {
                    AppLogger.get().info("Customer logged in: " + e);
                    return found.get();
                }
            }
            AppLogger.get().warning("Failed login for " + e);
            throw new InvalidInputException("Wrong email or password.");
        } catch (SQLException ex) {
            throw new BookingException("Login failed: " + ex.getMessage(), ex);
        }
    }

    /** Saved {state, city} of a customer, or {null, null}. */
    public String[] loadLocation(Person p) {
        if (p instanceof Customer) {
            try {
                return dao.getLocation(p.getId());
            } catch (SQLException e) {
                AppLogger.get().warning("Could not load location: " + e.getMessage());
            }
        }
        return new String[]{null, null};
    }

    public void saveLocation(Person p, String state, String city) {
        if (p instanceof Customer) {
            try {
                dao.setLocation(p.getId(), state, city);
            } catch (SQLException e) {
                AppLogger.get().warning("Could not save location: " + e.getMessage());
            }
        }
    }
}
