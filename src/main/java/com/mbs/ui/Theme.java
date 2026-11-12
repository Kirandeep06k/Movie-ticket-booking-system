package com.mbs.ui;

import com.mbs.model.Movie;
import com.mbs.util.PosterLocator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.File;

/** Colors, icons and small UI builders shared by all screens. Palette: black, white, greys, yellow star. */
public final class Theme {
    public static final String INK = "#111111";
    public static final String MUTED = "#6B7280";
    public static final String STAR = "#FFC107";

    // Material-style icon outlines (24 x 24)
    public static final String ICON_SEARCH = "M15.5 14h-.79l-.28-.27A6.47 6.47 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";
    public static final String ICON_PIN = "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 0 1 0-5 2.5 2.5 0 0 1 0 5z";
    public static final String ICON_PERSON = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    public static final String ICON_STAR = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";
    public static final String ICON_CHEVRON = "M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6z";
    public static final String ICON_PLAY = "M8 5v14l11-7z";
    public static final String ICON_TUNE = "M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 14v2h8v-2h-8zM7 9v2H3v2h4v2h2V9H7zm14 4v-2H11v2h10zm-6-4h2V7h4V5h-4V3h-2v6z";

    private Theme() { }

    /** Loads the bundled heading font (Bebas Neue). Call once before the first scene is created. */
    public static void loadFonts() {
        try (java.io.InputStream in = Theme.class.getResourceAsStream("/fonts/BebasNeue-Regular.ttf")) {
            if (in != null) {
                Font.loadFont(in, 12);
            }
        } catch (java.io.IOException e) {
            // heading font is optional: the stylesheet falls back to Georgia
        }
    }

    public static Region icon(String svg, double size, String color) {
        Region r = new Region();
        SVGPath path = new SVGPath();
        path.setContent(svg);
        r.setShape(path);
        r.setMinSize(size, size);
        r.setPrefSize(size, size);
        r.setMaxSize(size, size);
        r.setStyle("-fx-background-color: " + color + ";");
        return r;
    }

    public static Button outline(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("outline-btn");
        return b;
    }

    public static Button solid(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("solid-btn");
        return b;
    }

    /** Movie poster (from the posters folder) with a rating badge. Dark placeholder if no file was added yet. */
    public static StackPane poster(Movie m, double w, double h) {
        StackPane box = new StackPane();
        box.setPrefSize(w, h);
        box.setMinSize(w, h);
        box.setMaxSize(w, h);
        ImageView img = coverImage(PosterLocator.find(m), w, h);
        if (img != null) {
            box.getChildren().add(img);
        } else {
            box.setStyle("-fx-background-color: linear-gradient(to bottom right, #2b2b2b, #0d0d0d);");
            Label title = new Label(m.getTitle().toUpperCase());
            title.getStyleClass().add("poster-title");
            title.setStyle("-fx-font-size: " + (int) Math.max(18, w / 8) + "px; -fx-text-fill: white;");
            title.setWrapText(true);
            title.setMaxWidth(w - 30);
            title.setAlignment(Pos.CENTER);
            title.setTextAlignment(TextAlignment.CENTER);
            Label hint = new Label("poster not added yet");
            hint.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");
            VBox center = new VBox(6, title, hint);
            center.setAlignment(Pos.CENTER);
            box.getChildren().add(center);
        }
        HBox badge = new HBox(5, icon(ICON_STAR, 14, STAR), ratingLabel(m));
        badge.setAlignment(Pos.CENTER);
        badge.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        badge.setStyle("-fx-background-color: rgba(0,0,0,0.62); -fx-background-radius: 14; -fx-padding: 4 11 4 9;");
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        StackPane.setMargin(badge, new Insets(10));
        box.getChildren().add(badge);
        Rectangle clip = new Rectangle(w, h + 40);      // rounded top corners only
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        box.setClip(clip);
        return box;
    }

    private static Label ratingLabel(Movie m) {
        Label l = new Label(String.valueOf(m.getRating()));
        l.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        return l;
    }

    /** Fills the w x h area with the picture (center-cropped). Returns null if there is no usable file. */
    private static ImageView coverImage(File file, double w, double h) {
        if (file == null) {
            return null;
        }
        Image img = new Image(file.toURI().toString(), w * 2, 0, true, true);
        if (img.isError() || img.getWidth() <= 0 || img.getHeight() <= 0) {
            return null;
        }
        ImageView view = new ImageView(img);
        view.setFitWidth(w);
        view.setFitHeight(h);
        view.setPreserveRatio(false);
        double scale = Math.max(w / img.getWidth(), h / img.getHeight());
        double vw = w / scale;
        double vh = h / scale;
        view.setViewport(new Rectangle2D((img.getWidth() - vw) / 2, (img.getHeight() - vh) / 2, vw, vh));
        return view;
    }
}
