package com.foodorbit.presentation;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class NotificationsPage extends VBox {

    private final TableView<AppStore.NotificationRecord> table =
            new TableView<>();

    public NotificationsPage() {
        this(null);
    }

    /** Creates a notification view, optionally restricted to one customer. */
    public NotificationsPage(String customerUsername) {

        setSpacing(18);
        setPadding(new Insets(15));

        boolean managerView = customerUsername == null;
        Label title = new Label(managerView ? "Notifications" : "My Notifications");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label(
                "Track order updates, driver assignments and payment events");
        subtitle.getStyleClass().add("page-subtitle");

        // SEARCH AND FILTER

        TextField searchField = new TextField();
        searchField.setPromptText(
                "Search notifications...");
        searchField.setPrefWidth(280);

        ComboBox<String> statusFilter = new ComboBox<>();

        statusFilter.getItems().addAll(
                "All", "Unread", "Read");

        statusFilter.setValue("All");

        HBox toolbar = new HBox(
                12, searchField, statusFilter);

        toolbar.setAlignment(Pos.CENTER_LEFT);

        // TABLE COLUMNS

        TableColumn<AppStore.NotificationRecord, String> idCol =
                column("Notification ID",
                        n -> n.id);

        TableColumn<AppStore.NotificationRecord, String> orderCol =
                column("Order ID",
                        n -> n.orderId);

        TableColumn<AppStore.NotificationRecord, String> recipientCol =
                column("Recipient",
                        n -> n.recipient);

        TableColumn<AppStore.NotificationRecord, String> messageCol =
                column("Message",
                        n -> n.message);

        TableColumn<AppStore.NotificationRecord, String> statusCol =
                column("Status",
                        AppStore.NotificationRecord::getStatus);

        table.getColumns().addAll(
                idCol,
                orderCol,
                recipientCol,
                messageCol,
                statusCol
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(
                new Label("No notifications yet"));

        table.setPrefHeight(420);

        VBox.setVgrow(table, Priority.ALWAYS);

        // CONNECT TO APP STORE

        FilteredList<AppStore.NotificationRecord> filtered =
                new FilteredList<>(
                        AppStore.get().notifications(),
                        n -> managerView || n.recipient.equalsIgnoreCase(customerUsername)
                );

        table.setItems(filtered);

        // FILTER LOGIC

        Runnable updateFilter = () -> {

            String query = searchField.getText()
                    .toLowerCase()
                    .trim();

            String selectedStatus =
                    statusFilter.getValue();

            filtered.setPredicate(notification -> {

                boolean matchesSearch =
                        notification.id.toLowerCase()
                                .contains(query)
                        || notification.orderId.toLowerCase()
                                .contains(query)
                        || notification.recipient.toLowerCase()
                                .contains(query)
                        || notification.message.toLowerCase()
                                .contains(query);

                boolean matchesStatus =
                        selectedStatus.equals("All")
                        || notification.getStatus()
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

        // ACTION BUTTONS

        Button markReadButton =
                new Button("Mark as Read");

        Button markAllReadButton =
                new Button("Mark All as Read");

        Button refreshButton =
                new Button("Refresh");

        markReadButton.setStyle(
                "-fx-background-color: #176b50;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 16;" +
                "-fx-background-radius: 9;");

        markReadButton.setOnAction(e -> {

            AppStore.NotificationRecord selected =
                    table.getSelectionModel()
                            .getSelectedItem();

            if (selected == null) {
                showMessage(
                        "Please select a notification.");
                return;
            }

            selected.setRead(true);

            updateFilter.run();
            table.refresh();
        });

        markAllReadButton.setOnAction(e -> {

            for (AppStore.NotificationRecord notification :
                    AppStore.get().notifications()) {
                if (managerView || notification.recipient.equalsIgnoreCase(customerUsername)) {
                    notification.setRead(true);
                }
            }

            updateFilter.run();
            table.refresh();
        });

        refreshButton.setOnAction(e -> {

            updateFilter.run();
            table.refresh();
        });

        HBox actions = new HBox(
                10,
                markReadButton,
                markAllReadButton,
                refreshButton
        );

        actions.setAlignment(Pos.CENTER_LEFT);

        // NOTIFICATION COUNTER

        Label unreadLabel = new Label();

        unreadLabel.getStyleClass()
                .add("page-subtitle");

        Runnable updateCounter = () -> {

            long unreadCount =
                    AppStore.get().notifications()
                            .stream()
                            .filter(n -> managerView
                                    || n.recipient.equalsIgnoreCase(customerUsername))
                            .filter(n -> n.getStatus().equals("Unread"))
                            .count();

            unreadLabel.setText(
                    "Unread Notifications: " +
                    unreadCount);
        };

        AppStore.get().notifications().addListener(
                (javafx.collections.ListChangeListener<
                        AppStore.NotificationRecord>)
                        change -> updateCounter.run());

        markReadButton.setOnAction(e -> {

            AppStore.NotificationRecord selected =
                    table.getSelectionModel()
                            .getSelectedItem();

            if (selected == null) {
                showMessage(
                        "Please select a notification.");
                return;
            }

            selected.setRead(true);

            updateFilter.run();
            table.refresh();
            updateCounter.run();
        });

        markAllReadButton.setOnAction(e -> {

            for (AppStore.NotificationRecord notification :
                    AppStore.get().notifications()) {
                if (managerView || notification.recipient.equalsIgnoreCase(customerUsername)) {
                    notification.setRead(true);
                }
            }

            updateFilter.run();
            table.refresh();
            updateCounter.run();
        });

        updateCounter.run();

        // ADD COMPONENTS

        getChildren().addAll(
                title,
                subtitle,
                toolbar,
                unreadLabel,
                table,
                actions
        );
    }

    private TableColumn<AppStore.NotificationRecord, String> column(
            String title,
            java.util.function.Function<
                    AppStore.NotificationRecord, String> getter) {

        TableColumn<AppStore.NotificationRecord, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        getter.apply(data.getValue())));

        return column;
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