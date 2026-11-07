package com.foodorbit.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.foodorbit.application.auth.AuthenticationService;
import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.model.Manager;
import com.foodorbit.domain.model.User;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FoodOrbitApp extends Application {

    private final BorderPane root = new BorderPane();
    private final StackPane pageHost = new StackPane();
    private final AuthenticationService authenticationService =
            new AuthenticationService();

    private User currentUser;
    private boolean dataListenersRegistered;
    private final Map<String, Button> navigation =
            new LinkedHashMap<>();

    private Label headerTitle;
    private Label headerSubtitle;

    private Label orderCount;
    private Label completedCount;
    private Label revenueValue;
    private Label activeOrdersValue;
    private Label availableDriversValue;
    private Label unreadNotificationsValue;

    private TableView<AppStore.CustomerOrder> recentOrders;

    @Override
    public void start(Stage stage) {

        root.getStyleClass().add("login-shell");

        registerDemoAccounts();

        Scene scene = new Scene(root, 1050, 650);
        scene.getStylesheets().add(
                getClass()
                        .getResource("/css/style.css")
                        .toExternalForm()
        );

        stage.setTitle(
                "FoodOrbit • Delivery Management Studio");
        stage.setMinWidth(850);
        stage.setMinHeight(550);
        stage.setScene(scene);

        showLogin();
        stage.show();
    }

    /** Registers a local manager account for demonstrating the interface. */
    private void registerDemoAccounts() {
        authenticationService.registerUser(
                new Manager("M-001", "manager", "FoodOrbit@2026"));
    }

    private void showLogin() {
        if (pageHost.getParent() instanceof VBox oldWorkspace) {
            oldWorkspace.getChildren().remove(pageHost);
        }
        root.setLeft(null);
        root.setCenter(new LoginPage(authenticationService, this::openWorkspace));
        root.getStyleClass().remove("app-shell");
        if (!root.getStyleClass().contains("login-shell")) {
            root.getStyleClass().add("login-shell");
        }
    }

    private void openWorkspace(User user) {
        currentUser = user;
        root.getStyleClass().remove("login-shell");
        if (!root.getStyleClass().contains("app-shell")) {
            root.getStyleClass().add("app-shell");
        }

        root.setLeft(sidebar());
        root.setCenter(workspace());

        if (!dataListenersRegistered) {
            registerDataListeners();
            dataListenersRegistered = true;
        }

        open("Dashboard");
    }

    private void logout() {
        currentUser = null;
        navigation.clear();
        showLogin();
    }

    private VBox sidebar() {

        VBox side = new VBox(0);
        side.getStyleClass().add("sidebar");
        side.setPrefWidth(252);
        side.setMinWidth(252);

        HBox logo = new HBox(12);
        logo.setAlignment(Pos.CENTER_LEFT);

        Label mark = new Label("◉");
        mark.getStyleClass().add("logo-mark");

        VBox brandText = new VBox(3);

        Label brand = new Label("FoodOrbit");
        brand.getStyleClass().add("brand-title");

        Label tagline = new Label("DELIVERY STUDIO");
        tagline.getStyleClass().add("brand-subtitle");

        brandText.getChildren().addAll(brand, tagline);
        logo.getChildren().addAll(mark, brandText);
        side.getChildren().add(logo);

        Label group = new Label("WORKSPACE");
        group.getStyleClass().add("sidebar-section");
        side.getChildren().add(group);

        addNav(side, "Dashboard", "▦");
        addNav(side, "Restaurants", "◈");
        addNav(side, "Orders", "▤");

        // Customers can browse and follow their own orders only.
        // Operational tools are available to managers.
        if (isManager()) {
            addNav(side, "Catalog Management", "⚙");
            addNav(side, "Drivers", "◉");
            addNav(side, "Payments", "▣");
        }
        addNav(side, "Notifications", "♧");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        side.getChildren().add(spacer);

        VBox bottom = new VBox(7);
        bottom.getStyleClass().add("sidebar-bottom");

        Label environment = new Label("●  LOCAL DEMO");
        environment.getStyleClass().add("environment-label");

        Label note = new Label("JavaFX 21  •  Java 17");
        note.getStyleClass().add("sidebar-note");

        bottom.getChildren().addAll(environment, note);
        side.getChildren().add(bottom);

        return side;
    }

    private void addNav(VBox side, String name, String symbol) {

        Button button = new Button(symbol + "     " + name);

        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> open(name));

        navigation.put(name, button);
        side.getChildren().add(button);
    }

    private VBox workspace() {

        VBox area = new VBox(0);
        area.getStyleClass().add("workspace");

        HBox topbar = new HBox(10);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);

        VBox heading = new VBox(4);

        headerTitle = new Label("Dashboard");
        headerTitle.getStyleClass().add("topbar-title");

        headerSubtitle = new Label(
                "Your food delivery operations at a glance");

        headerSubtitle.getStyleClass().add("topbar-subtitle");

        heading.getChildren().addAll(
                headerTitle, headerSubtitle);

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);

        Label today = new Label(
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd MMM yyyy"))
        );

        today.getStyleClass().add("date-pill");

        Label account = new Label(
                currentUser.getUsername() + "  •  "
                + currentUser.getRole());
        account.getStyleClass().add("user-account-label");

        Label avatar = new Label(
                currentUser.getUsername().substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("user-avatar");

        Button logoutButton = new Button("Log out");
        logoutButton.setId("logoutButton");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(event -> logout());

        topbar.getChildren().addAll(
                heading, gap, today, account, avatar, logoutButton);

        VBox.setVgrow(pageHost, Priority.ALWAYS);
        pageHost.getStyleClass().add("page-host");

        area.getChildren().addAll(topbar, pageHost);

        return area;
    }

    private boolean isManager() {
        return currentUser != null
                && "MANAGER".equalsIgnoreCase(currentUser.getRole());
    }

    private void open(String name) {

        if (!isManager()
                && !(name.equals("Dashboard")
                || name.equals("Restaurants")
                || name.equals("Orders")
                || name.equals("Notifications"))) {
            name = "Dashboard";
        }

        final String selectedPage = name;

        navigation.forEach((key, button) -> {

            button.getStyleClass()
                    .remove("sidebar-button-active");

            if (key.equals(selectedPage)) {
                button.getStyleClass()
                        .add("sidebar-button-active");
            }
        });

        headerTitle.setText(selectedPage);

        headerSubtitle.setText(
                switch (selectedPage) {
                    case "Restaurants" ->
                            "Discover menus, manage your cart and place orders";
                    case "Orders" ->
                            "Review orders and follow delivery progress";
                    case "Catalog Management" ->
                            "Add, update and manage restaurants and menu items";
                    case "Drivers" ->
                            "Manage your fleet and delivery team";
                    case "Payments" ->
                            "Track payments and transaction information";
                    case "Notifications" ->
                            "Stay up to date with your operations";
                    default ->
                            "Your food delivery operations at a glance";
                }
        );

        Node page = switch (selectedPage) {
            case "Restaurants" -> isManager()
                    ? new RestaurantsPage()
                    : new RestaurantsPage(currentUser.getUsername());

            case "Orders" -> isManager()
                    ? new OrdersPage()
                    : new OrdersPage(currentUser.getUsername());

            case "Catalog Management" -> new CatalogManagementPage();

            case "Drivers" -> new DriversPage();

            case "Payments" -> new PaymentsPage();

            case "Notifications" -> isManager()
                    ? new NotificationsPage()
                    : new NotificationsPage(currentUser.getUsername());

            default -> dashboard();
        };

        pageHost.getChildren().setAll(page);

        if (page instanceof Region region) {
            region.setMaxSize(
                    Double.MAX_VALUE,
                    Double.MAX_VALUE);
        }

        if (selectedPage.equals("Dashboard")) {
            refreshStats();
        }
    }
    private Node dashboard() {

        VBox body = new VBox(24);
        body.getStyleClass().add("dashboard-body");

        HBox hero = new HBox(20);
        hero.getStyleClass().add("hero-card");
        hero.setAlignment(Pos.CENTER_LEFT);

        VBox heroText = new VBox(11);

        Label eyebrow = new Label(
                "✦  YOUR COMMAND CENTER");
        eyebrow.getStyleClass().add("hero-eyebrow");

        Label headline = new Label(isManager()
                ? "Deliver more. Manage smarter."
                : "Good food is just around the corner.");
        headline.getStyleClass().add("hero-title");
        headline.setWrapText(true);

        Label description = new Label(isManager()
                ? "Everything you need to run your food delivery workflow, all in one place."
                : "Explore restaurants, place an order and follow its progress from here.");

        description.getStyleClass()
                .add("hero-description");
        description.setWrapText(true);

        Button explore = new Button(
                "Explore restaurants  →");
        explore.getStyleClass().add("hero-button");
        explore.setOnAction(e -> open("Restaurants"));

        heroText.getChildren().addAll(
                eyebrow,
                headline,
                description,
                explore
        );

        HBox.setHgrow(heroText, Priority.ALWAYS);

        Label decoration = new Label("◉");
        decoration.getStyleClass().add("hero-decoration");

        hero.getChildren().addAll(
                heroText, decoration);

        orderCount = new Label();
        completedCount = new Label();
        revenueValue = new Label();

        HBox stats = new HBox(16);
        if (isManager()) {
            stats.getChildren().addAll(
                    statCard("TOTAL ORDERS", orderCount,
                            "All customer orders", "▤"),
                    statCard("COMPLETED", completedCount,
                            "Delivered orders", "✓"),
                    statCard("REVENUE", revenueValue,
                            "Collected payments", "₪"));
        } else {
            stats.getChildren().addAll(
                    statCard("MY ORDERS", orderCount,
                            "Orders you have placed", "▤"),
                    statCard("DELIVERED", completedCount,
                            "Successfully delivered", "✓"),
                    statCard("ACTIVE", revenueValue,
                            "Currently in progress", "◷"));
        }

        for (Node card : stats.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        activeOrdersValue = new Label();
        availableDriversValue = new Label();
        unreadNotificationsValue = new Label();

        HBox extraStats = new HBox(16);

        if (isManager()) {
            extraStats.getChildren().addAll(
                statCard(
                        "ACTIVE ORDERS",
                        activeOrdersValue,
                        "Orders in progress",
                        "◷"
                ),
                statCard(
                        "AVAILABLE DRIVERS",
                        availableDriversValue,
                        "Ready for delivery",
                        "◉"
                ),
                statCard(
                        "UNREAD ALERTS",
                        unreadNotificationsValue,
                        "Notifications to review",
                        "♧"
                )
        );

            for (Node card : extraStats.getChildren()) {
                HBox.setHgrow(card, Priority.ALWAYS);
            }
        }

        HBox sectionHeader = new HBox(15);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);

        Label recentTitle = new Label("Recent orders");
        recentTitle.getStyleClass().add("section-heading");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Button viewAll = new Button("View all orders  →");
        viewAll.getStyleClass().add("link-button");
        viewAll.setOnAction(e -> open("Orders"));

        sectionHeader.getChildren().addAll(
                recentTitle, space, viewAll);

        recentOrders = new TableView<>();
        recentOrders.getStyleClass().add("recent-table");

        recentOrders.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        recentOrders.setPlaceholder(
                new Label(
                        "No orders yet. Start by ordering from Restaurants.")
        );

        recentOrders.setPrefHeight(300);

        recentOrders.getColumns().addAll(
                orderColumn(
                        "ORDER ID",
                        order -> order.id
                ),
                orderColumn(
                        "CUSTOMER",
                        order -> order.customer
                ),
                orderColumn(
                        "RESTAURANT",
                        order -> order.restaurant
                ),
                orderColumn(
                        "STATUS",
                        AppStore.CustomerOrder::getStatus
                ),
                orderColumn(
                        "TOTAL",
                        order -> String.format(
                                "₪%.2f", order.total)
                )
        );

        if (isManager()) {
            recentOrders.setItems(AppStore.get().orders());
        } else {
            javafx.collections.transformation.FilteredList<AppStore.CustomerOrder> myOrders =
                    new javafx.collections.transformation.FilteredList<>(
                            AppStore.get().orders(),
                            order -> order.customer.equalsIgnoreCase(
                                    currentUser.getUsername()));
            recentOrders.setItems(myOrders);
        }

        VBox tableContainer = new VBox(recentOrders);
        tableContainer.getStyleClass()
                .add("table-container");

        body.getChildren().addAll(
                hero,
                stats,
                extraStats,
                sectionHeader,
                tableContainer
        );

        refreshStats();

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER);

        scroll.getStyleClass()
                .add("dashboard-scroll");

        return scroll;
    }

    private TableColumn<AppStore.CustomerOrder, String>
            orderColumn(
                    String title,
                    java.util.function.Function<
                            AppStore.CustomerOrder, String> getter) {

        TableColumn<AppStore.CustomerOrder, String> col =
                new TableColumn<>(title);

        col.setCellValueFactory(data ->
                new SimpleStringProperty(
                        getter.apply(data.getValue()))
        );

        return col;
    }

    private VBox statCard(
            String heading,
            Label value,
            String caption,
            String icon) {

        VBox card = new VBox(9);
        card.getStyleClass().add("stat-card");

        card.setMinWidth(145);
        card.setMaxWidth(Double.MAX_VALUE);

        HBox first = new HBox(8);
        first.setAlignment(Pos.CENTER_LEFT);

        Label headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("stat-heading");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label symbol = new Label(icon);
        symbol.getStyleClass().add("stat-icon");

        first.getChildren().addAll(
                headingLabel, spacer, symbol);

        value.getStyleClass().add("stat-number");

        Label sub = new Label(caption);
        sub.getStyleClass().add("stat-caption");

        card.getChildren().addAll(first, value, sub);

        return card;
    }

    private void registerDataListeners() {

        AppStore store = AppStore.get();

        store.orders().addListener(
                (ListChangeListener<AppStore.CustomerOrder>)
                        change -> refreshStats()
        );

        store.payments().addListener(
                (ListChangeListener<AppStore.PaymentRecord>)
                        change -> refreshStats()
        );

        store.notifications().addListener(
                (ListChangeListener<AppStore.NotificationRecord>)
                        change -> refreshStats()
        );

        store.drivers().addListener(
                (ListChangeListener<Driver>)
                        change -> refreshStats()
        );
    }

    private void refreshStats() {

        if (orderCount == null
                || completedCount == null
                || revenueValue == null
                || activeOrdersValue == null
                || availableDriversValue == null
                || unreadNotificationsValue == null) {
            return;
        }

        AppStore store = AppStore.get();

        java.util.List<AppStore.CustomerOrder> visibleOrders = isManager()
                ? new java.util.ArrayList<>(store.orders())
                : store.orders().stream()
                    .filter(order -> order.customer.equalsIgnoreCase(
                            currentUser.getUsername()))
                    .toList();

        long totalOrders = visibleOrders.size();

        long delivered = visibleOrders.stream()
                .filter(order ->
                        "Delivered".equals(
                                order.getStatus()))
                .count();

        long activeOrders = visibleOrders.stream()
                .filter(order ->
                        !"Delivered".equals(
                                order.getStatus())
                        && !"Cancelled".equals(
                                order.getStatus()))
                .count();

        long availableDrivers = isManager() ? store.drivers().stream()
                .filter(Driver::isAvailable)
                .count() : 0;

        long unreadNotifications = store.notifications().stream()
                        .filter(notification -> isManager()
                                || notification.recipient.equalsIgnoreCase(
                                        currentUser.getUsername()))
                        .filter(notification -> "Unread".equals(
                                notification.getStatus()))
                        .count();

        double collectedRevenue = isManager() ? store.payments().stream()
                .filter(payment ->
                        "Completed".equals(
                                payment.getStatus()))
                .mapToDouble(payment -> payment.amount)
                .sum() : 0.0;

        orderCount.setText(String.valueOf(totalOrders));
        completedCount.setText(String.valueOf(delivered));
        revenueValue.setText(isManager()
                ? String.format("₪%.2f", collectedRevenue)
                : String.valueOf(activeOrders));

        if (isManager()) {
            activeOrdersValue.setText(String.valueOf(activeOrders));
            availableDriversValue.setText(String.valueOf(availableDrivers));
            unreadNotificationsValue.setText(String.valueOf(unreadNotifications));
        }

        if (recentOrders != null) {
            recentOrders.refresh();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}