package com.foodorbit.presentation;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PaymentsPage extends VBox {

    private final TableView<AppStore.PaymentRecord> table =
            new TableView<>();

    public PaymentsPage() {

        setSpacing(16);
        setPadding(new Insets(15));

        Label title = new Label("Payments & Cash Collection");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label(
                "Payments are created automatically when customers place orders.");
        subtitle.getStyleClass().add("page-subtitle");

        TextField search = new TextField();
        search.setPromptText("Search order, customer, payment...");

        ComboBox<String> filter = new ComboBox<>();
        filter.getItems().addAll(
                "All", "Pending", "Completed", "Refunded");
        filter.setValue("All");

        HBox toolbar = new HBox(12, search, filter);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        table.getColumns().addAll(
                col("Payment ID", p -> p.id),
                col("Order ID", p -> p.orderId),
                col("Customer", p -> p.customer),
                col("Method", p -> p.method),
                col("Amount", p ->
                        String.format("₪%.2f", p.amount)),
                col("Status", AppStore.PaymentRecord::getStatus)
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(new Label(
                "Payments will appear when an order is placed"));

        FilteredList<AppStore.PaymentRecord> filtered =
                new FilteredList<>(AppStore.get().payments());

        table.setItems(filtered);
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable refresh = () -> {

            String q = search.getText().toLowerCase().trim();
            String status = filter.getValue();

            filtered.setPredicate(p ->
                    (p.id.toLowerCase().contains(q)
                    || p.orderId.toLowerCase().contains(q)
                    || p.customer.toLowerCase().contains(q))
                    && (status.equals("All")
                    || p.getStatus().equals(status)));
        };

        search.textProperty().addListener(
                (o, a, b) -> refresh.run());

        filter.valueProperty().addListener(
                (o, a, b) -> refresh.run());

        Label revenue = new Label();
        revenue.getStyleClass().add("page-subtitle");

        Runnable updateRevenue = () ->
                revenue.setText(String.format(
                        "Collected revenue: ₪%.2f",
                        AppStore.get().payments().stream()
                                .filter(p -> p.getStatus()
                                        .equals("Completed"))
                                .mapToDouble(p -> p.amount)
                                .sum()));

        AppStore.get().payments().addListener(
                (javafx.collections.ListChangeListener<AppStore.PaymentRecord>)
                        c -> updateRevenue.run());

        updateRevenue.run();

        // Payment action buttons
        Button collect = new Button("Collect Cash");

        Button refund = new Button("Refund Payment");

        // Update buttons according to selected payment status
        Runnable updatePaymentActions = () -> {

            AppStore.PaymentRecord selected =
                    table.getSelectionModel().getSelectedItem();

            boolean pendingCash = selected != null
                    && "Pending".equals(selected.getStatus())
                    && selected.method.toLowerCase().contains("cash");

            boolean completed = selected != null
                    && "Completed".equals(selected.getStatus());

            collect.setDisable(!pendingCash);

            refund.setDisable(!completed);
        };

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldPayment, newPayment) ->
                        updatePaymentActions.run()
        );

        updatePaymentActions.run();

        // Collect Cash
        collect.setOnAction(e -> {

            AppStore.PaymentRecord p =
                    table.getSelectionModel().getSelectedItem();

            if (p == null) {
                message("Select a payment first");
                return;
            }

            if (!p.method.toLowerCase().contains("cash")) {
                message("Only cash payments can be collected here");
                return;
            }

            AppStore.CustomerOrder order =
                    AppStore.get().orders().stream()
                            .filter(o -> o.id.equals(p.orderId))
                            .findFirst()
                            .orElse(null);

            if (order == null) {
                message("Order not found");
                return;
            }

            TextInputDialog dialog = new TextInputDialog(
                    String.format("%.2f", p.amount));

            dialog.setTitle("Collect Cash");

            dialog.setHeaderText(
                    "Amount due: ₪" +
                    String.format("%.2f", p.amount));

            dialog.setContentText("Cash received (₪):");

            dialog.showAndWait().ifPresent(value -> {

                try {

                    AppStore.get().collectCash(
                            order,
                            Double.parseDouble(value.trim()));

                    table.refresh();

                    updatePaymentActions.run();

                } catch (IllegalArgumentException
                        | IllegalStateException ex) {

                    message(ex.getMessage());
                }
            });
        });

        // Refund Payment
        refund.setOnAction(e -> {

            try {

                AppStore.get().refundPayment(
                        table.getSelectionModel()
                                .getSelectedItem());

                table.refresh();

                updatePaymentActions.run();

            } catch (IllegalArgumentException
                    | IllegalStateException ex) {

                message(ex.getMessage());
            }
        });

        HBox actions = new HBox(10, collect, refund);
        actions.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(
                title,
                subtitle,
                toolbar,
                table,
                revenue,
                actions
        );
    }

    private TableColumn<AppStore.PaymentRecord, String> col(
            String name,
            java.util.function.Function<
                    AppStore.PaymentRecord, String> getter) {

        TableColumn<AppStore.PaymentRecord, String> c =
                new TableColumn<>(name);

        c.setCellValueFactory(d ->
                new ReadOnlyStringWrapper(
                        getter.apply(d.getValue())));

        return c;
    }

    private void message(String msg) {

        Alert a = new Alert(
                Alert.AlertType.INFORMATION,
                msg,
                ButtonType.OK);

        a.setTitle("FoodOrbit");
        a.setHeaderText(null);
        a.showAndWait();
    }
}