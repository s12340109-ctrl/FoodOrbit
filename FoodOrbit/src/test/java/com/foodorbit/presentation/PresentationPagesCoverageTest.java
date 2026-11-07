package com.foodorbit.presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.driver.DriverStatus;
import com.foodorbit.domain.driver.MotorcycleDriver;
import com.foodorbit.domain.model.Restaurant;
import com.foodorbit.domain.driver.BicycleDriver;
import com.foodorbit.domain.driver.CarDriver;
import com.foodorbit.domain.driver.ElectricVehicleDriver;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/** Additional interaction tests for the JavaFX presentation pages. */
class PresentationPagesCoverageTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);

        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            started.countDown();
        }

        Platform.setImplicitExit(false);
        assertTrue(started.await(15, TimeUnit.SECONDS));
    }

    // ---------------------------------------------------------
    // Restaurants Page
    // ---------------------------------------------------------

    @Test
    void restaurantsPageRendersSearchMenuCartAndCheckout() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage("coverage-customer");
            assertNotNull(page);

            ScrollPane scroll = (ScrollPane) page.getChildren().get(0);
            Parent content = (Parent) scroll.getContent();

            assertNotNull(content);
            assertTrue(content.getChildrenUnmodifiable().size() >= 6);

            TextField search = find(content, TextField.class).stream()
                    .filter(f -> f.getPromptText() != null
                            && f.getPromptText().contains("Search restaurants"))
                    .findFirst().orElseThrow();

            search.setText("pizza");
            assertTrue(content.getChildrenUnmodifiable().size() >= 6);
            search.clear();

            Button explore = find(content, Button.class).stream()
                    .filter(b -> "Explore Menu".equals(b.getText()))
                    .findFirst().orElseThrow();

            explore.fire();

            assertTrue(find(content, Button.class).stream()
                    .anyMatch(n -> n.getText() != null
                            && n.getText().contains("Back to Restaurants")));

            Button add = find(content, Button.class).stream()
                    .filter(b -> "Add to Cart".equals(b.getText())
                            && !b.isDisabled())
                    .findFirst().orElseThrow();

            add.fire();

            Button cart = find(content, Button.class).stream()
                    .filter(b -> b.getText() != null
                            && b.getText().startsWith("Cart ("))
                    .findFirst().orElseThrow();

            cart.fire();

            assertTrue(find(content, Button.class).stream()
                    .anyMatch(b -> "Proceed to Checkout".equals(b.getText())));

            find(content, Button.class).stream()
                    .filter(b -> "Proceed to Checkout".equals(b.getText()))
                    .findFirst().orElseThrow().fire();

            assertTrue(find(content, ComboBox.class).stream()
                    .anyMatch(c -> c.getItems().contains("Scheduled")));

            @SuppressWarnings("unchecked")
            ComboBox<String> orderType =
                    (ComboBox<String>) find(content, ComboBox.class).stream()
                            .filter(c -> c.getItems().contains("Scheduled"))
                            .findFirst().orElseThrow();

            orderType.setValue("Scheduled");
            orderType.fireEvent(new javafx.event.ActionEvent());

            assertEquals("Scheduled", orderType.getValue());
        });
    }

    // ---------------------------------------------------------
    // Catalog Management Page
    // ---------------------------------------------------------

    @Test
    void catalogManagementBuildsTablesAndRefreshesSelectedMenu()
            throws Exception {

        onFx(() -> {
            CatalogManagementPage page = new CatalogManagementPage();

            TableView<Restaurant> restaurants =
                    field(page, "restaurantTable");

            TableView<?> menu =
                    field(page, "menuTable");

            ComboBox<Restaurant> filter =
                    field(page, "restaurantFilter");

            assertEquals(6, restaurants.getColumns().size());
            assertEquals(4, menu.getColumns().size());
            assertFalse(restaurants.getItems().isEmpty());

            Method refreshAll =
                    method(CatalogManagementPage.class, "refreshAll");

            refreshAll.invoke(page);

            assertFalse(filter.getItems().isEmpty());

            Restaurant first = filter.getItems().get(0);

            filter.setValue(first);

            method(CatalogManagementPage.class, "refreshMenu")
                    .invoke(page);

            assertEquals(
                    AppStore.get().menuItemsForRestaurant(first.getId()).size(),
                    menu.getItems().size()
            );

            @SuppressWarnings("unchecked")
            javafx.util.StringConverter<Restaurant> converter =
                    (javafx.util.StringConverter<Restaurant>) filter.getConverter();

            assertEquals(first.getName(), converter.toString(first));
            assertEquals("", converter.toString(null));
            assertNull(converter.fromString("anything"));

            filter.setValue(null);

            method(CatalogManagementPage.class, "refreshMenu")
                    .invoke(page);

            assertTrue(menu.getItems().isEmpty());

            Object restaurantForm =
                    method(
                            CatalogManagementPage.class,
                            "restaurantForm",
                            Restaurant.class
                    ).invoke(page, new Object[]{null});

            @SuppressWarnings("unchecked")
            Dialog<Object> restaurantDialog =
                    (Dialog<Object>) recordField(restaurantForm, "dialog");

            List<TextField> restaurantInputs =
                    find(restaurantDialog.getDialogPane(), TextField.class);

            restaurantInputs.get(0).setText("Coverage Restaurant Form");

            assertNotNull(
                    restaurantDialog.getResultConverter().call(ButtonType.OK)
            );

            assertNull(
                    restaurantDialog.getResultConverter().call(ButtonType.CANCEL)
            );

            com.foodorbit.domain.model.MenuItem sample =
                    AppStore.get().menuItemsForRestaurant(first.getId()).get(0);

            Object menuForm =
                    method(
                            CatalogManagementPage.class,
                            "menuForm",
                            com.foodorbit.domain.model.MenuItem.class
                    ).invoke(page, sample);

            @SuppressWarnings("unchecked")
            Dialog<Object> menuDialog =
                    (Dialog<Object>) recordField(menuForm, "dialog");

            List<TextField> menuInputs =
                    find(menuDialog.getDialogPane(), TextField.class);

            menuInputs.get(0).setText("Coverage Menu Form");

            assertNotNull(
                    menuDialog.getResultConverter().call(ButtonType.OK)
            );

            assertNull(
                    menuDialog.getResultConverter().call(ButtonType.CANCEL)
            );
        });
    }

    // ---------------------------------------------------------
    // Orders Page - Existing coverage
    // ---------------------------------------------------------

    @Test
    void ordersPageFiltersCustomersAndUpdatesManagerActions()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            String suffix = Long.toString(System.nanoTime());

            AppStore.CustomerOrder order = store.placeOrder(
                    "Coverage " + suffix,
                    "0591234567",
                    "Nablus Street",
                    "Coverage Restaurant",
                    "Cash on Delivery",
                    List.of("Meal"),
                    25.0
            );

            OrdersPage manager = new OrdersPage();

            TableView<AppStore.CustomerOrder> table =
                    field(manager, "table");

            table.getSelectionModel().select(order);

            method(OrdersPage.class, "updateActionAvailability")
                    .invoke(manager);

            Button confirmed = field(manager, "confirmedButton");
            Button cancel = field(manager, "cancelledButton");
            Button assign = field(manager, "assignButton");
            Button preparing = field(manager, "preparingButton");
            Button delivered = field(manager, "deliveredButton");

            assertFalse(confirmed.isDisabled());
            assertFalse(cancel.isDisabled());
            assertTrue(assign.isDisabled());

            store.changeOrderStatus(order, "Confirmed");

            method(OrdersPage.class, "updateActionAvailability")
                    .invoke(manager);

            assertFalse(assign.isDisabled());
            assertFalse(preparing.isDisabled());
            assertTrue(delivered.isDisabled());

            OrdersPage customer =
                    new OrdersPage("Coverage " + suffix);

            TableView<AppStore.CustomerOrder> customerTable =
                    field(customer, "table");

            assertEquals(1, customerTable.getItems().size());

            assertEquals(
                    "My Orders",
                    ((javafx.scene.control.Label)
                            customer.getChildren().get(0)).getText()
            );
        });
    }

    // ---------------------------------------------------------
    // Orders Page - updateActionAvailability
    // ---------------------------------------------------------

    @Test
    void updateActionAvailabilityHandlesDifferentStatuses()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            OrdersPage page = new OrdersPage();

            TableView<AppStore.CustomerOrder> table =
                    field(page, "table");

            Method update =
                    method(OrdersPage.class, "updateActionAvailability");

            Button assign = field(page, "assignButton");
            Button confirmed = field(page, "confirmedButton");
            Button preparing = field(page, "preparingButton");
            Button ready = field(page, "readyButton");
            Button cancelled = field(page, "cancelledButton");

            // No selection
            table.getSelectionModel().clearSelection();
            update.invoke(page);

            assertTrue(assign.isDisabled());

            AppStore.CustomerOrder order =
                    createOrder("Availability");

            table.getSelectionModel().select(order);
            update.invoke(page);

            // Pending
            assertFalse(confirmed.isDisabled());
            assertFalse(cancelled.isDisabled());
            assertTrue(assign.isDisabled());

            // Confirmed
            store.changeOrderStatus(order, "Confirmed");
            update.invoke(page);

            assertFalse(assign.isDisabled());
            assertFalse(preparing.isDisabled());

            // Preparing
            store.changeOrderStatus(order, "Preparing");
            update.invoke(page);

            assertFalse(ready.isDisabled());

            // Ready
            store.changeOrderStatus(order, "Ready");
            update.invoke(page);

            assertTrue(preparing.isDisabled());
        });
    }

    // ---------------------------------------------------------
    // Orders Page - changeStatus
    // ---------------------------------------------------------

    @Test
    void changeStatusHandlesNoSelectionAndValidTransition()
            throws Exception {

        onFx(() -> {
            OrdersPage page = new OrdersPage();

            Method change =
                    method(OrdersPage.class, "changeStatus", String.class);

            // No selection
            scheduleDialogButton(ButtonType.OK);
            change.invoke(page, "Confirmed");

            // Selected order
            AppStore.CustomerOrder order =
                    createOrder("ChangeStatus");

            TableView<AppStore.CustomerOrder> table =
                    field(page, "table");

            table.getSelectionModel().select(order);

            // Pending -> Confirmed
            scheduleDialogButton(ButtonType.OK);

            change.invoke(page, "Confirmed");

            assertEquals("Confirmed", order.getStatus());
        });
    }

    @Test
    void changeStatusHandlesInvalidTransition()
            throws Exception {

        onFx(() -> {
            OrdersPage page = new OrdersPage();

            AppStore.CustomerOrder order =
                    createOrder("InvalidTransition");

            TableView<AppStore.CustomerOrder> table =
                    field(page, "table");

            table.getSelectionModel().select(order);

            Method change =
                    method(OrdersPage.class, "changeStatus", String.class);

            // Pending -> Delivered is invalid
            scheduleDialogButton(ButtonType.OK);

            change.invoke(page, "Delivered");

            assertEquals("Pending", order.getStatus());
        });
    }

    // ---------------------------------------------------------
    // Orders Page - viewDetails
    // ---------------------------------------------------------

    @Test
    void viewDetailsHandlesNoSelectionAndSelectedOrder()
            throws Exception {

        onFx(() -> {
            OrdersPage page = new OrdersPage();

            Method viewDetails =
                    method(OrdersPage.class, "viewDetails");

            // No selection
            scheduleDialogButton(ButtonType.OK);
            viewDetails.invoke(page);

            // With selection
            AppStore.CustomerOrder order =
                    createOrder("ViewDetails");

            TableView<AppStore.CustomerOrder> table =
                    field(page, "table");

            table.getSelectionModel().select(order);

            scheduleDialogButton(ButtonType.OK);
            viewDetails.invoke(page);
        });
    }

    // ---------------------------------------------------------
    // Orders Page - assignDriver
    // ---------------------------------------------------------

    @Test
    void assignDriverHandlesNoSelection()
            throws Exception {

        onFx(() -> {
            OrdersPage page = new OrdersPage();

            Method assign =
                    method(OrdersPage.class, "assignDriver");

            scheduleDialogButton(ButtonType.OK);

            assign.invoke(page);
        });
    }

    @Test
    void assignDriverHandlesWrongOrderStatus()
            throws Exception {

        onFx(() -> {
            OrdersPage page = new OrdersPage();

            AppStore.CustomerOrder order =
                    createOrder("AssignPending");

            TableView<AppStore.CustomerOrder> table =
                    field(page, "table");

            table.getSelectionModel().select(order);

            Method assign =
                    method(OrdersPage.class, "assignDriver");

            scheduleDialogButton(ButtonType.OK);

            assign.invoke(page);

            assertEquals("Pending", order.getStatus());
        });
    }

    @Test
    void assignDriverOpensDialogAndHandlesCancel()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            OrdersPage page = new OrdersPage();

            AppStore.CustomerOrder order =
                    createOrder("AssignCancel");

            store.changeOrderStatus(order, "Confirmed");

            TableView<AppStore.CustomerOrder> table =
                    field(page, "table");

            table.getSelectionModel().select(order);

            Method assign =
                    method(OrdersPage.class, "assignDriver");

            // Cancel the assignment dialog.
            scheduleDialogButton(ButtonType.CANCEL);

            assign.invoke(page);

            assertEquals("Confirmed", order.getStatus());
        });
    }

    // ---------------------------------------------------------
    // Orders Page - cancelCustomerOrder
    // ---------------------------------------------------------

    @Test
    void cancelCustomerOrderHandlesNoSelection()
            throws Exception {

        onFx(() -> {
            String customer = "NoSelection" + System.nanoTime();

            OrdersPage page = new OrdersPage(customer);

            Method cancel =
                    method(OrdersPage.class, "cancelCustomerOrder");

            scheduleDialogButton(ButtonType.OK);

            cancel.invoke(page);
        });
    }

    @Test
    void cancelCustomerOrderRejectsNonPendingOrder()
            throws Exception {

        onFx(() -> {
            String customer = "NotPending" + System.nanoTime();

            AppStore.CustomerOrder order =
                    createOrder(customer);

            AppStore.get().changeOrderStatus(order, "Confirmed");

            OrdersPage page = new OrdersPage(customer);

            TableView<AppStore.CustomerOrder> table =
                    field(page, "table");

            table.getSelectionModel().select(order);

            Method cancel =
                    method(OrdersPage.class, "cancelCustomerOrder");

            scheduleDialogButton(ButtonType.OK);

            cancel.invoke(page);

            assertEquals("Confirmed", order.getStatus());
        });
    }

    @Test
    void cancelCustomerOrderCanBeConfirmed()
            throws Exception {

        onFx(() -> {
            String customer = "CancelPending" + System.nanoTime();

            AppStore.CustomerOrder order =
                    AppStore.get().placeOrder(
                            customer,
                            "0591234567",
                            "Nablus",
                            "Coverage Restaurant",
                            "Cash",
                            List.of("Meal"),
                            20.0
                    );

            OrdersPage page = new OrdersPage(customer);

            TableView<AppStore.CustomerOrder> table =
                    field(page, "table");

            table.getSelectionModel().select(order);

            Method cancel =
                    method(OrdersPage.class, "cancelCustomerOrder");

            // First dialog: confirmation.
            scheduleDialogButton(ButtonType.OK);

            // Second dialog: successful cancellation message.
            scheduleDialogButton(ButtonType.OK);

            cancel.invoke(page);

            assertEquals("Cancelled", order.getStatus());
        });
    }

    // ---------------------------------------------------------
    // Drivers Page
    // ---------------------------------------------------------

    @Test
    void driversPageSearchesFiltersAndChangesDriverStatus()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            int id = 100000
                    + (int) Math.floorMod(System.nanoTime(), 100000L);

            Driver driver = new MotorcycleDriver(
                    id,
                    "Coverage Driver",
                    "0597654321",
                    1,
                    2
            );

            store.addDriver(driver);

            DriversPage page = new DriversPage();

            TableView<Driver> table =
                    field(page, "table");

            TextField search = find(page, TextField.class).stream()
                    .filter(f -> "Search drivers...".equals(f.getPromptText()))
                    .findFirst().orElseThrow();

            ComboBox<String> status = find(page, ComboBox.class).stream()
                    .filter(c -> c.getItems().contains("Offline"))
                    .map(c -> (ComboBox<String>) c)
                    .findFirst().orElseThrow();

            search.setText("Coverage Driver");

            assertEquals(1, table.getItems().size());

            status.setValue("Available");

            assertEquals(1, table.getItems().size());

            table.getSelectionModel().select(driver);

            button(page, "Set Busy").fire();

            assertEquals(DriverStatus.BUSY, driver.getStatus());

            status.setValue("Busy");

            assertEquals(1, table.getItems().size());

            search.clear();

            assertTrue(table.getItems().size() >= 1);

            method(DriversPage.class, "vehicleType", Driver.class)
                    .invoke(page, driver);

            assertEquals(
                    "Motorcycle",
                    method(DriversPage.class, "vehicleType", Driver.class)
                            .invoke(page, driver)
            );

            assertEquals(
                    "N/A",
                    method(DriversPage.class, "batteryText", Driver.class)
                            .invoke(page, driver)
            );

            assertEquals(
                    "Busy",
                    method(DriversPage.class, "statusText", DriverStatus.class)
                            .invoke(page, DriverStatus.BUSY)
            );
        });
    }
    @Test
    void driversPageCoversAllVehicleTypesAndBatteryBranches()
            throws Exception {

        onFx(() -> {
            DriversPage page = new DriversPage();

            Method vehicleType =
                    method(DriversPage.class, "vehicleType", Driver.class);

            Method batteryText =
                    method(DriversPage.class, "batteryText", Driver.class);

            int id = 300000
                    + (int) Math.floorMod(System.nanoTime(), 1000000L);

            Driver bicycle = new BicycleDriver(
                    id, "Bike", "0591111111", 1, 1);

            Driver motorcycle = new MotorcycleDriver(
                    id + 1, "Moto", "0591111112", 1, 1);

            Driver car = new CarDriver(
                    id + 2, "Car", "0591111113", 1, 1);

            Driver electric = new ElectricVehicleDriver(
                    id + 3, "Electric", "0591111114", 1, 1, 75);

            assertEquals("Bicycle", vehicleType.invoke(page, bicycle));
            assertEquals("Motorcycle", vehicleType.invoke(page, motorcycle));
            assertEquals("Car", vehicleType.invoke(page, car));
            assertEquals("Electric Vehicle", vehicleType.invoke(page, electric));

            assertEquals("N/A", batteryText.invoke(page, bicycle));
            assertEquals("N/A", batteryText.invoke(page, motorcycle));
            assertEquals("N/A", batteryText.invoke(page, car));
            assertEquals("75%", batteryText.invoke(page, electric));

            Driver electricZero = new ElectricVehicleDriver(
                    id + 4, "Electric Zero", "0591111115", 1, 1, 0);

            assertEquals("0%", batteryText.invoke(page, electricZero));
        });
    }

    @Test
    void driversPageCoversAllStatusTextBranches()
            throws Exception {

        onFx(() -> {
            DriversPage page = new DriversPage();

            Method statusText =
                    method(DriversPage.class, "statusText", DriverStatus.class);

            assertEquals("Available",
                    statusText.invoke(page, DriverStatus.AVAILABLE));

            assertEquals("Busy",
                    statusText.invoke(page, DriverStatus.BUSY));

            assertEquals("Offline",
                    statusText.invoke(page, DriverStatus.OFFLINE));
        });
    }
    @Test
    void driversPageChangesStatusForSelectedDriver()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            int id = 400000
                    + (int) Math.floorMod(System.nanoTime(), 1000000L);

            Driver driver = new MotorcycleDriver(
                    id, "Status Coverage", "0591111116", 1, 1);

            store.addDriver(driver);

            DriversPage page = new DriversPage();

            TableView<Driver> table = field(page, "table");

            scheduleDialogButton(ButtonType.OK);
            button(page, "Set Busy").fire();

            assertEquals(DriverStatus.AVAILABLE, driver.getStatus());

            table.getSelectionModel().select(driver);
            button(page, "Set Busy").fire();

            assertEquals(DriverStatus.BUSY, driver.getStatus());

            button(page, "Set Offline").fire();

            assertEquals(DriverStatus.OFFLINE, driver.getStatus());

            button(page, "Set Available").fire();

            assertEquals(DriverStatus.AVAILABLE, driver.getStatus());
        });
    }
    @Test
    void driversPageDeletesSelectedDriver()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            int id = 500000
                    + (int) Math.floorMod(System.nanoTime(), 1000000L);

            Driver driver = new MotorcycleDriver(
                    id, "Delete Coverage", "0591111117", 1, 1);

            store.addDriver(driver);

            DriversPage page = new DriversPage();

            TableView<Driver> table = field(page, "table");

            scheduleDialogButton(ButtonType.OK);
            button(page, "Delete Driver").fire();

            assertTrue(store.drivers().contains(driver));

            table.getSelectionModel().select(driver);

            button(page, "Delete Driver").fire();

            assertFalse(store.drivers().contains(driver));
        });
    }
    @Test
    void driversPageCoversHasActiveAssignmentBranches()
            throws Exception {

        onFx(() -> {
            DriversPage page = new DriversPage();

            Method hasActiveAssignment =
                    method(
                            DriversPage.class,
                            "hasActiveAssignment",
                            Driver.class
                    );

            Driver driver = new MotorcycleDriver(
                    600001,
                    "Assignment Coverage",
                    "0591111118",
                    1,
                    1
            );

            assertFalse((boolean)
                    hasActiveAssignment.invoke(page, driver));

            AppStore.CustomerOrder order =
                    createOrder("DriverAssignment");

            AppStore.get().changeOrderStatus(order, "Confirmed");

            Driver assigned = AppStore.get().assignDriver(
                    order,
                    "Nearest",
                    1,
                    1,
                    2,
                    2
            );

            assertNotNull(assigned);

            assertTrue((boolean)
                    hasActiveAssignment.invoke(page, assigned));

            AppStore.get().changeOrderStatus(order, "Preparing");
            AppStore.get().changeOrderStatus(order, "Ready");
            AppStore.get().changeOrderStatus(order, "Out for Delivery");
            AppStore.get().changeOrderStatus(order, "Delivered");

            assertFalse((boolean)
                    hasActiveAssignment.invoke(page, assigned));
        });
    }
    @Test
    void driversPageAddsAllVehicleTypesSuccessfully()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            DriversPage page = new DriversPage();

            String[] types = {
                    "Bicycle",
                    "Motorcycle",
                    "Car",
                    "Electric Vehicle"
            };

            Class<?>[] expectedTypes = {
                    BicycleDriver.class,
                    MotorcycleDriver.class,
                    CarDriver.class,
                    ElectricVehicleDriver.class
            };

            for (int i = 0; i < types.length; i++) {

                int id = 700000
                        + (int) Math.floorMod(
                                System.nanoTime() + i,
                                1000000L);

                final int driverId = id;
                final String type = types[i];

                Platform.runLater(() -> {

                    DialogPane pane = currentDialogPane();

                    assertNotNull(pane);

                    List<TextField> inputs =
                            find(pane, TextField.class);

                    assertTrue(inputs.size() >= 6);

                    inputs.get(0).setText(
                            String.valueOf(driverId));

                    inputs.get(1).setText("Added Driver");
                    inputs.get(2).setText("0591234567");
                    inputs.get(3).setText("2");
                    inputs.get(4).setText("3");
                    inputs.get(5).setText("80");

                    @SuppressWarnings("unchecked")
                    ComboBox<String> vehicle =
                            (ComboBox<String>) find(
                                    pane, ComboBox.class).get(0);

                    vehicle.setValue(type);

                    button(pane, "Save").fire();
                });

                button(page, "+ Add Driver").fire();

                Driver added = store.drivers().stream()
                        .filter(d -> d.getDriverId() == driverId)
                        .findFirst()
                        .orElseThrow();

                assertEquals(expectedTypes[i], added.getClass());

                if (added instanceof ElectricVehicleDriver ev) {
                    assertEquals(
                            80,
                            ev.getBatteryLevel(),
                            0.001
                    );
                }
            }
        });
    }
    // ---------------------------------------------------------
    // Payments Page
    // ---------------------------------------------------------

    @Test
    void paymentsPageFiltersPaymentsAndUpdatesActionAvailability()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            String suffix = Long.toString(System.nanoTime());

            AppStore.CustomerOrder order = store.placeOrder(
                    "Payment Coverage " + suffix,
                    "0591234567",
                    "Nablus Street",
                    "Payment Restaurant",
                    "Cash on Delivery",
                    List.of("Meal"),
                    31.5
            );

            PaymentsPage page = new PaymentsPage();

            TableView<AppStore.PaymentRecord> table =
                    field(page, "table");

            AppStore.PaymentRecord payment =
                    store.payments().stream()
                            .filter(p -> p.orderId.equals(order.id))
                            .findFirst().orElseThrow();

            table.getSelectionModel().select(payment);

            assertFalse(button(page, "Collect Cash").isDisabled());
            assertTrue(button(page, "Refund Payment").isDisabled());

            TextField search = find(page, TextField.class).stream()
                    .filter(f -> f.getPromptText() != null
                            && f.getPromptText().contains("Search order"))
                    .findFirst().orElseThrow();

            search.setText(order.id);

            assertEquals(1, table.getItems().size());

            ComboBox<String> filter = find(page, ComboBox.class).stream()
                    .filter(c -> c.getItems().contains("Refunded"))
                    .map(c -> (ComboBox<String>) c)
                    .findFirst().orElseThrow();

            filter.setValue("Completed");

            assertTrue(table.getItems().isEmpty());

            filter.setValue("All");
            search.clear();

            assertTrue(table.getItems().size() >= 1);
        });
    }
    @Test
    void paymentsPageCoversPaymentActionBranches() throws Exception {

        onFx(() -> {

            AppStore store = AppStore.get();

            String suffix = Long.toString(System.nanoTime());

            AppStore.CustomerOrder order = store.placeOrder(
                    "Payment Branch " + suffix,
                    "0591234567",
                    "Nablus Street",
                    "Payment Restaurant",
                    "Cash on Delivery",
                    List.of("Meal"),
                    25.0
            );

            PaymentsPage page = new PaymentsPage();

            TableView<AppStore.PaymentRecord> table =
                    field(page, "table");

            Button collect = button(page, "Collect Cash");
            Button refund = button(page, "Refund Payment");

            // Branch: selected == null
            table.getSelectionModel().clearSelection();

            assertTrue(collect.isDisabled());
            assertTrue(refund.isDisabled());

            // Branch: Pending Cash
            AppStore.PaymentRecord payment =
                    store.payments().stream()
                            .filter(p -> p.orderId.equals(order.id))
                            .findFirst()
                            .orElseThrow();

            table.getSelectionModel().select(payment);

            assertFalse(collect.isDisabled());
            assertTrue(refund.isDisabled());

        });
    }

@Test
void paymentsPageCoversCompletedAndRefundedActionBranches()
        throws Exception {

    onFx(() -> {

        AppStore store = AppStore.get();
        String suffix = Long.toString(System.nanoTime());

        // Create cash payment
        AppStore.CustomerOrder cashOrder = store.placeOrder(
                "Cash Branch " + suffix,
                "0591234567",
                "Nablus",
                "Payment Restaurant",
                "Cash on Delivery",
                List.of("Meal"),
                30.0
        );

        AppStore.PaymentRecord cashPayment =
                store.payments().stream()
                        .filter(p -> p.orderId.equals(cashOrder.id))
                        .findFirst()
                        .orElseThrow();

        // Complete payment
        store.collectCash(cashOrder, 30.0);

        PaymentsPage page = new PaymentsPage();

        TableView<AppStore.PaymentRecord> table =
                field(page, "table");

        Button collect = button(page, "Collect Cash");
        Button refund = button(page, "Refund Payment");

        // Completed payment: collect disabled, refund enabled
        table.getSelectionModel().select(cashPayment);

        assertTrue(collect.isDisabled());
        assertFalse(refund.isDisabled());

        // Refund payment
        store.refundPayment(cashPayment);
        table.refresh();

        // Refunded payment: both actions disabled
        assertTrue(collect.isDisabled());
        assertTrue(refund.isDisabled());

        // Create non-cash payment
        AppStore.CustomerOrder cardOrder = store.placeOrder(
                "Card Branch " + suffix,
                "0591234568",
                "Nablus",
                "Payment Restaurant",
                "Card",
                List.of("Meal"),
                20.0
        );

        AppStore.PaymentRecord cardPayment =
                store.payments().stream()
                        .filter(p -> p.orderId.equals(cardOrder.id))
                        .findFirst()
                        .orElseThrow();

        table.getSelectionModel().select(cardPayment);

        // Pending non-cash payment
        assertTrue(collect.isDisabled());
        assertTrue(refund.isDisabled());
    });
}

    // ---------------------------------------------------------
    // Notifications Page
    // ---------------------------------------------------------

    @Test
    void notificationsPageFiltersAndMarksCustomerNotificationsRead()
            throws Exception {

        onFx(() -> {
            AppStore store = AppStore.get();

            String suffix = Long.toString(System.nanoTime());

            store.placeOrder(
                    "Notify " + suffix,
                    "0591234567",
                    "Nablus Street",
                    "Notify Restaurant",
                    "Cash",
                    List.of("Meal"),
                    18.0
            );

            NotificationsPage manager = new NotificationsPage();

            TableView<AppStore.NotificationRecord> table =
                    field(manager, "table");

            assertFalse(table.getItems().isEmpty());

            TextField search = find(manager, TextField.class).stream()
                    .filter(f -> "Search notifications...".equals(f.getPromptText()))
                    .findFirst().orElseThrow();

            search.setText(suffix);

            assertTrue(table.getItems().size() >= 1);

            ComboBox<String> status = find(manager, ComboBox.class).stream()
                    .filter(c -> c.getItems().contains("Unread"))
                    .map(c -> (ComboBox<String>) c)
                    .findFirst().orElseThrow();

            status.setValue("Read");

            assertTrue(table.getItems().isEmpty());

            status.setValue("All");
            search.clear();

            button(manager, "Mark All as Read").fire();

            assertTrue(store.notifications().stream()
                    .allMatch(n -> "Read".equals(n.getStatus())));

            NotificationsPage customer =
                    new NotificationsPage("not-a-recipient");

            TableView<AppStore.NotificationRecord> customerTable =
                    field(customer, "table");

            assertTrue(customerTable.getItems().isEmpty());
        });
    }

    // ---------------------------------------------------------
    // AssignmentInput
    // ---------------------------------------------------------

    @Test
    void assignmentInputStoresEveryParsedValue() throws Exception {

        Class<?> inputType = Class.forName(
                "com.foodorbit.presentation.OrdersPage$AssignmentInput"
        );

        Constructor<?> constructor =
                inputType.getDeclaredConstructor(
                        String.class,
                        double.class,
                        double.class,
                        double.class,
                        double.class
                );

        constructor.setAccessible(true);

        Object input = constructor.newInstance(
                "Fastest",
                1.25,
                2.5,
                7.75,
                3.0
        );

        assertEquals("Fastest", field(input, "strategy"));

        assertEquals(
                1.25,
                (double) field(input, "pickupX"),
                0.0001
        );

        assertEquals(
                2.5,
                (double) field(input, "pickupY"),
                0.0001
        );

        assertEquals(
                7.75,
                (double) field(input, "distance"),
                0.0001
        );

        assertEquals(
                3.0,
                (double) field(input, "weight"),
                0.0001
        );
    }

    // ---------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------

    private static AppStore.CustomerOrder createOrder(String prefix) {
        return AppStore.get().placeOrder(
                prefix + System.nanoTime(),
                "0591234567",
                "Nablus",
                "Coverage Restaurant",
                "Cash",
                List.of("Meal"),
                25.0
        );
    }

    private static void scheduleDialogButton(ButtonType type) {
        Platform.runLater(() -> {
            DialogPane pane = currentDialogPane();

            if (pane == null) {
                return;
            }

            Node node = pane.lookupButton(type);

            if (node instanceof Button button) {
                button.fire();
            }
        });
    }

    private static DialogPane currentDialogPane() {
        for (Window window : Window.getWindows()) {
            if (!window.isShowing() || window.getScene() == null) {
                continue;
            }

            Parent root = window.getScene().getRoot();

            if (root instanceof DialogPane pane) {
                return pane;
            }

            List<DialogPane> panes =
                    find(root, DialogPane.class);

            if (!panes.isEmpty()) {
                return panes.get(0);
            }
        }

        return null;
    }

    private static <T> T field(Object target, String name)
            throws Exception {

        Field field =
                target.getClass().getDeclaredField(name);

        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        T value = (T) field.get(target);

        return value;
    }

    private static Object recordField(Object target, String name)
            throws Exception {

        Field field =
                target.getClass().getDeclaredField(name);

        field.setAccessible(true);

        return field.get(target);
    }

    private static Method method(
            Class<?> type,
            String name,
            Class<?>... parameters
    ) throws Exception {

        Method method =
                type.getDeclaredMethod(name, parameters);

        method.setAccessible(true);

        return method;
    }

    private static Button button(Parent root, String text) {
        return find(root, Button.class).stream()
                .filter(b -> text.equals(b.getText()))
                .findFirst()
                .orElseThrow();
    }

    private static <T extends Node> List<T> find(
            Parent root,
            Class<T> type
    ) {

        java.util.ArrayList<T> result =
                new java.util.ArrayList<>();

        collect(root, type, result);

        return result;
    }

    private static <T extends Node> void collect(
            Node node,
            Class<T> type,
            List<T> result
    ) {

        if (type.isInstance(node)) {
            result.add(type.cast(node));
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, type, result);
            }
        }
    }

    private static void onFx(ThrowingRunnable action)
            throws Exception {

        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch done = new CountDownLatch(1);

        AtomicReference<Throwable> failure =
                new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable ex) {
                failure.set(ex);
            } finally {
                done.countDown();
            }
        });

        assertTrue(
                done.await(30, TimeUnit.SECONDS),
                "Timed out waiting for JavaFX task"
        );

        if (failure.get() != null) {
            if (failure.get() instanceof Exception ex) {
                throw ex;
            }

            if (failure.get() instanceof Error error) {
                throw error;
            }

            throw new RuntimeException(failure.get());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}