package com.mbs.ui;

import com.mbs.exception.BookingException;
import com.mbs.model.Movie;
import com.mbs.util.AppConfig;
import com.mbs.util.FileUtil;
import com.mbs.util.Validator;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;

/** Admin only: add/delete movies, add shows (with validation), view log file. */
public class AdminView {
    private final TableView<Movie> table = new TableView<>();
    private final ComboBox<Movie> movieBox = new ComboBox<>();
    private final TextArea logArea = new TextArea();
    private String posterPath;

    public Parent build() {
        // ---- add movie form
        Label h1 = new Label("Add movie");
        h1.getStyleClass().add("section-title");
        TextField title = new TextField();
        TextField genre = new TextField();
        TextField duration = new TextField();
        TextField rating = new TextField();
        TextField director = new TextField();
        TextField year = new TextField(String.valueOf(java.time.LocalDate.now().getYear()));
        TextField trailerUrl = new TextField();
        trailerUrl.setPromptText("YouTube link (optional)");
        TextArea synopsis = new TextArea();
        synopsis.setPrefRowCount(2);
        synopsis.setWrapText(true);
        TextField cast = new TextField();
        cast.setPromptText("Name (Role); Name (Role); ...");
        Label posterName = new Label("No poster chosen");
        Button choose = new Button("Choose poster...");
        choose.getStyleClass().add("outline-btn");
        choose.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File f = fc.showOpenDialog(Session.stage());
            if (f != null) {
                try {
                    posterPath = FileUtil.copyPoster(f);      // byte stream copy
                    posterName.setText(f.getName());
                } catch (IOException ex) {
                    Dialogs.error("Could not copy poster: " + ex.getMessage());
                }
            }
        });
        Button addMovie = new Button("Add movie");
        addMovie.getStyleClass().add("primary-btn");
        addMovie.setOnAction(e -> {
            try {
                String t = Validator.required(title.getText(), "Title");
                String g = Validator.required(genre.getText(), "Genre");
                int d = Validator.positiveInt(duration.getText(), "Duration");
                double r = Validator.decimal(rating.getText(), "Rating", 0, 10);
                Session.CATALOG.addMovie(t, g, d, r, posterPath, synopsis.getText().trim(),
                        director.getText().trim(), cast.getText().trim(), trailerUrl.getText().trim(),
                        Validator.year(year.getText()));
                title.clear();
                genre.clear();
                duration.clear();
                rating.clear();
                director.clear();
                trailerUrl.clear();
                synopsis.clear();
                cast.clear();
                posterPath = null;
                posterName.setText("No poster chosen");
                reload();
                DashboardView.notifyUser("Movie added: " + t);
            } catch (BookingException ex) {
                Dialogs.error(ex.getMessage());
            }
        });
        GridPane f1 = new GridPane();
        f1.setHgap(10);
        f1.setVgap(8);
        f1.addRow(0, new Label("Title"), title, new Label("Genre"), genre);
        f1.addRow(1, new Label("Duration (min)"), duration, new Label("Rating (0-10)"), rating);
        f1.addRow(2, new Label("Director"), director, new Label("Trailer link"), trailerUrl);
        f1.addRow(3, new Label("Release year"), year);
        f1.addRow(4, new Label("Story"), synopsis);
        f1.addRow(5, new Label("Cast"), cast);
        GridPane.setColumnSpan(synopsis, 3);
        GridPane.setColumnSpan(cast, 3);
        f1.addRow(6, choose, posterName, addMovie);

        // ---- add show form
        Label h2 = new Label("Add show");
        h2.getStyleClass().add("section-title");
        DatePicker date = new DatePicker();
        TextField time = new TextField("18:30");
        TextField hall = new TextField("Hall 1");
        TextField price = new TextField("250");
        Button addShow = new Button("Add show");
        addShow.getStyleClass().add("primary-btn");
        addShow.setOnAction(e -> {
            try {
                LocalTime t = Validator.time(time.getText());
                String hl = Validator.required(hall.getText(), "Hall");
                double p = Validator.decimal(price.getText(), "Price", 1, 5000);
                Session.CATALOG.addShow(movieBox.getValue(), date.getValue(), t, hl, p);
                DashboardView.notifyUser("Show added for " + movieBox.getValue());
                Dialogs.info("Done", "Show added successfully.");
            } catch (BookingException ex) {
                Dialogs.error(ex.getMessage());
            }
        });
        GridPane f2 = new GridPane();
        f2.setHgap(10);
        f2.setVgap(8);
        f2.addRow(0, new Label("Movie"), movieBox, new Label("Date"), date);
        f2.addRow(1, new Label("Time (HH:mm)"), time, new Label("Hall"), hall);
        f2.addRow(2, new Label("Ticket price"), price, addShow);

        // ---- movies table + delete
        Label h3 = new Label("All movies");
        h3.getStyleClass().add("section-title");
        TableColumn<Movie, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        TableColumn<Movie, String> cTitle = new TableColumn<>("Title");
        cTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        TableColumn<Movie, String> cGenre = new TableColumn<>("Genre");
        cGenre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGenre()));
        TableColumn<Movie, Number> cRate = new TableColumn<>("Rating");
        cRate.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getRating()));
        table.getColumns().add(cId);
        table.getColumns().add(cTitle);
        table.getColumns().add(cGenre);
        table.getColumns().add(cRate);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(180);
        Button delete = new Button("Delete selected movie");
        delete.getStyleClass().add("danger-btn");
        delete.setOnAction(e -> {
            Movie m = table.getSelectionModel().getSelectedItem();
            if (m == null) {
                Dialogs.warn("Select a movie first.");
            } else if (Dialogs.confirm("Delete '" + m.getTitle() + "' with all its shows and bookings?")) {
                try {
                    Session.CATALOG.deleteMovie(m);
                    reload();
                } catch (BookingException ex) {
                    Dialogs.error(ex.getMessage());
                }
            }
        });

        // ---- log viewer
        Label h4 = new Label("Application log (last 15 lines)");
        h4.getStyleClass().add("section-title");
        logArea.setEditable(false);
        logArea.setPrefRowCount(7);
        Button refreshLog = new Button("Refresh log");
        refreshLog.getStyleClass().add("outline-btn");
        refreshLog.setOnAction(e -> loadLog());

        VBox root = new VBox(10, h1, f1, h2, f2, h3, table, delete, h4, logArea, refreshLog);
        root.setPadding(new Insets(24, 36, 24, 36));
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        reload();
        loadLog();
        return scroll;
    }

    private void reload() {
        try {
            table.setItems(FXCollections.observableArrayList(Session.CATALOG.allMovies()));
            movieBox.setItems(FXCollections.observableArrayList(Session.CATALOG.allMovies()));
        } catch (BookingException e) {
            Dialogs.error(e.getMessage());
        }
    }

    private void loadLog() {
        File f = new File(AppConfig.get("log.file", "logs/app.log"));
        try {
            logArea.setText(f.exists() ? FileUtil.readLastLines(f, 15) : "No log file yet.");
        } catch (IOException e) {
            logArea.setText("Could not read log: " + e.getMessage());
        }
    }
}
