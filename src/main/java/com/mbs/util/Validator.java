package com.mbs.util;

import com.mbs.exception.InvalidInputException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Input validation helpers (all static). */
public final class Validator {
    private Validator() { }

    public static String name(String s) throws InvalidInputException {
        if (s == null || s.trim().length() < 2) {
            throw new InvalidInputException("Name must be at least 2 characters.");
        }
        return s.trim();
    }

    public static String email(String s) throws InvalidInputException {
        if (s == null || !s.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new InvalidInputException("Please enter a valid email address.");
        }
        return s.trim().toLowerCase();
    }

    public static String password(String s) throws InvalidInputException {
        if (s == null || s.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }
        return s;
    }

    public static int year(String s) throws InvalidInputException {
        int y = positiveInt(s, "Release year");
        if (y < 1888 || y > LocalDate.now().getYear() + 2) {
            throw new InvalidInputException("Release year looks wrong.");
        }
        return y;
    }

    public static String required(String s, String field) throws InvalidInputException {
        if (s == null || s.trim().isEmpty()) {
            throw new InvalidInputException(field + " cannot be empty.");
        }
        return s.trim();
    }

    public static int positiveInt(String s, String field) throws InvalidInputException {
        try {
            int v = Integer.parseInt(s.trim());
            if (v <= 0) {
                throw new InvalidInputException(field + " must be greater than 0.");
            }
            return v;
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidInputException(field + " must be a whole number.");
        }
    }

    public static double decimal(String s, String field, double min, double max) throws InvalidInputException {
        try {
            double v = Double.parseDouble(s.trim());
            if (v < min || v > max) {
                throw new InvalidInputException(field + " must be between " + min + " and " + max + ".");
            }
            return v;
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidInputException(field + " must be a number.");
        }
    }

    public static LocalTime time(String s) throws InvalidInputException {
        try {
            return LocalTime.parse(s.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new InvalidInputException("Time must be in HH:mm format, e.g. 18:30");
        }
    }
}
