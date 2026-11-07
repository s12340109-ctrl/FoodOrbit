package com.foodorbit.presentation;

import com.foodorbit.domain.driver.Driver;

import javafx.beans.property.SimpleStringProperty;
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

public class OrdersPage extends VBox {

    private final TableView<AppStore.CustomerOrder> table =
            new TableView<>();

    private final boolean managerView;

    // Order action buttons
    private Button assignButton;
    private Button confirmedButton;
    private Button preparingButton;
    private Button readyButton;
    private Button outButton;
    private Button deliveredButton;
    private Button cancelledButton;

    public OrdersPage() {
        this(null);
    }

    /** Creates an order view limited to one customer's own orders. */
    public OrdersPage(String customerUsername) {

        this.managerView = customerUsername == null;

        setSpacing(18);
        setPadding(new Insets(15));

        Label title = new Label(
                managerView ? "Orders Management" : "My Orders");

        title.getStyleClass().add("page-title");

        Label subtitle = new Label(managerView
                ? "Manage orders, assign drivers and track deliveries"
                : "View your orders and follow their delivery progress");

        subtitle.getStyleClass().add("page-subtitle");

        TableColumn<AppStore.CustomerOrder, String> idCol =
                column("Order ID", order -> order.id);

        TableColumn<AppStore.CustomerOrder, String> customerCol =
                column("Customer", order -> order.customer);

        TableColumn<AppStore.CustomerOrder, String> restaurantCol =
                column("Restaurant", order -> order.restaurant);

        // Order Type
        TableColumn<AppStore.CustomerOrder, String> typeCol =
                column(
                        "Order Type",
                        order -> order.getOrderType().name()
                );

        // Scheduled Delivery
        TableColumn<AppStore.CustomerOrder, String> scheduledCol =
                column(
                        "Scheduled Delivery",
                        order -> order.getScheduledDeliveryTime() == null
                                ? "-"
                                : order.getScheduledDeliveryTime()
                                        .toString()
                                        .replace('T', ' ')
                );

        TableColumn<AppStore.CustomerOrder, String> totalCol =
                column(
                        "Total",
                        order -> String.format("₪%.2f", order.total)
                );

        TableColumn<AppStore.CustomerOrder, String> statusCol =
                column(
                        "Status",
                        AppStore.CustomerOrder::getStatus
                );

        TableColumn<AppStore.CustomerOrder, String> driverCol =
                column(
                        "Assigned Driver",
                        AppStore.CustomerOrder::getDriverName
                );

        TableColumn<AppStore.CustomerOrder, String> strategyCol =
                column(
                        "Strategy",
                        AppStore.CustomerOrder::getSelectionStrategy
                );

        table.getColumns().addAll(
                idCol,
                customerCol,
                restaurantCol,
                typeCol,
                scheduledCol,
                totalCol,
                statusCol,
                driverCol,
                strategyCol
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        if (managerView) {

            table.setItems(AppStore.get().orders());

        } else {

            table.setItems(
                    new javafx.collections.transformation.FilteredList<>(
                            AppStore.get().orders(),
                            order -> order.customer.equalsIgnoreCase(
                                    customerUsername)
                    )
            );
        }

        table.setPlaceholder(
                new Label("No orders yet. Place an order from Restaurants."));

        table.setPrefHeight(400);
        VBox.setVgrow(table, Priority.ALWAYS);

        if (managerView) {

            // Assign Driver button
            assignButton = new Button("Assign Driver");

            assignButton.setStyle(
                    "-fx-background-color: #176b50;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 10 16;" +
                    "-fx-background-radius: 9;");

            assignButton.setOnAction(e -> assignDriver());

            // Order status buttons
            confirmedButton = new Button("Confirmed");
            preparingButton = new Button("Preparing");
            readyButton = new Button("Ready");
            outButton = new Button("Out for Delivery");
            deliveredButton = new Button("Delivered");
            cancelledButton = new Button("Cancelled");

            Button detailsButton = new Button("View Details");

            confirmedButton.setOnAction(
                    e -> changeStatus("Confirmed"));

            preparingButton.setOnAction(
                    e -> changeStatus("Preparing"));

            readyButton.setOnAction(
                    e -> changeStatus("Ready"));

            outButton.setOnAction(
                    e -> changeStatus("Out for Delivery"));

            deliveredButton.setOnAction(
                    e -> changeStatus("Delivered"));

            cancelledButton.setOnAction(
                    e -> changeStatus("Cancelled"));

            detailsButton.setOnAction(e -> viewDetails());

            HBox actions = new HBox(
                    9,
                    assignButton,
                    confirmedButton,
                    preparingButton,
                    readyButton,
                    outButton,
                    deliveredButton,
                    cancelledButton,
                    detailsButton
            );

            actions.setAlignment(Pos.CENTER_LEFT);

            // Update buttons whenever the selected order changes
            table.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldOrder, newOrder) ->
                            updateActionAvailability()
            );

            updateActionAvailability();

            // Action hint
            Label actionHint = new Label(
                    "Select an order to see the actions allowed for its current status."
            );

            actionHint.getStyleClass().add("page-subtitle");

            // Scrollable action buttons
            javafx.scene.control.ScrollPane actionScroll =
                    new javafx.scene.control.ScrollPane(actions);

            actionScroll.setFitToHeight(true);

            actionScroll.setHbarPolicy(
                    javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);

            actionScroll.setVbarPolicy(
                    javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);

            actionScroll.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-background: transparent;");

            getChildren().addAll(
                    title,
                    subtitle,
                    table,
                    actionHint,
                    actionScroll
            );

        } else {

            Button detailsButton =
                    new Button("View Order Details");

            detailsButton.setOnAction(e -> viewDetails());

            Button cancelButton =
                    new Button("Cancel Pending Order");

            cancelButton.setOnAction(
                    e -> cancelCustomerOrder());

            HBox customerActions =
                    new HBox(10, cancelButton, detailsButton);

            customerActions.setAlignment(Pos.CENTER_RIGHT);

            getChildren().addAll(
                    title,
                    subtitle,
                    table,
                    customerActions
            );
        }
    }

    /**
     * Enables only actions that are valid for the selected order.
     */
    private void updateActionAvailability() {

        if (!managerView || assignButton == null) {
            return;
        }

        AppStore.CustomerOrder selected =
                table.getSelectionModel().getSelectedItem();

        String status = selected == null
                ? ""
                : selected.getStatus();

        boolean hasDriver = selected != null
                && selected.getAssignedDriver() != null;

        assignButton.setDisable(
                selected == null
                || !"Confirmed".equals(status)
                || hasDriver
        );

        confirmedButton.setDisable(
                selected == null
                || !"Pending".equals(status)
        );

        preparingButton.setDisable(
                selected == null
                || !"Confirmed".equals(status)
        );

        readyButton.setDisable(
                selected == null
                || !"Preparing".equals(status)
        );

        outButton.setDisable(
                selected == null
                || !"Ready".equals(status)
                || !hasDriver
        );

        deliveredButton.setDisable(
                selected == null
                || !"Out for Delivery".equals(status)
        );

        cancelledButton.setDisable(
                selected == null
                || !"Pending".equals(status)
        );
    }

    private void cancelCustomerOrder() {

        AppStore.CustomerOrder selected =
                table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Please select one of your orders first.");
            return;
        }

        if (!"Pending".equals(selected.getStatus())) {
            showMessage("Only pending orders can be cancelled.");
            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Cancel Order");

        confirmation.setHeaderText(
                "Cancel order " + selected.id + "?");

        confirmation.setContentText(
                "This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                try {

                    AppStore.get().changeOrderStatus(
                            selected, "Cancelled");

                    table.refresh();

                    showMessage(
                            "Your order has been cancelled.");

                } catch (IllegalArgumentException
                        | IllegalStateException ex) {

                    showMessage(ex.getMessage());
                }
            }
        });
    }

    private TableColumn<AppStore.CustomerOrder, String> column(
            String title,
            java.util.function.Function<AppStore.CustomerOrder, String> getter) {

        TableColumn<AppStore.CustomerOrder, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new SimpleStringProperty(
                        getter.apply(data.getValue())));

        return column;
    }

    private void assignDriver() {

        AppStore.CustomerOrder selectedOrder =
                table.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showMessage("Please select an order first.");
            return;
        }

        if (!selectedOrder.getStatus().equals("Confirmed")) {
            showMessage(
                    "Only confirmed orders can be assigned to drivers.");
            return;
        }

        if (selectedOrder.getAssignedDriver() != null) {
            showMessage(
                    "This order already has an assigned driver.");
            return;
        }

        if (AppStore.get().drivers().isEmpty()) {
            showMessage(
                    "No drivers found. Add drivers from Drivers page first.");
            return;
        }

        Dialog<AssignmentInput> dialog = new Dialog<>();

        dialog.setTitle("Assign Driver");

        dialog.setHeaderText(
                "Select a strategy and enter delivery information");

        ButtonType assignType = new ButtonType(
                "Assign",
                ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(assignType, ButtonType.CANCEL);

        ComboBox<String> strategyBox = new ComboBox<>();

        strategyBox.getItems().addAll(
                "Nearest",
                "Fastest");

        strategyBox.setValue("Nearest");

        TextField pickupXField = new TextField("0");
        TextField pickupYField = new TextField("0");
        TextField distanceField = new TextField("5");
        TextField weightField = new TextField("2");

        GridPane form = new GridPane();

        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(15));

        form.addRow(
                0,
                new Label("Selection Strategy:"),
                strategyBox
        );

        form.addRow(
                1,
                new Label("Restaurant X (km):"),
                pickupXField
        );

        form.addRow(
                2,
                new Label("Restaurant Y (km):"),
                pickupYField
        );

        form.addRow(
                3,
                new Label("Delivery Distance (km):"),
                distanceField
        );

        form.addRow(
                4,
                new Label("Order Weight (kg):"),
                weightField
        );

        dialog.getDialogPane().setContent(form);

        Button assign = (Button) dialog
                .getDialogPane()
                .lookupButton(assignType);

        assign.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    try {

                        double x = Double.parseDouble(
                                pickupXField.getText().trim());

                        double y = Double.parseDouble(
                                pickupYField.getText().trim());

                        double distance = Double.parseDouble(
                                distanceField.getText().trim());

                        double weight = Double.parseDouble(
                                weightField.getText().trim());

                        if (!Double.isFinite(x)
                                || !Double.isFinite(y)
                                || !Double.isFinite(distance)
                                || !Double.isFinite(weight)
                                || distance < 0
                                || weight <= 0) {

                            throw new IllegalArgumentException(
                                    "Enter valid location, distance and weight.");
                        }

                    } catch (NumberFormatException ex) {

                        showMessage(
                                "Please enter valid numeric values.");

                        event.consume();

                    } catch (IllegalArgumentException ex) {

                        showMessage(ex.getMessage());

                        event.consume();
                    }
                });

        dialog.setResultConverter(button -> {

            if (button != assignType) {
                return null;
            }

            return new AssignmentInput(
                    strategyBox.getValue(),
                    Double.parseDouble(
                            pickupXField.getText().trim()),
                    Double.parseDouble(
                            pickupYField.getText().trim()),
                    Double.parseDouble(
                            distanceField.getText().trim()),
                    Double.parseDouble(
                            weightField.getText().trim())
            );
        });

        dialog.showAndWait().ifPresent(input -> {

            try {

                Driver selectedDriver =
                        AppStore.get().assignDriver(
                                selectedOrder,
                                input.strategy,
                                input.pickupX,
                                input.pickupY,
                                input.distance,
                                input.weight
                        );

                table.refresh();

                // Refresh available actions after assigning driver
                updateActionAvailability();

                showMessage(
                        "Driver assigned successfully!\n\n" +
                        "Order: " + selectedOrder.id + "\n" +
                        "Driver: " + selectedDriver.getName() + "\n" +
                        "Driver ID: " + selectedDriver.getDriverId() + "\n" +
                        "Strategy: " + input.strategy + "\n" +
                        "Driver Status: Busy"
                );

            } catch (IllegalArgumentException
                    | IllegalStateException ex) {

                showMessage(ex.getMessage());
            }
        });
    }

    private void changeStatus(String newStatus) {

        AppStore.CustomerOrder selected =
                table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Please select an order first.");
            return;
        }

        try {

            AppStore.get().changeOrderStatus(
                    selected,
                    newStatus);

            table.refresh();

            // Refresh available actions after changing order status
            updateActionAvailability();

            showMessage(
                    "Order " + selected.id +
                    " status updated to " + newStatus);

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            showMessage(ex.getMessage());
        }
    }

    private void viewDetails() {

        AppStore.CustomerOrder order =
                table.getSelectionModel().getSelectedItem();

        if (order == null) {
            showMessage("Please select an order first.");
            return;
        }

        String driverInfo = order.getAssignedDriver() == null
                ? "Not Assigned"
                : order.getAssignedDriver().getName()
                  + " (ID: "
                  + order.getAssignedDriver().getDriverId()
                  + ")";

        showMessage(
                "Order: " + order.id +
                "\nCustomer: " + order.customer +
                "\nPhone: " + order.phone +
                "\nAddress: " + order.address +
                "\nRestaurant: " + order.restaurant +
                "\nItems: " + String.join(", ", order.items) +
                "\nPayment: " + order.paymentMethod +
                "\nOrder Type: " + order.getOrderType() +
                "\nScheduled Delivery: " +
                (order.getScheduledDeliveryTime() == null
                        ? "Not scheduled"
                        : order.getScheduledDeliveryTime()
                                .toString()
                                .replace('T', ' ')) +
                "\nTotal: ₪" + String.format("%.2f", order.total) +
                "\nStatus: " + order.getStatus() +
                "\nAssigned Driver: " + driverInfo +
                "\nStrategy: " + order.getSelectionStrategy()
        );
    }

    private void showMessage(String message) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION);

        alert.setTitle("FoodOrbit");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private static class AssignmentInput {

        final String strategy;
        final double pickupX;
        final double pickupY;
        final double distance;
        final double weight;

        AssignmentInput(
                String strategy,
                double pickupX,
                double pickupY,
                double distance,
                double weight) {

            this.strategy = strategy;
            this.pickupX = pickupX;
            this.pickupY = pickupY;
            this.distance = distance;
            this.weight = weight;
        }
    }
}