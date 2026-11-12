package com.mbs.exception;

public class SeatUnavailableException extends BookingException {
    private static final long serialVersionUID = 1L;

    public SeatUnavailableException(String seat) {
        super("Seat " + seat + " is already booked. Please pick another seat.");
    }
}
