package com.mbs.ui;

import com.mbs.model.Customer;
import com.mbs.util.IndiaLocations;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Pick state, then city (India). Mandatory right after the first login. */
public final class LocationDialog {
    private boolean chosen = false;

    private LocationDialog() { }

    /** Returns true if the user confirmed a location. */
    public static boolean open(boolean mandatory) {
        LocationDialog d = new LocationDialog();
        d.show(mandatory);
        return d.chosen;
    }

    private void show(boolean mandatory) {
        Label title = new Label("WHERE ARE YOU WATCHING?");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Pick your state and city in India. You can change it any time from the top bar.");
        sub.getStyleClass().add("mono");
        sub.setWrapText(true);

        ComboBox<String> states = new ComboBox<>();
        states.getItems().addAll(IndiaLocations.states());
        states.setPromptText("Select state");
        states.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> cities = new ComboBox<>();
        cities.setPromptText("Select city");
        cities.setMaxWidth(Double.MAX_VALUE);
        cities.setDisable(true);
        CheckBox remember = new CheckBox("Remember my location");
        remember.setSelected(true);

        Button go = Theme.solid("CONTINUE");
        go.setDisable(true);
        Button cancel = Theme.outline("CANCEL");
        cancel.setVisible(!mandatory);
        cancel.setManaged(!mandatory);

        states.valueProperty().addListener((obs, oldV, newV) -> {       // listener: city depends on state
            cities.getItems().setAll(IndiaLocations.cities(newV));
            cities.setValue(null);
            cities.setDisable(newV == null);
            go.setDisable(true);
        });
        cities.valueProperty().addListener((obs, oldV, newV) -> go.setDisable(newV == null));
        if (Session.state() != null) {
            states.setValue(Session.state());
            cities.setValue(Session.city());
        }

        Stage stage = new Stage();
        go.setOnAction(e -> {
            Session.setLocation(states.getValue(), cities.getValue());
            if (remember.isSelected() && Session.user() instanceof Customer) {
                Session.USERS.saveLocation(Session.user(), states.getValue(), cities.getValue());
            }
            chosen = true;
            stage.close();
        });
        cancel.setOnAction(e -> stage.close());
        HBox buttons = new HBox(10, go, cancel);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(14, title, sub, new Label("State"), states, new Label("City"), cities, remember, buttons);
        root.setPadding(new Insets(26));
        root.setStyle("-fx-background-color: #FAF7F5;");

        stage.initOwner(Session.stage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Choose your location");
        stage.setOnCloseRequest(e -> {
            if (mandatory && !chosen) {
                e.consume();                                              // must pick a city first
            }
        });
        Scene scene = new Scene(root, 440, 440);
        scene.getStylesheets().addAll(Session.stage().getScene().getStylesheets());
        stage.setScene(scene);
        stage.showAndWait();
    }
}
