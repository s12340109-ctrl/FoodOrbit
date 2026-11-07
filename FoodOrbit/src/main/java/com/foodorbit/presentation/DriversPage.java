package com.foodorbit.presentation;

import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.driver.DriverStatus;
import com.foodorbit.domain.driver.BicycleDriver;
import com.foodorbit.domain.driver.CarDriver;
import com.foodorbit.domain.driver.ElectricVehicleDriver;
import com.foodorbit.domain.driver.MotorcycleDriver;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DriversPage extends VBox {

    private final TableView<Driver> table = new TableView<>();

    public DriversPage() {

        setSpacing(18);
        setPadding(new Insets(15));

        Label title = new Label("Drivers Management");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label(
                "Manage drivers, locations and delivery assignments");
        subtitle.getStyleClass().add("page-subtitle");

        TextField searchField = new TextField();
        searchField.setPromptText("Search drivers...");
        searchField.setPrefWidth(230);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
                "All", "Available", "Busy", "Offline");
        statusFilter.setValue("All");

        Button addButton = new Button("+ Add Driver");
        addButton.setStyle(
                "-fx-background-color: #176b50;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 18;" +
                "-fx-background-radius: 9;");

        HBox toolbar = new HBox(
                12, searchField, statusFilter, addButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TableColumn<Driver, String> idCol =
                column("Driver ID",
                        d -> String.valueOf(d.getDriverId()));

        TableColumn<Driver, String> nameCol =
                column("Name", Driver::getName);

        TableColumn<Driver, String> phoneCol =
                column("Phone", Driver::getPhoneNumber);

        TableColumn<Driver, String> typeCol =
                column("Vehicle Type", this::vehicleType);

        TableColumn<Driver, String> locationCol =
                column("Location", d -> String.format(
                        "(%.1f, %.1f)",
                        d.getCurrentX(),
                        d.getCurrentY()));

        TableColumn<Driver, String> statusCol =
                column("Status", d -> statusText(d.getStatus()));

        // Battery column
        TableColumn<Driver, String> batteryCol =
                column("Battery", this::batteryText);

        TableColumn<Driver, String> deliveriesCol =
                column("Deliveries",
                        d -> String.valueOf(
                                d.getCompletedDeliveries()));

        table.getColumns().addAll(
                idCol,
                nameCol,
                phoneCol,
                typeCol,
                locationCol,
                statusCol,
                batteryCol,
                deliveriesCol
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(new Label("No drivers found"));
        table.setPrefHeight(410);
        VBox.setVgrow(table, Priority.ALWAYS);

        FilteredList<Driver> filtered =
                new FilteredList<>(
                        AppStore.get().drivers(),
                        driver -> true
                );

        table.setItems(filtered);

        Runnable updateFilter = () -> {

            String query = searchField.getText()
                    .trim()
                    .toLowerCase();

            String selectedStatus = statusFilter.getValue();

            filtered.setPredicate(driver -> {

                boolean matchesSearch =
                        String.valueOf(driver.getDriverId())
                                .contains(query)
                        || driver.getName().toLowerCase()
                                .contains(query)
                        || driver.getPhoneNumber()
                                .contains(query);

                boolean matchesStatus =
                        selectedStatus.equals("All")
                        || statusText(driver.getStatus())
                                .equals(selectedStatus);

                return matchesSearch && matchesStatus;
            });
        };

        searchField.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        updateFilter.run());

        statusFilter.valueProperty().addListener(
                (obs, oldValue, newValue) ->
                        updateFilter.run());

        Button availableButton =
                new Button("Set Available");

        Button busyButton =
                new Button("Set Busy");

        Button offlineButton =
                new Button("Set Offline");

        Button deleteButton =
                new Button("Delete Driver");

        availableButton.setOnAction(
                e -> changeStatus(DriverStatus.AVAILABLE));

        busyButton.setOnAction(
                e -> changeStatus(DriverStatus.BUSY));

        offlineButton.setOnAction(
                e -> changeStatus(DriverStatus.OFFLINE));

        deleteButton.setOnAction(e -> {

            Driver selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showMessage("Please select a driver.");
                return;
            }

            try {
                AppStore.get().removeDriver(selected);
                table.refresh();
            } catch (IllegalStateException ex) {
                showMessage(ex.getMessage());
            }
        });

        addButton.setOnAction(e -> addDriver());

        HBox actions = new HBox(
                10,
                availableButton,
                busyButton,
                offlineButton,
                deleteButton
        );

        actions.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(
                title,
                subtitle,
                toolbar,
                table,
                actions
        );
    }

    private TableColumn<Driver, String> column(
            String title,
            java.util.function.Function<Driver, String> getter) {

        TableColumn<Driver, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        getter.apply(data.getValue())));

        return column;
    }

    private String vehicleType(Driver driver) {

        if (driver instanceof BicycleDriver) {
            return "Bicycle";
        }
        if (driver instanceof MotorcycleDriver) {
            return "Motorcycle";
        }
        if (driver instanceof CarDriver) {
            return "Car";
        }
        if (driver instanceof ElectricVehicleDriver) {
            return "Electric Vehicle";
        }
        return "Other";
    }

    // Display battery level for electric vehicle drivers
    private String batteryText(Driver driver) {

        if (driver instanceof ElectricVehicleDriver) {

            return String.format(
                    java.util.Locale.ROOT,
                    "%.0f%%",
                    ((ElectricVehicleDriver) driver).getBatteryLevel()
            );
        }

        return "N/A";
    }

    private String statusText(DriverStatus status) {

        return switch (status) {
            case AVAILABLE -> "Available";
            case BUSY -> "Busy";
            case OFFLINE -> "Offline";
        };
    }

    private boolean hasActiveAssignment(Driver driver) {

        return AppStore.get().orders().stream()
                .anyMatch(order ->
                        order.getAssignedDriver() == driver
                        && !order.getStatus().equals("Delivered")
                        && !order.getStatus().equals("Cancelled"));
    }

    private void changeStatus(DriverStatus newStatus) {

        Driver selected =
                table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Please select a driver.");
            return;
        }

        if (hasActiveAssignment(selected)) {
            showMessage(
                    "This driver has an active delivery. " +
                    "Complete or cancel the order first.");
            return;
        }

        selected.updateStatus(newStatus);
        table.refresh();
    }

    private void addDriver() {

        Dialog<Driver> dialog = new Dialog<>();

        dialog.setTitle("Add Driver");
        dialog.setHeaderText("Enter driver information");

        ButtonType saveType = new ButtonType(
                "Save", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(saveType, ButtonType.CANCEL);

        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField phoneField = new TextField();

        TextField xField = new TextField("0");
        TextField yField = new TextField("0");

        TextField batteryField = new TextField("100");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(
                "Bicycle", "Motorcycle", "Car", "Electric Vehicle");
        typeBox.setValue("Motorcycle");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(15));

        form.addRow(0,
                new Label("Driver ID:"), idField);

        form.addRow(1,
                new Label("Name:"), nameField);

        form.addRow(2,
                new Label("Phone:"), phoneField);

        form.addRow(3,
                new Label("Vehicle:"), typeBox);

        form.addRow(4,
                new Label("Current X (km):"), xField);

        form.addRow(5,
                new Label("Current Y (km):"), yField);

        form.addRow(6,
                new Label("Battery (%):"), batteryField);

        batteryField.setDisable(true);

        typeBox.valueProperty().addListener(
                (obs, oldValue, newValue) ->
                        batteryField.setDisable(
                                !"Electric Vehicle".equals(newValue)));

        dialog.getDialogPane().setContent(form);

        Button saveButton = (Button) dialog
                .getDialogPane()
                .lookupButton(saveType);

        saveButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {

                    try {

                        int id = Integer.parseInt(
                                idField.getText().trim());

                        String name =
                                nameField.getText().trim();

                        String phone =
                                phoneField.getText().trim();

                        double x = Double.parseDouble(
                                xField.getText().trim());

                        double y = Double.parseDouble(
                                yField.getText().trim());

                        if (id <= 0
                                || name.isBlank()
                                || phone.isBlank()
                                || !Double.isFinite(x)
                                || !Double.isFinite(y)) {

                            throw new IllegalArgumentException(
                                    "Enter valid driver information.");
                        }

                        boolean duplicate =
                                AppStore.get().drivers().stream()
                                        .anyMatch(d ->
                                                d.getDriverId() == id);

                        if (duplicate) {
                            throw new IllegalArgumentException(
                                    "Driver ID already exists.");
                        }

                        if ("Electric Vehicle".equals(
                                typeBox.getValue())) {

                            double battery =
                                    Double.parseDouble(
                                            batteryField.getText().trim());

                            if (!Double.isFinite(battery)
                                    || battery < 0
                                    || battery > 100) {

                                throw new IllegalArgumentException(
                                        "Battery must be between 0 and 100.");
                            }
                        }

                    } catch (NumberFormatException ex) {

                        showMessage(
                                "Driver ID, location and battery " +
                                "must contain valid numbers.");

                        event.consume();

                    } catch (IllegalArgumentException ex) {

                        showMessage(ex.getMessage());
                        event.consume();
                    }
                });

        dialog.setResultConverter(button -> {

            if (button != saveType) {
                return null;
            }

            int id = Integer.parseInt(
                    idField.getText().trim());

            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            double x = Double.parseDouble(
                    xField.getText().trim());

            double y = Double.parseDouble(
                    yField.getText().trim());

            String vehicleType = typeBox.getValue();

            if ("Electric Vehicle".equals(vehicleType)) {
                double battery = Double.parseDouble(
                        batteryField.getText().trim());

                return new ElectricVehicleDriver(
                        id, name, phone, x, y, battery);
            }

            if ("Bicycle".equals(vehicleType)) {
                return new BicycleDriver(id, name, phone, x, y);
            }

            if ("Car".equals(vehicleType)) {
                return new CarDriver(id, name, phone, x, y);
            }

            return new MotorcycleDriver(id, name, phone, x, y);
        });

        dialog.showAndWait().ifPresent(driver -> {

            try {
                AppStore.get().addDriver(driver);
                table.refresh();
            } catch (IllegalArgumentException ex) {
                showMessage(ex.getMessage());
            }
        });
    }

    private void showMessage(String message) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION);

        alert.setTitle("FoodOrbit");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}