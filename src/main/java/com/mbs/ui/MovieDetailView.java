package com.mbs.ui;

import com.mbs.exception.BookingException;
import com.mbs.model.Customer;
import com.mbs.model.Movie;
import com.mbs.model.Show;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** One movie: poster, story, director, cast, trailer button and show times. */
public class MovieDetailView {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE, dd MMM");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a");
    private final Movie movie;

    public MovieDetailView(Movie movie) {
        this.movie = movie;
    }

    public Parent build() {
        Button back = Theme.outline("<  BACK TO MOVIES");
        back.setOnAction(e -> DashboardView.showHome());

        StackPane poster = Theme.poster(movie, 340, 440);

        Label title = new Label(movie.getTitle().toUpperCase());
        title.getStyleClass().add("detail-title");
        title.setWrapText(true);
        Label meta = new Label((movie.getYear() > 0 ? movie.getYear() + "   /   " : "")
                + movie.getGenre().toUpperCase() + "   /   " + movie.getDuration()
                + " MIN   /   " + movie.getRating() + " OUT OF 10");
        meta.getStyleClass().add("mono");
        meta.setStyle("-fx-background-color: #F1EEEC; -fx-background-radius: 12; -fx-padding: 5 12 5 12;");

        String story = movie.getSynopsis() == null || movie.getSynopsis().isEmpty()
                ? "No description added yet." : movie.getSynopsis();
        Label synopsis = new Label(story);
        synopsis.setWrapText(true);
        synopsis.getStyleClass().add("body-text");
        Label director = new Label("DIRECTED BY   " + (movie.getDirector() == null ? "-" : movie.getDirector()));
        director.getStyleClass().add("mono");

        Label castTitle = new Label("CAST");
        castTitle.getStyleClass().add("small-title");
        FlowPane cast = new FlowPane(22, 14);
        for (String[] c : movie.getCastList()) {
            cast.getChildren().add(castChip(c[0], c[1], "#E7E5E4"));
        }
        if (cast.getChildren().isEmpty()) {
            cast.getChildren().add(new Label("Cast not added yet."));
        }

        Button trailer = Theme.solid("WATCH TRAILER");
        trailer.setOnAction(e -> Session.openLink(movie.trailerLink()));
        Label trailerNote = new Label("opens in your browser");
        trailerNote.getStyleClass().add("mono");
        HBox trailerRow = new HBox(12, trailer, trailerNote);
        trailerRow.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(14, title, meta, synopsis, director, castTitle, cast, trailerRow);
        HBox.setHgrow(info, Priority.ALWAYS);
        HBox top = new HBox(40, poster, info);

        VBox content = new VBox(28, back, top, showtimes());
        content.setPadding(new Insets(28, 36, 48, 36));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-scroll");
        DashboardView.notifyUser(movie.getTitle());
        return scroll;
    }

    private Node castChip(String name, String role, String color) {
        String[] words = name.split("\\s+");
        String initials = words.length > 1
                ? ("" + words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase()
                : name.substring(0, 1).toUpperCase();
        Label ini = new Label(initials);
        ini.getStyleClass().add("avatar-text");
        StackPane circle = new StackPane(ini);
        circle.setPrefSize(64, 64);
        circle.setMinSize(64, 64);
        circle.setMaxSize(64, 64);
        circle.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 32; -fx-border-radius: 32; -fx-border-color: #d6d3d1;");
        Label n = new Label(name);
        n.getStyleClass().add("card-title");
        Label r = new Label(role);
        r.getStyleClass().add("mono");
        VBox text = new VBox(2, n, r);
        text.setAlignment(Pos.CENTER_LEFT);
        HBox chip = new HBox(10, circle, text);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPrefWidth(300);
        return chip;
    }

    private Node showtimes() {
        Label heading = new Label("PICK A SHOW");
        heading.getStyleClass().add("section-title");
        VBox box = new VBox(14, heading);
        List<Show> shows = new ArrayList<>();
        try {
            shows = Session.CATALOG.upcomingShows(movie);
        } catch (BookingException e) {
            Dialogs.error(e.getMessage());
        }
        if (shows.isEmpty()) {
            box.getChildren().add(new Label("No upcoming shows. Ask the admin to add some."));
            return box;
        }
        Map<LocalDate, List<Show>> byDate = new TreeMap<>();
        for (Show s : shows) {
            byDate.computeIfAbsent(s.getDate(), d -> new ArrayList<>()).add(s);
        }
        for (Map.Entry<LocalDate, List<Show>> entry : byDate.entrySet()) {
            LocalDate d = entry.getKey();
            String dayName = d.equals(LocalDate.now()) ? "TODAY" : d.equals(LocalDate.now().plusDays(1))
                    ? "TOMORROW" : d.format(DAY).toUpperCase();
            Label day = new Label(dayName);
            day.getStyleClass().add("small-title");
            FlowPane chips = new FlowPane(12, 12);
            for (Show s : entry.getValue()) {
                Button b = Theme.outline(s.getTime().format(TIME) + "   |   Theatrely " + (Session.city() == null ? "" : Session.city() + ", ") + s.getHall() + "   |   Rs." + s.getTicketPrice());
                b.setOnAction(e -> book(s));
                chips.getChildren().add(b);
            }
            box.getChildren().addAll(day, chips);
        }
        return box;
    }

    private void book(Show show) {
        if (Session.isAdmin()) {
            Dialogs.warn("Admins cannot book tickets. Login as a customer to book.");
            return;
        }
        if (SeatDialog.open(show, (Customer) Session.user())) {
            DashboardView.notifyUser("Booking confirmed!");
        }
    }
}
