package com.mbs.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Show implements Payable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private int id;
    private int movieId;
    private String movieTitle;
    private LocalDate date;
    private LocalTime time;
    private String hall;
    private double ticketPrice;

    public Show(int id, int movieId, String movieTitle, LocalDate date, LocalTime time, String hall, double ticketPrice) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.date = date;
        this.time = time;
        this.hall = hall;
        this.ticketPrice = ticketPrice;
    }

    @Override
    public double priceFor(int seats) { return ticketPrice * seats; }

    public LocalDateTime getDateTime() { return LocalDateTime.of(date, time); }
    public int getId() { return id; }
    public int getMovieId() { return movieId; }
    public String getMovieTitle() { return movieTitle; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getHall() { return hall; }
    public double getTicketPrice() { return ticketPrice; }

    @Override
    public String toString() {
        return getDateTime().format(FMT) + "  |  " + hall + "  |  Rs." + ticketPrice;
    }
}
