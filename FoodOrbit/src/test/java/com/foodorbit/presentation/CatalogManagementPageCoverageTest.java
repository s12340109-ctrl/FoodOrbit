package com.foodorbit.presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.model.Restaurant;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Window;

class CatalogManagementPageCoverageTest {

    private static final AppStore STORE = AppStore.get();

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);

        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException ex) {
            Platform.runLater(started::countDown);
        }

        assertTrue(started.await(15, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    @Test
    void catalogOperationsCanBeExecutedThroughButtons() throws Exception {
        onFx(() -> {
            CatalogManagementPage page = new CatalogManagementPage();

            scheduleDialog("Add Restaurant", pane -> {
                List<TextField> fields = find(pane, TextField.class);
                assertEquals(3, fields.size());

                fields.get(0).setText("Coverage Restaurant");
                fields.get(1).setText("20");
                fields.get(2).setText("5");

                ((Button) pane.lookupButton(ButtonType.OK)).fire();
            });

            fireInTab(page, "Restaurants", "+ Add Restaurant");

            Restaurant restaurant = STORE.restaurants().stream()
                    .filter(r -> r.getName().equals("Coverage Restaurant"))
                    .findFirst()
                    .orElseThrow();

            selectRestaurant(page, restaurant);

            scheduleDialog("Edit Restaurant", pane -> {
                List<TextField> fields = find(pane, TextField.class);

                fields.get(0).setText("Coverage Restaurant Updated");
                fields.get(1).setText("25");
                fields.get(2).setText("6");

                ((Button) pane.lookupButton(ButtonType.OK)).fire();
            });

            fireInTab(page, "Restaurants", "Edit Selected");

            Restaurant updatedRestaurant = STORE.restaurants().stream()
                    .filter(r -> r.getId().equals(restaurant.getId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(
                    "Coverage Restaurant Updated",
                    updatedRestaurant.getName()
            );

            selectRestaurant(page, updatedRestaurant);

            scheduleDialog("Add Menu Item", pane -> {
                List<TextField> fields = find(pane, TextField.class);
                assertEquals(2, fields.size());

                fields.get(0).setText("Coverage Menu Item");
                fields.get(1).setText("30");

                ((Button) pane.lookupButton(ButtonType.OK)).fire();
            });

            fireInTab(page, "Menu Items", "+ Add Menu Item");

            MenuItem item = STORE.menuItemsForRestaurant(
                    restaurant.getId()
            ).stream()
                    .filter(m -> m.getName().equals("Coverage Menu Item"))
                    .findFirst()
                    .orElseThrow();

            selectMenuItem(page, item);

            boolean oldAvailability = item.isAvailable();

            fireInTab(page, "Menu Items", "Toggle Availability");

            MenuItem toggledItem = STORE.menuItemsForRestaurant(
                    restaurant.getId()
            ).stream()
                    .filter(m -> m.getId().equals(item.getId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(!oldAvailability, toggledItem.isAvailable());

            selectMenuItem(page, toggledItem);

            scheduleDialog("Edit Menu Item", pane -> {
                List<TextField> fields = find(pane, TextField.class);

                fields.get(0).setText("Coverage Menu Updated");
                fields.get(1).setText("45");

                ((Button) pane.lookupButton(ButtonType.OK)).fire();
            });

            fireInTab(page, "Menu Items", "Edit Selected");

            MenuItem editedItem = STORE.menuItemsForRestaurant(
                    restaurant.getId()
            ).stream()
                    .filter(m -> m.getId().equals(item.getId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals("Coverage Menu Updated", editedItem.getName());
            assertEquals(45.0, editedItem.getPrice());

            selectMenuItem(page, editedItem);

            scheduleAlert(ButtonType.YES);
            fireInTab(page, "Menu Items", "Delete Selected");

            assertTrue(STORE.menuItemsForRestaurant(
                    restaurant.getId()
            ).stream().noneMatch(m -> m.getId().equals(item.getId())));

            selectRestaurant(page, updatedRestaurant);

            scheduleAlert(ButtonType.YES);
            fireInTab(page, "Restaurants", "Delete Selected");

            assertTrue(STORE.restaurants().stream()
                    .noneMatch(r -> r.getId().equals(restaurant.getId())));
        });
    }

    @Test
    void cancelRestaurantDeletionKeepsRestaurant() throws Exception {
        onFx(() -> {
            Restaurant restaurant = createRestaurant(
                    "Coverage Cancel Delete"
            );

            CatalogManagementPage page = new CatalogManagementPage();

            selectRestaurant(page, restaurant);

            scheduleAlert(ButtonType.NO);
            fireInTab(page, "Restaurants", "Delete Selected");

            assertTrue(STORE.restaurants().stream()
                    .anyMatch(r -> r.getId().equals(restaurant.getId())));

            STORE.deleteRestaurant(restaurant.getId());
        });
    }

 

    @Test
    void operationsWithoutSelectionShowInformationAlerts() throws Exception {
        onFx(() -> {
            CatalogManagementPage page = new CatalogManagementPage();

            // No restaurant selected: add menu item.
            scheduleAlert(ButtonType.OK);
            fireInTab(page, "Menu Items", "+ Add Menu Item");

            // No restaurant selected: edit restaurant.
            scheduleAlert(ButtonType.OK);
            fireInTab(page, "Restaurants", "Edit Selected");

            // No restaurant selected: delete restaurant.
            scheduleAlert(ButtonType.OK);
            fireInTab(page, "Restaurants", "Delete Selected");

            // Select a restaurant, but no menu item.
            Restaurant restaurant = STORE.restaurants().get(0);
            selectRestaurant(page, restaurant);

            scheduleAlert(ButtonType.OK);
            fireInTab(page, "Menu Items", "Edit Selected");

            scheduleAlert(ButtonType.OK);
            fireInTab(page, "Menu Items", "Delete Selected");

            scheduleAlert(ButtonType.OK);
            fireInTab(page, "Menu Items", "Toggle Availability");
        });
    }

    // ---------------------------------
    // DATA HELPERS
    // ---------------------------------

    private static Restaurant createRestaurant(String name) {
        String id = "TEST-R-" + System.nanoTime();

        Restaurant restaurant = new Restaurant(
                id, name, true, true, 10.0, 5.0
        );

        STORE.addRestaurant(restaurant);
        return restaurant;
    }

    private static MenuItem createMenuItem(
            Restaurant restaurant,
            String name) {

        String id = "TEST-M-" + System.nanoTime();

        MenuItem item = new MenuItem(
                id, name, 20.0, true
        );

        STORE.addMenuItem(restaurant.getId(), item);
        return item;
    }

    // ---------------------------------
    // PAGE HELPERS
    // ---------------------------------

    private static void selectRestaurant(
            CatalogManagementPage page,
            Restaurant restaurant) throws Exception {

        TableView<Restaurant> table =
                field(page, "restaurantTable");

        table.getSelectionModel().select(
                table.getItems().stream()
                        .filter(r -> r.getId().equals(restaurant.getId()))
                        .findFirst()
                        .orElseThrow()
        );

        ComboBox<Restaurant> filter =
                field(page, "restaurantFilter");

        filter.getSelectionModel().select(
                filter.getItems().stream()
                        .filter(r -> r.getId().equals(restaurant.getId()))
                        .findFirst()
                        .orElseThrow()
        );
    }

    private static void selectMenuItem(
            CatalogManagementPage page,
            MenuItem item) throws Exception {

        ComboBox<Restaurant> filter =
                field(page, "restaurantFilter");

        TableView<MenuItem> table =
                field(page, "menuTable");

        // تأكد أن المطعم المرتبط بالصنف هو المختار
        Restaurant restaurant = STORE.restaurants().stream()
                .filter(r -> STORE.menuItemsForRestaurant(r.getId())
                        .stream()
                        .anyMatch(m -> m.getId().equals(item.getId())))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Restaurant for menu item not found: " + item.getId()
                ));

        filter.getSelectionModel().select(restaurant);

        // ابحث عن الصنف بعد تحديث القائمة
        MenuItem row = table.getItems().stream()
                .filter(m -> m.getId().equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Menu item not found in table: " + item.getName()
                        + ", restaurant: " + restaurant.getName()
                        + ", table items: " + table.getItems().size()
                ));

        table.getSelectionModel().select(row);
    }

    private static void fireInTab(
            CatalogManagementPage page,
            String tabName,
            String buttonText) {

        TabPane tabs = find(page, TabPane.class).stream()
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("TabPane not found"));

        Tab tab = tabs.getTabs().stream()
                .filter(t -> tabName.equals(t.getText()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Tab not found: " + tabName));

        tabs.getSelectionModel().select(tab);

        if (!(tab.getContent() instanceof Parent content)) {
            throw new AssertionError(
                    "Tab content not found: " + tabName
            );
        }

        Button button = find(content, Button.class).stream()
                .filter(b -> buttonText.equals(b.getText()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Button not found: " + buttonText
                        ));

        button.fire();
    }

    // ---------------------------------
    // DIALOG AUTOMATION
    // ---------------------------------

    private static void scheduleDialog(
            String title,
            Consumer<DialogPane> action) {

        Platform.runLater(() -> {
            int expectedFields =
                    title.contains("Restaurant") ? 3 : 2;

            Window window = Window.getWindows().stream()
                    .filter(Window::isShowing)
                    .filter(w -> w.getScene() != null)
                    .filter(w -> w.getScene().getRoot()
                            instanceof DialogPane)
                    .filter(w -> {
                        DialogPane pane =
                                (DialogPane) w.getScene().getRoot();

                        return find(pane, TextField.class).size()
                                == expectedFields;
                    })
                    .findFirst()
                    .orElseThrow(() ->
                            new AssertionError(
                                    "Dialog not found: " + title
                            ));

            DialogPane pane =
                    (DialogPane) window.getScene().getRoot();

            action.accept(pane);
        });
    }

    private static void scheduleAlert(ButtonType buttonType) {
        Platform.runLater(() -> {
            Window window = Window.getWindows().stream()
                    .filter(Window::isShowing)
                    .filter(w -> w.getScene() != null
                            && w.getScene().getRoot() instanceof DialogPane)
                    .filter(w -> {
                        DialogPane pane =
                                (DialogPane) w.getScene().getRoot();

                        return pane.getButtonTypes().contains(buttonType);
                    })
                    .findFirst()
                    .orElseThrow(() ->
                            new AssertionError(
                                    "Alert not found: " + buttonType
                            ));

            DialogPane pane =
                    (DialogPane) window.getScene().getRoot();

            Button button = (Button) pane.lookupButton(buttonType);
            button.fire();
        });
    }

    // ---------------------------------
    // REFLECTION AND NODE HELPERS
    // ---------------------------------

    @SuppressWarnings("unchecked")
    private static <T> T field(
            Object target,
            String name) throws Exception {

        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);

        return (T) field.get(target);
    }

    private static <T extends Node> List<T> find(
            Parent root,
            Class<T> type) {

        java.util.ArrayList<T> result =
                new java.util.ArrayList<>();

        collect(root, type, result);
        return result;
    }

    private static <T extends Node> void collect(
            Node node,
            Class<T> type,
            List<T> result) {

        if (type.isInstance(node)) {
            result.add(type.cast(node));
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, type, result);
            }
        }
    }

    // ---------------------------------
    // JAVAFX THREAD HELPER
    // ---------------------------------

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
                done.await(45, TimeUnit.SECONDS),
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