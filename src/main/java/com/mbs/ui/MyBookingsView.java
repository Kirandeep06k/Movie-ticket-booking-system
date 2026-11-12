package com.mbs.ui;

import com.mbs.exception.BookingException;
import com.mbs.model.Booking;
import com.mbs.service.BookingService;
import com.mbs.util.FileUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Booking history, cancel, export (text/CSV) and serialization backup/restore. */
public class MyBookingsView {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");
    private final TableView<Booking> table = new TableView<>();
    private final ListView<String> recent = new ListView<>();

    public Parent build() {
        TableColumn<Booking, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        cId.setPrefWidth(50);
        TableColumn<Booking, String> cMovie = new TableColumn<>("Movie");
        cMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMovieTitle()));
        cMovie.setPrefWidth(190);
        TableColumn<Booking, String> cTime = new TableColumn<>("Show time");
        cTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getShowTime().format(FMT)));
        cTime.setPrefWidth(130);
        TableColumn<Booking, String> cSeats = new TableColumn<>("Seats");
        cSeats.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSeats()));
        cSeats.setPrefWidth(120);
        TableColumn<Booking, Number> cTotal = new TableColumn<>("Total (Rs.)");
        cTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotal()));
        TableColumn<Booking, String> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        table.getColumns().add(cId);
        table.getColumns().add(cMovie);
        table.getColumns().add(cTime);
        table.getColumns().add(cSeats);
        table.getColumns().add(cTotal);
        table.getColumns().add(cStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        Button cancel = new Button("Cancel booking");
        cancel.getStyleClass().add("danger-btn");
        cancel.setOnAction(e -> onCancel());
        Button receipt = new Button("Save ticket (.txt)");
        receipt.getStyleClass().add("outline-btn");
        receipt.setOnAction(e -> onReceipt());
        Button csv = new Button("Export CSV");
        csv.getStyleClass().add("outline-btn");
        csv.setOnAction(e -> onCsv());
        Button backup = new Button("Backup (serialize)");
        backup.getStyleClass().add("outline-btn");
        backup.setOnAction(e -> onBackup());
        Button restore = new Button("View backup file");
        restore.getStyleClass().add("outline-btn");
        restore.setOnAction(e -> onRestore());
        HBox buttons = new HBox(8, cancel, receipt, csv, backup, restore);

        Label recentLabel = new Label("Recent activity");
        recentLabel.getStyleClass().add("section-title");
        recent.setPrefHeight(110);

        VBox root = new VBox(10, table, buttons, recentLabel, recent);
        root.setPadding(new Insets(24, 36, 24, 36));
        load();
        return root;
    }

    private void load() {
        try {
            List<Booking> list = Session.isAdmin()
                    ? Session.BOOKINGS.allBookings()
                    : Session.BOOKINGS.bookingsOf((com.mbs.model.Customer) Session.user());
            table.setItems(FXCollections.observableArrayList(list));
            recent.setItems(FXCollections.observableArrayList(BookingService.recentActivity()));
            DashboardView.notifyUser(list.size() + " booking(s)");
        } catch (BookingException e) {
            Dialogs.error(e.getMessage());
        }
    }

    private Booking selected() {
        Booking b = table.getSelectionModel().getSelectedItem();
        if (b == null) {
            Dialogs.warn("Select a booking in the table first.");
        }
        return b;
    }

    private void onCancel() {
        Booking b = selected();
        if (b == null || !Dialogs.confirm("Cancel booking #" + b.getId() + " for " + b.getMovieTitle() + "?")) {
            return;
        }
        try {
            Session.BOOKINGS.cancelBooking(b);
            load();
            DashboardView.notifyUser("Booking #" + b.getId() + " cancelled");
        } catch (BookingException e) {
            Dialogs.error(e.getMessage());
        }
    }

    private void onReceipt() {
        Booking b = selected();
        if (b == null) {
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("ticket_" + b.getId() + ".txt");
        File f = fc.showSaveDialog(Session.stage());
        if (f != null) {
            try {
                FileUtil.writeReceipt(f, b, Session.user().getName());
                DashboardView.notifyUser("Saved " + f.getName());
            } catch (IOException e) {
                Dialogs.error("Could not write file: " + e.getMessage());
            }
        }
    }

    private void onCsv() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("bookings.csv");
        File f = fc.showSaveDialog(Session.stage());
        if (f != null) {
            try {
                FileUtil.exportCsv(f, table.getItems());
                DashboardView.notifyUser("Exported " + table.getItems().size() + " bookings to " + f.getName());
            } catch (IOException e) {
                Dialogs.error("Could not write file: " + e.getMessage());
            }
        }
    }

    private void onBackup() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("bookings.ser");
        File f = fc.showSaveDialog(Session.stage());
        if (f != null) {
            try {
                FileUtil.backup(f, table.getItems());
                DashboardView.notifyUser("Backup saved: " + f.getName());
            } catch (IOException e) {
                Dialogs.error("Backup failed: " + e.getMessage());
            }
        }
    }

    private void onRestore() {
        File f = new FileChooser().showOpenDialog(Session.stage());
        if (f == null) {
            return;
        }
        try {
            List<Booking> list = FileUtil.restore(f);
            StringBuilder sb = new StringBuilder();
            for (Booking b : list) {
                sb.append(b).append('\n');
            }
            TextArea area = new TextArea(sb.toString());
            area.setEditable(false);
            Alert a = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            a.setHeaderText("Backup contains " + list.size() + " booking(s)");
            a.getDialogPane().setContent(area);
            a.showAndWait();
        } catch (IOException | ClassNotFoundException e) {
            Dialogs.error("Could not read backup: " + e.getMessage());
        }
    }
}
