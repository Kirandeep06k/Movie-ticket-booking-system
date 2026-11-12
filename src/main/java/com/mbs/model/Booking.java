package com.mbs.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int customerId;
    private int showId;
    private String movieTitle;
    private LocalDateTime showTime;
    private String seats;          // "A1,A2,B5"
    private double total;
    private LocalDateTime bookedAt;
    private String status;         // CONFIRMED / CANCELLED

    public Booking(int id, int customerId, int showId, String movieTitle, LocalDateTime showTime,
                   String seats, double total, LocalDateTime bookedAt, String status) {
        this.id = id;
        this.customerId = customerId;
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.showTime = showTime;
        this.seats = seats;
        this.total = total;
        this.bookedAt = bookedAt;
        this.status = status;
    }

    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public int getShowId() { return showId; }
    public String getMovieTitle() { return movieTitle; }
    public LocalDateTime getShowTime() { return showTime; }
    public String getSeats() { return seats; }
    public double getTotal() { return total; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public String getStatus() { return status; }
    public void setId(int id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "#" + id + " " + movieTitle + " [" + seats + "] Rs." + total + " " + status;
    }
}
