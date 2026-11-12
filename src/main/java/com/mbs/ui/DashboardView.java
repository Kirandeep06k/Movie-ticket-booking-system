package com.mbs.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/** App shell: header (logo, search, location, profile menu), page area (StackPane), status line. */
public class DashboardView {
    private static Label statusBar;
    private static StackPane page;
    private static MoviesView currentMovies;
    private static boolean homeVisible;
    private static Label locationText;
    private static TextField searchField;

    public static void notifyUser(String message) {
        if (statusBar != null) {
            statusBar.setText(message);
        }
    }

    /** Swap the page shown in the middle of the window (any page except the home grid). */
    public static void showPage(Node node) {
        if (page != null) {
            homeVisible = false;
            page.getChildren().setAll(node);
        }
    }

    /** Show the movie grid (re-created so heading, location and search are fresh). */
    public static void showHome() {
        if (page != null) {
            currentMovies = new MoviesView();
            currentMovies.setSearch(searchField == null ? "" : searchField.getText());
            page.getChildren().setAll(currentMovies.build());
            homeVisible = true;
        }
    }

    public static void refreshLocation() {
        if (locationText != null) {
            locationText.setText(Session.cityLabel());
        }
        if (homeVisible) {
            showHome();
        }
    }

    public Parent build() {
        // ---- logo
        Region square = new Region();
        square.setPrefSize(36, 36);
        square.setMinSize(36, 36);
        square.setStyle("-fx-background-color: #111; -fx-background-radius: 10;");
        StackPane mark = new StackPane(square, Theme.icon(Theme.ICON_PLAY, 18, "#ffffff"));
        Label logoText = new Label("Theatrely");
        logoText.getStyleClass().add("logo-text");
        HBox logo = new HBox(10, mark, logoText);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setCursor(Cursor.HAND);
        logo.setOnMouseClicked(e -> showHome());

        // ---- search (filters the home grid while you type)
        searchField = new TextField();
        searchField.setPromptText("Search for movies...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            if (!homeVisible) {
                showHome();
            }
            currentMovies.setSearch(newV);
        });
        Region searchIcon = Theme.icon(Theme.ICON_SEARCH, 20, "#6B7280");
        StackPane search = new StackPane(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 18));
        search.setPrefWidth(520);
        search.setMaxWidth(560);

        // ---- location
        locationText = new Label(Session.cityLabel());
        HBox locContent = new HBox(6, Theme.icon(Theme.ICON_PIN, 20, "#111"), locationText,
                Theme.icon(Theme.ICON_CHEVRON, 18, "#111"));
        locContent.setAlignment(Pos.CENTER);
        Button location = new Button();
        location.setGraphic(locContent);
        location.getStyleClass().add("loc-btn");
        location.setOnAction(e -> {
            if (LocationDialog.open(false)) {
                refreshLocation();
            }
        });

        // ---- profile menu
        MenuButton profile = new MenuButton();
        profile.setGraphic(Theme.icon(Theme.ICON_PERSON, 22, "#111"));
        profile.getStyleClass().add("profile-btn");
        MenuItem who = new MenuItem(Session.user().getName());
        who.setDisable(true);
        MenuItem bookings = new MenuItem(Session.isAdmin() ? "All bookings" : "My bookings");
        bookings.setOnAction(e -> showPage(new MyBookingsView().build()));
        MenuItem changeLoc = new MenuItem("Change location");
        changeLoc.setOnAction(e -> {
            if (LocationDialog.open(false)) {
                refreshLocation();
            }
        });
        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(e -> {
            Session.setUser(null);
            Session.setLocation(null, null);
            Session.setRoot(new LoginView().build());
        });
        profile.getItems().addAll(who, new SeparatorMenuItem(), bookings);
        if (Session.isAdmin()) {
            MenuItem admin = new MenuItem("Admin panel");
            admin.setOnAction(e -> showPage(new AdminView().build()));
            profile.getItems().add(admin);
        }
        profile.getItems().addAll(changeLoc, new SeparatorMenuItem(), logout);

        Region left = new Region();
        Region right = new Region();
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        HBox header = new HBox(18, logo, left, search, right, location, profile);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("header");

        statusBar = new Label("Welcome, " + Session.user().getName() + ".");
        statusBar.getStyleClass().add("status");

        page = new StackPane();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(header);
        root.setCenter(page);
        root.setBottom(statusBar);
        showHome();
        return root;
    }
}
