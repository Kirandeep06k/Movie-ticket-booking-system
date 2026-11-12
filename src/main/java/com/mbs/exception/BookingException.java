package com.mbs.exception;

/** Base custom (checked) exception for the whole app. */
public class BookingException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookingException(String message) { super(message); }
    public BookingException(String message, Throwable cause) { super(message, cause); }
}
