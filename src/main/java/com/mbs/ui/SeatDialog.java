package com.mbs.ui;

import com.mbs.exception.BookingException;
import com.mbs.exception.SeatUnavailableException;
import com.mbs.model.Booking;
import com.mbs.model.Customer;
import com.mbs.model.Show;
import com.mbs.service.BookingService;
import com.mbs.util.AppConfig;
import com.mbs.util.FileUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/** Modal window with a seat grid. Returns true if a booking was made. */
public final class SeatDialog {
    private final Show show;
    private final Customer customer;
    private final Set<String> selected = new TreeSet<>();
    private final TextArea summary = new TextArea();
    private final Label message = new Label();
    private final Button confirm = new Button("Confirm booking");
    private int discount;
    private boolean booked = false;
    private Stage stage;

    private SeatDialog(Show show, Customer customer) {
        this.show = show;
        this.customer = customer;
    }

    public static boolean open(Show show, Customer customer) {
        SeatDialog d = new SeatDialog(show, customer);
        d.display();
        return d.booked;
    }

    private void display() {
        Set<String> taken;
        try {
            taken = Session.BOOKINGS.bookedSeats(show);
            discount = Session.BOOKINGS.discountPercent(customer);
        } catch (BookingException e) {
            Dialogs.error(e.getMessage());
            return;
        }

        Label screen = new Label("SCREEN THIS WAY");
        screen.getStyleClass().add("screen");
        screen.setMaxWidth(Double.MAX_VALUE);
        screen.setAlignment(Pos.CENTER);
        StackPane top = new StackPane(screen);
        top.setPadding(new Insets(10, 10, 14, 10));

        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setAlignment(Pos.TOP_CENTER);
        for (int r = 0; r < BookingService.ROWS.length; r++) {
            for (int c = 1; c <= BookingService.COLS; c++) {
                String seat = BookingService.ROWS[r] + String.valueOf(c);
                ToggleButton tb = new ToggleButton(seat);
                tb.getStyleClass().add("seat");
                if (taken.contains(seat)) {
                    tb.setDisable(true);
                    tb.setText("X");
                } else {
                    tb.selectedProperty().addListener((obs, was, now) -> onToggle(tb, seat, now));
                }
                grid.add(tb, c, r);
            }
        }

        Label heading = new Label(show.getMovieTitle());
        heading.getStyleClass().add("section-title");
        heading.setWrapText(true);
        Label when = new Label(show.toString());
        when.setWrapText(true);
        summary.setEditable(false);
        summary.setStyle("-fx-font-family: Consolas, Courier New, monospace;");
        summary.setPrefRowCount(8);
        summary.setText("Select your seats.");
        confirm.getStyleClass().add("solid-btn");
        confirm.setDisable(true);
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setOnAction(e -> onConfirm());
        Button cancel = new Button("Close");
        cancel.getStyleClass().add("outline-btn");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setOnAction(e -> stage.close());
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #c92a2a;");
        Label hint = new Label("Max " + AppConfig.getInt("max.seats.per.booking", 6)
                + " seats. X = already booked, black = yours.");
        hint.setWrapText(true);
        VBox right = new VBox(10, heading, when, hint, summary, message, confirm, cancel);
        right.setPadding(new Insets(0, 0, 0, 16));
        right.setPrefWidth(290);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(grid);
        root.setRight(right);
        root.setPadding(new Insets(14));

        stage = new Stage();
        stage.initOwner(Session.stage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Select seats");
        Scene scene = new Scene(root, 880, 470);
        scene.getStylesheets().addAll(Session.stage().getScene().getStylesheets());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void onToggle(ToggleButton tb, String seat, boolean now) {
        message.setText("");
        if (now) {
            if (selected.size() >= AppConfig.getInt("max.seats.per.booking", 6)) {
                tb.setSelected(false);
                message.setText("Maximum seats reached.");
                return;
            }
            selected.add(seat);
        } else {
            selected.remove(seat);
        }
        updateSummary();
    }

    private void updateSummary() {
        confirm.setDisable(selected.isEmpty());
        if (selected.isEmpty()) {
            summary.setText("Select your seats.");
            return;
        }
        summary.setText("Seats: " + String.join(", ", selected) + "\n\n"
                + Session.BOOKINGS.priceSummary(show, selected.size(), discount));
    }

    private void onConfirm() {
        try {
            Booking b = Session.BOOKINGS.bookSeats(customer, show, selected);
            booked = true;
            stage.close();
            askToSaveTicket(b);
        } catch (SeatUnavailableException e) {
            Dialogs.warn(e.getMessage());
            stage.close();                       // reopen to see the updated seats
        } catch (BookingException e) {
            message.setText(e.getMessage());
        }
    }

    private void askToSaveTicket(Booking b) {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Booking #" + b.getId() + " confirmed!\nSeats: " + b.getSeats() + "\nTotal: Rs." + b.getTotal()
                        + "\n\nSave the ticket as a text file?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText("Booking successful");
        if (a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("ticket_" + b.getId() + ".txt");
            File f = fc.showSaveDialog(Session.stage());
            if (f != null) {
                try {
                    FileUtil.writeReceipt(f, b, customer.getName());
                    DashboardView.notifyUser("Ticket saved to " + f.getName());
                } catch (IOException ex) {
                    Dialogs.error("Could not save ticket: " + ex.getMessage());
                }
            }
        }
    }
}
