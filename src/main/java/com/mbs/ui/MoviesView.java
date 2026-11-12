package com.mbs.ui;

import com.mbs.exception.BookingException;
import com.mbs.model.Movie;
import com.mbs.service.CatalogService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Home page: genre pills, sort/filter menu and a grid of movie cards (search comes from the header). */
public class MoviesView {
    private static final String ALL = "All";
    private static final double CARD_W = 196;

    private final FlowPane grid = new FlowPane(18, 22);
    private final FlowPane pills = new FlowPane(10, 10);
    private List<Movie> all = new ArrayList<>();
    private String genre = ALL;
    private String search = "";
    private String sort = CatalogService.SORT_YEAR;
    private boolean onlyTop = false;
    private boolean loading = true;

    /** Called by the header search box. */
    public void setSearch(String text) {
        search = text == null ? "" : text.trim().toLowerCase();
        if (!loading) {
            refresh();
        }
    }

    public Parent build() {
        try {
            all = Session.CATALOG.allMovies();
        } catch (BookingException e) {
            Dialogs.error(e.getMessage());
        }

        String city = Session.city();
        Label heading = new Label(city == null ? "NOW SHOWING" : "NOW SHOWING IN " + city.toUpperCase());
        heading.getStyleClass().add("page-title");
        Label sub = new Label(all.size() + " movies: all-time favourites and recent releases");
        sub.getStyleClass().add("mono");
        VBox titleBox = new VBox(0, heading, sub);

        buildPills();
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox pillRow = new HBox(12, pills, grow, filterMenu());
        pillRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(pills, Priority.ALWAYS);

        VBox body = new VBox(18, titleBox, pillRow, grid);
        body.setPadding(new Insets(10, 32, 40, 32));

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-scroll");
        pills.prefWrapLengthProperty().bind(scroll.widthProperty().subtract(150));
        grid.prefWrapLengthProperty().bind(scroll.widthProperty().subtract(90));
        loading = false;
        refresh();
        return scroll;
    }

    // ---------------------------------------------------------------- sort / filter menu
    private Node filterMenu() {
        MenuButton menu = new MenuButton();
        menu.setGraphic(Theme.icon(Theme.ICON_TUNE, 22, "#111"));
        menu.getStyleClass().add("filter-btn");
        ToggleGroup group = new ToggleGroup();
        String[] options = {CatalogService.SORT_YEAR, CatalogService.SORT_RATING,
                CatalogService.SORT_DURATION, CatalogService.SORT_TITLE};
        for (String o : options) {
            RadioMenuItem item = new RadioMenuItem(o);
            item.setToggleGroup(group);
            item.setSelected(o.equals(sort));
            item.setOnAction(e -> {                 // event handler
                sort = o;
                refresh();
            });
            menu.getItems().add(item);
        }
        CheckMenuItem top = new CheckMenuItem("Rating 8.5+ only");
        top.setSelected(onlyTop);
        top.setOnAction(e -> {
            onlyTop = top.isSelected();
            refresh();
        });
        menu.getItems().addAll(new SeparatorMenuItem(), top);
        return menu;
    }

    // ---------------------------------------------------------------- genre pills
    private void buildPills() {
        pills.getChildren().clear();
        List<String> names = new ArrayList<>();
        names.add(ALL);
        names.addAll(Session.CATALOG.genres(all));
        for (String g : names) {
            Button pill = new Button(g);
            pill.getStyleClass().add("pill-btn");
            if (g.equals(genre)) {
                pill.getStyleClass().add("pill-selected");
            }
            pill.setOnAction(e -> {
                genre = g;
                buildPills();
                refresh();
            });
            pills.getChildren().add(pill);
        }
    }

    // ---------------------------------------------------------------- grid
    private void refresh() {
        if (loading) {
            return;
        }
        List<Movie> result = CatalogService.filter(all, m ->
                m.getTitle().toLowerCase().contains(search)
                && (ALL.equals(genre) || m.getGenre().equals(genre))
                && (!onlyTop || m.getRating() >= 8.5));
        CatalogService.sortMovies(result, sort);
        grid.getChildren().clear();
        for (Movie m : result) {
            grid.getChildren().add(movieCard(m));
        }
        if (result.isEmpty()) {
            Label none = new Label("No movies match your search.");
            none.getStyleClass().add("mono");
            grid.getChildren().add(none);
        }
        DashboardView.notifyUser(result.size() + " movie(s) found");
    }

    private Node movieCard(Movie m) {
        Node poster = Theme.poster(m, CARD_W, 232);
        Label title = new Label(m.getTitle());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        title.setMinHeight(38);
        title.setMaxWidth(CARD_W - 24);
        title.setAlignment(Pos.TOP_LEFT);

        FlowPane tags = new FlowPane(5, 5);
        tags.getChildren().add(pill(m.getGenre()));
        tags.getChildren().add(pill(m.getDuration() + " min"));
        if (m.getYear() > 0) {
            tags.getChildren().add(pill(String.valueOf(m.getYear())));
        }

        Button book = new Button("Book Tickets  \u2192");
        book.getStyleClass().add("book-btn");
        book.setMaxWidth(Double.MAX_VALUE);
        book.setOnAction(e -> DashboardView.showPage(new MovieDetailView(m).build()));

        VBox info = new VBox(8, title, tags, book);
        info.setPadding(new Insets(12, 12, 14, 12));
        VBox card = new VBox(poster, info);
        card.getStyleClass().add("movie-card");
        card.setMinWidth(CARD_W);
        card.setPrefWidth(CARD_W);
        card.setMaxWidth(CARD_W);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> DashboardView.showPage(new MovieDetailView(m).build()));
        card.setOnMouseEntered(e -> card.setTranslateY(-4));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    private Label pill(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("pill");
        return l;
    }
}
