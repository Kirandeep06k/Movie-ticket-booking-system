package com.mbs.ui;

import com.mbs.exception.BookingException;
import com.mbs.model.Person;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Wide desktop screen: pastel hero on the left, Login / Register form on the right. */
public class LoginView {
    private final TextField name = new TextField();
    private final TextField email = new TextField();
    private final PasswordField password = new PasswordField();
    private final Button loginTab = new Button("LOGIN");
    private final Button registerTab = new Button("REGISTER");
    private final Button submit = Theme.solid("");
    private final Label heading = new Label();
    private final Label status = new Label();
    private final VBox nameBox;
    private boolean registerMode = false;

    public LoginView() {
        name.setPromptText("Full name");
        email.setPromptText("you@example.com");
        password.setPromptText("At least 6 characters");
        nameBox = new VBox(6, fieldLabel("NAME"), name);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("mono");
        return l;
    }

    public Parent build() {
        // ---------- left: hero
        Label brand = new Label("THEATRELY");
        brand.getStyleClass().add("logo");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 40px;");
        Label headline = new Label("YOUR SEAT.\nYOUR SHOW.");
        headline.getStyleClass().add("login-headline");
        headline.setStyle("-fx-text-fill: white;");
        Label sub = new Label("Pick a movie, pick your exact seat and walk in like you own the place.");
        sub.getStyleClass().add("mono");
        sub.setStyle("-fx-text-fill: #d1d5db; -fx-font-size: 14px;");
        sub.setWrapText(true);
        sub.setMaxWidth(400);
        VBox leftBox = new VBox(22, brand, headline, sub);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setPadding(new Insets(0, 40, 0, 70));
        StackPane left = new StackPane(leftBox);
        left.setStyle("-fx-background-color: #111111;");
        StackPane.setAlignment(leftBox, Pos.CENTER_LEFT);

        // ---------- right: form
        loginTab.getStyleClass().add("login-tab");
        registerTab.getStyleClass().add("login-tab");
        loginTab.setOnAction(e -> setMode(false));
        registerTab.setOnAction(e -> setMode(true));
        HBox tabs = new HBox(6, loginTab, registerTab);

        heading.getStyleClass().add("section-title");
        status.setWrapText(true);
        status.setStyle("-fx-text-fill: #c92a2a;");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setPrefHeight(42);
        submit.setDefaultButton(true);
        submit.setOnAction(e -> onSubmit());

        VBox form = new VBox(16, tabs, heading, nameBox,
                new VBox(6, fieldLabel("EMAIL"), email),
                new VBox(6, fieldLabel("PASSWORD"), password),
                submit, status);
        form.setMaxWidth(400);
        form.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane right = new StackPane(form);
        right.setPadding(new Insets(30));
        right.setStyle("-fx-background-color: white;");

        HBox root = new HBox(left, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        left.prefWidthProperty().bind(root.widthProperty().multiply(0.50));
        setMode(false);
        return root;
    }

    private void setMode(boolean register) {
        registerMode = register;
        status.setText("");
        nameBox.setVisible(register);
        nameBox.setManaged(register);
        heading.setText(register ? "CREATE YOUR ACCOUNT" : "WELCOME BACK");
        submit.setText(register ? "CREATE ACCOUNT" : "LOGIN");
        loginTab.getStyleClass().remove("login-tab-active");
        registerTab.getStyleClass().remove("login-tab-active");
        (register ? registerTab : loginTab).getStyleClass().add("login-tab-active");
    }

    private void onSubmit() {
        try {
            Person p = registerMode
                    ? Session.USERS.register(name.getText(), email.getText(), password.getText())
                    : Session.USERS.login(email.getText(), password.getText());
            Session.setUser(p);
            String[] saved = Session.USERS.loadLocation(p);
            Session.setLocation(saved[0], saved[1]);
            Session.setRoot(new DashboardView().build());
            if (Session.city() == null) {
                javafx.application.Platform.runLater(() -> {      // first login: choose state + city
                    if (LocationDialog.open(true)) {
                        DashboardView.refreshLocation();
                    }
                });
            }
        } catch (BookingException ex) {
            status.setText(ex.getMessage());
        }
    }
}
