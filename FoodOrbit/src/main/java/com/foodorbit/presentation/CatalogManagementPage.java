package com.foodorbit.presentation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.model.Restaurant;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Manager interface for maintaining restaurants and their menu catalogs. */
public class CatalogManagementPage extends VBox {

    private static final AtomicInteger RESTAURANT_IDS = new AtomicInteger(100);
    private static final AtomicInteger MENU_IDS = new AtomicInteger(100);

    private final AppStore store = AppStore.get();
    private final TableView<Restaurant> restaurantTable = new TableView<>();
    private final TableView<MenuItem> menuTable = new TableView<>();
    private final ComboBox<Restaurant> restaurantFilter = new ComboBox<>();

    public CatalogManagementPage() {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #F4F9F7;");

        Label title = new Label("Catalog Management");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #103B36;");
        Label subtitle = new Label("Manage restaurants, menus, prices and availability.");
        subtitle.setStyle("-fx-text-fill: #647B77; -fx-font-size: 13px;");

        TabPane tabs = new TabPane();
        Tab restaurantsTab = new Tab("Restaurants", buildRestaurantsTab());
        Tab menuTab = new Tab("Menu Items", buildMenuTab());
        restaurantsTab.setClosable(false);
        menuTab.setClosable(false);
        tabs.getTabs().addAll(restaurantsTab, menuTab);

        getChildren().addAll(title, subtitle, tabs);
        VBox.setVgrow(tabs, javafx.scene.layout.Priority.ALWAYS);
        refreshAll();
    }

    private VBox buildRestaurantsTab() {
        configureRestaurantTable();
        Button add = actionButton("+ Add Restaurant", this::addRestaurant);
        Button edit = actionButton("Edit Selected", this::editRestaurant);
        Button delete = actionButton("Delete Selected", this::deleteRestaurant);
        Button refresh = actionButton("Refresh", this::refreshAll);
        HBox actions = new HBox(10, add, edit, delete, refresh);
        actions.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(12, actions, restaurantTable);
        box.setPadding(new Insets(16, 4, 4, 4));
        VBox.setVgrow(restaurantTable, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    private VBox buildMenuTab() {
        configureMenuTable();
        restaurantFilter.setPromptText("Select restaurant");
        restaurantFilter.setMaxWidth(280);
        restaurantFilter.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Restaurant restaurant) {
                return restaurant == null ? "" : restaurant.getName();
            }

            @Override
            public Restaurant fromString(String value) {
                return null;
            }
        });
        restaurantFilter.setOnAction(e -> refreshMenu());

        Button add = actionButton("+ Add Menu Item", this::addMenuItem);
        Button edit = actionButton("Edit Selected", this::editMenuItem);
        Button delete = actionButton("Delete Selected", this::deleteMenuItem);
        Button availability = actionButton("Toggle Availability", this::toggleAvailability);
        HBox actions = new HBox(10, add, edit, delete, availability);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, restaurantFilter, actions, menuTable);
        box.setPadding(new Insets(16, 4, 4, 4));
        VBox.setVgrow(menuTable, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    private void configureRestaurantTable() {
        TableColumn<Restaurant, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getId()));
        TableColumn<Restaurant, String> name = new TableColumn<>("Restaurant");
        name.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        TableColumn<Restaurant, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isOpen() ? "Open" : "Closed"));
        TableColumn<Restaurant, String> zone = new TableColumn<>("Delivery Zone");
        zone.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isWithinDeliveryZone() ? "Served" : "Not Served"));
        TableColumn<Restaurant, Number> minimum = new TableColumn<>("Minimum Order");
        minimum.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMinimumOrderValue()));
        TableColumn<Restaurant, Number> distance = new TableColumn<>("Distance (km)");
        distance.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getDeliveryDistance()));
        restaurantTable.getColumns().setAll(id, name, status, zone, minimum, distance);
        restaurantTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        restaurantTable.setPlaceholder(new Label("No restaurants found."));
    }

    private void configureMenuTable() {
        TableColumn<MenuItem, String> id = new TableColumn<>("ID");
        id.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getId()));
        TableColumn<MenuItem, String> name = new TableColumn<>("Menu Item");
        name.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        TableColumn<MenuItem, Number> price = new TableColumn<>("Price");
        price.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrice()));
        TableColumn<MenuItem, String> availability = new TableColumn<>("Availability");
        availability.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isAvailable() ? "Available" : "Unavailable"));
        menuTable.getColumns().setAll(id, name, price, availability);
        menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        menuTable.setPlaceholder(new Label("Select a restaurant to view its menu."));
    }

    private void refreshAll() {
        restaurantTable.setItems(FXCollections.observableArrayList(store.restaurants()));
        Restaurant selected = restaurantFilter.getValue();
        restaurantFilter.setItems(FXCollections.observableArrayList(store.restaurants()));
        if (selected != null) {
            restaurantFilter.getItems().stream()
                    .filter(r -> r.getId().equals(selected.getId()))
                    .findFirst().ifPresent(restaurantFilter::setValue);
        }
        refreshMenu();
    }

    private void refreshMenu() {
        Restaurant selected = restaurantFilter.getValue();
        List<MenuItem> items = selected == null
                ? List.of() : store.menuItemsForRestaurant(selected.getId());
        menuTable.setItems(FXCollections.observableArrayList(items));
    }

    private void addRestaurant() {
        RestaurantForm form = restaurantForm(null);
        form.dialog.setTitle("Add Restaurant");
        form.dialog.showAndWait().ifPresent(values -> {
            try {
                String id = "R" + String.format("%03d", RESTAURANT_IDS.incrementAndGet());
                store.addRestaurant(new Restaurant(id, values.name, values.open,
                        values.inZone, values.minimum, values.distance));
                refreshAll();
            } catch (RuntimeException ex) { error(ex.getMessage()); }
        });
    }

    private void editRestaurant() {
        Restaurant selected = restaurantTable.getSelectionModel().getSelectedItem();
        if (selected == null) { info("Select a restaurant first."); return; }
        RestaurantForm form = restaurantForm(selected);
        form.dialog.setTitle("Edit Restaurant");
        form.dialog.showAndWait().ifPresent(values -> {
            try {
                store.updateRestaurant(new Restaurant(selected.getId(), values.name,
                        values.open, values.inZone, values.minimum, values.distance));
                refreshAll();
            } catch (RuntimeException ex) { error(ex.getMessage()); }
        });
    }

    private void deleteRestaurant() {
        Restaurant selected = restaurantTable.getSelectionModel().getSelectedItem();
        if (selected == null) { info("Select a restaurant first."); return; }
        if (confirm("Delete " + selected.getName() + " and all its menu items?")) {
            try { store.deleteRestaurant(selected.getId()); refreshAll(); }
            catch (RuntimeException ex) { error(ex.getMessage()); }
        }
    }

    private void addMenuItem() {
        Restaurant restaurant = restaurantFilter.getValue();
        if (restaurant == null) { info("Select a restaurant first."); return; }
        MenuForm form = menuForm(null);
        form.dialog.setTitle("Add Menu Item");
        form.dialog.showAndWait().ifPresent(values -> {
            try {
                String id = "M" + String.format("%03d", MENU_IDS.incrementAndGet());
                store.addMenuItem(restaurant.getId(), new MenuItem(id, values.name, values.price, values.available));
                refreshMenu();
            } catch (RuntimeException ex) { error(ex.getMessage()); }
        });
    }

    private void editMenuItem() {
        Restaurant restaurant = restaurantFilter.getValue();
        MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (restaurant == null || selected == null) { info("Select a restaurant and menu item first."); return; }
        MenuForm form = menuForm(selected);
        form.dialog.setTitle("Edit Menu Item");
        form.dialog.showAndWait().ifPresent(values -> {
            try {
                store.updateMenuItem(restaurant.getId(), new MenuItem(selected.getId(), values.name, values.price, values.available));
                refreshMenu();
            } catch (RuntimeException ex) { error(ex.getMessage()); }
        });
    }

    private void deleteMenuItem() {
        Restaurant restaurant = restaurantFilter.getValue();
        MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (restaurant == null || selected == null) { info("Select a menu item first."); return; }
        if (confirm("Delete " + selected.getName() + "?")) {
            try { store.deleteMenuItem(restaurant.getId(), selected.getId()); refreshMenu(); }
            catch (RuntimeException ex) { error(ex.getMessage()); }
        }
    }

    private void toggleAvailability() {
        Restaurant restaurant = restaurantFilter.getValue();
        MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (restaurant == null || selected == null) { info("Select a menu item first."); return; }
        store.updateMenuItem(restaurant.getId(), new MenuItem(selected.getId(), selected.getName(),
                selected.getPrice(), !selected.isAvailable()));
        refreshMenu();
    }

    private RestaurantForm restaurantForm(Restaurant current) {
        Dialog<RestaurantValues> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField name = new TextField(current == null ? "" : current.getName());
        TextField minimum = new TextField(current == null ? "0" : String.valueOf(current.getMinimumOrderValue()));
        TextField distance = new TextField(current == null ? "0" : String.valueOf(current.getDeliveryDistance()));
        CheckBox open = new CheckBox("Currently open"); open.setSelected(current == null || current.isOpen());
        CheckBox zone = new CheckBox("Within delivery zone"); zone.setSelected(current == null || current.isWithinDeliveryZone());
        GridPane grid = dialogGrid();
        grid.addRow(0, new Label("Restaurant name"), name);
        grid.addRow(1, new Label("Minimum order value"), minimum);
        grid.addRow(2, new Label("Delivery distance (km)"), distance);
        grid.add(open, 1, 3); grid.add(zone, 1, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            try {
                double min = Double.parseDouble(minimum.getText().trim());
                double km = Double.parseDouble(distance.getText().trim());
                if (name.getText().trim().isEmpty() || !Double.isFinite(min) || min < 0 || !Double.isFinite(km) || km < 0) throw new IllegalArgumentException();
                return new RestaurantValues(name.getText().trim(), open.isSelected(), zone.isSelected(), min, km);
            } catch (RuntimeException ex) { error("Enter a name and valid non-negative numeric values."); return null; }
        });
        return new RestaurantForm(dialog);
    }

    private MenuForm menuForm(MenuItem current) {
        Dialog<MenuValues> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField name = new TextField(current == null ? "" : current.getName());
        TextField price = new TextField(current == null ? "" : String.valueOf(current.getPrice()));
        CheckBox available = new CheckBox("Available for ordering"); available.setSelected(current == null || current.isAvailable());
        GridPane grid = dialogGrid();
        grid.addRow(0, new Label("Item name"), name);
        grid.addRow(1, new Label("Price"), price);
        grid.add(available, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            try {
                double amount = Double.parseDouble(price.getText().trim());
                if (name.getText().trim().isEmpty() || !Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException();
                return new MenuValues(name.getText().trim(), amount, available.isSelected());
            } catch (RuntimeException ex) { error("Enter an item name and a valid non-negative price."); return null; }
        });
        return new MenuForm(dialog);
    }

    private GridPane dialogGrid() {
        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(18));
        return grid;
    }

    private Button actionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #103B36; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 14;");
        button.setOnAction(e -> action.run());
        return button;
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Please confirm");
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void info(String message) { new Alert(Alert.AlertType.INFORMATION, message).showAndWait(); }
    private void error(String message) { new Alert(Alert.AlertType.ERROR, message == null ? "Operation failed." : message).showAndWait(); }

    private record RestaurantValues(String name, boolean open, boolean inZone, double minimum, double distance) { }
    private record MenuValues(String name, double price, boolean available) { }
    private record RestaurantForm(Dialog<RestaurantValues> dialog) { }
    private record MenuForm(Dialog<MenuValues> dialog) { }
}
