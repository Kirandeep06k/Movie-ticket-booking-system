package com.mbs.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Movie implements Comparable<Movie>, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String genre;
    private int duration;      // minutes
    private double rating;
    private String poster;     // path of copied poster image (may be null)
    private String synopsis;
    private String director;
    private String cast;       // "Name (Role); Name (Role)"
    private String trailerUrl; // optional; if empty a YouTube search link is used
    private int year;

    public Movie(int id, String title, String genre, int duration, double rating, String poster) {
        this(id, title, genre, duration, rating, poster, null, null, null, null);
    }

    public Movie(int id, String title, String genre, int duration, double rating, String poster,
                 String synopsis, String director, String cast, String trailerUrl) {
        this(id, title, genre, duration, rating, poster, synopsis, director, cast, trailerUrl, 0);
    }

    public Movie(int id, String title, String genre, int duration, double rating, String poster,
                 String synopsis, String director, String cast, String trailerUrl, int year) {
        this.year = year;
        this.synopsis = synopsis;
        this.director = director;
        this.cast = cast;
        this.trailerUrl = trailerUrl;
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.poster = poster;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public double getRating() { return rating; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public int getYear() { return year; }
    public String getSynopsis() { return synopsis; }
    public String getDirector() { return director; }
    public String getCast() { return cast; }
    public String getTrailerUrl() { return trailerUrl; }

    /** Trailer link: the saved one, or a YouTube search for "<title> official trailer". */
    public String trailerLink() {
        if (trailerUrl != null && !trailerUrl.trim().isEmpty()) {
            return trailerUrl.trim();
        }
        try {
            return "https://www.youtube.com/results?search_query="
                    + URLEncoder.encode(title + " official trailer", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "https://www.youtube.com";
        }
    }

    /** Cast as list of {name, role}. */
    public List<String[]> getCastList() {
        List<String[]> list = new ArrayList<>();
        if (cast == null || cast.trim().isEmpty()) {
            return list;
        }
        for (String part : cast.split(";")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            int open = p.indexOf('(');
            if (open > 0 && p.endsWith(")")) {
                list.add(new String[]{p.substring(0, open).trim(), p.substring(open + 1, p.length() - 1).trim()});
            } else {
                list.add(new String[]{p, ""});
            }
        }
        return list;
    }

    @Override
    public int compareTo(Movie other) { return title.compareToIgnoreCase(other.title); }

    @Override
    public boolean equals(Object o) {
        return o instanceof Movie && ((Movie) o).id == id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return title; }
}
