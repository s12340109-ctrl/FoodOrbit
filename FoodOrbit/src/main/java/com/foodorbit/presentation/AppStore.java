package com.foodorbit.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Clock;
import java.time.LocalDateTime;

import com.foodorbit.application.catalog.MenuItemService;
import com.foodorbit.application.catalog.RestaurantService;
import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.model.Restaurant;
import com.foodorbit.persistence.MenuItemRepository;
import com.foodorbit.persistence.RestaurantRepository;

import com.foodorbit.application.payment.CashPaymentService;
import com.foodorbit.application.strategy.DriverSelectionStrategy;
import com.foodorbit.application.strategy.FastestDriverStrategy;
import com.foodorbit.application.strategy.NearestDriverStrategy;
import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.driver.DriverStatus;
import com.foodorbit.domain.driver.MotorcycleDriver;
import com.foodorbit.domain.driver.ElectricVehicleDriver;

import com.foodorbit.domain.order.OrderType;
import com.foodorbit.domain.order.OrderTypeValidator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class AppStore {

    private static final AppStore INSTANCE = new AppStore();

    private final AtomicInteger nextOrderId =
            new AtomicInteger(1001);

    private final ObservableList<CustomerOrder> orders =
            FXCollections.observableArrayList();

    private final ObservableList<Driver> drivers =
            FXCollections.observableArrayList();

    private final ObservableList<PaymentRecord> payments =
            FXCollections.observableArrayList();

    private final ObservableList<NotificationRecord> notifications =
            FXCollections.observableArrayList();

    private final CashPaymentService cashPaymentService =
            new CashPaymentService();

    // Shared catalog repositories and services used by the GUI.
    private final RestaurantRepository restaurantRepository =
            new RestaurantRepository();

    private final MenuItemRepository menuItemRepository =
            new MenuItemRepository();

    private final RestaurantService restaurantService =
            new RestaurantService(restaurantRepository);

    private final MenuItemService menuItemService =
            new MenuItemService(menuItemRepository);

    // MenuItem does not contain a restaurant ID in the current domain model,
    // so the association is kept by the application store.
    private final Map<String, List<MenuItem>> menuItemsByRestaurant =
            new LinkedHashMap<>();

    private int nextNotificationId = 1;

    private AppStore() {
        loadDemoDrivers();
        loadDemoCatalog();
    }

    public static AppStore get() {
        return INSTANCE;
    }

    public ObservableList<CustomerOrder> orders() {
        return orders;
    }

    public ObservableList<Driver> drivers() {
        return drivers;
    }

    public ObservableList<PaymentRecord> payments() {
        return payments;
    }

    public ObservableList<NotificationRecord> notifications() {
        return notifications;
    }

    // ---------------------------------------
    // RESTAURANT AND MENU CATALOG
    // ---------------------------------------

    /**
     * Returns all restaurants registered in the catalog.
     *
     * @return all catalog restaurants
     */
    public List<Restaurant> restaurants() {
        return restaurantService.getAllRestaurants();
    }

    /**
     * Returns the menu items associated with one restaurant.
     *
     * @param restaurantId the restaurant ID
     * @return the restaurant menu, or an empty list if it has no menu
     */
    public List<MenuItem> menuItemsForRestaurant(String restaurantId) {
        if (restaurantId == null) {
            return Collections.emptyList();
        }

        List<MenuItem> associatedItems =
                menuItemsByRestaurant.get(restaurantId);

        if (associatedItems == null || associatedItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<MenuItem> result = new ArrayList<>();

        for (MenuItem item : menuItemService.getAllMenuItems()) {
            if (associatedItems.contains(item)) {
                result.add(item);
            }
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Adds a restaurant through the catalog application service.
     *
     * @param restaurant the restaurant to add
     */
    public void addRestaurant(Restaurant restaurant) {
        restaurantService.addRestaurant(restaurant);
    }

    /** Updates a restaurant while keeping its menu association. */
    public void updateRestaurant(Restaurant restaurant) {
        restaurantService.updateRestaurant(restaurant);
    }

    /** Removes a restaurant and all menu items associated with it. */
    public void deleteRestaurant(String restaurantId) {
        if (!restaurantService.deleteRestaurant(restaurantId)) {
            throw new IllegalArgumentException("Restaurant not found.");
        }
        List<MenuItem> removed = menuItemsByRestaurant.remove(restaurantId);
        if (removed != null) {
            for (MenuItem item : removed) {
                menuItemService.deleteMenuItem(item.getId());
            }
        }
    }

    /** Updates a menu item without changing its restaurant association. */
    public void updateMenuItem(String restaurantId, MenuItem updatedItem) {
        List<MenuItem> items = menuItemsByRestaurant.get(restaurantId);
        if (items == null || items.stream().noneMatch(
                item -> item.getId().equals(updatedItem.getId()))) {
            throw new IllegalArgumentException("Menu item not found for this restaurant.");
        }
        menuItemService.updateMenuItem(updatedItem);
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(updatedItem.getId())) {
                items.set(i, updatedItem);
                return;
            }
        }
    }

    /** Deletes a menu item from a restaurant. */
    public void deleteMenuItem(String restaurantId, String menuItemId) {
        List<MenuItem> items = menuItemsByRestaurant.get(restaurantId);
        if (items == null || !items.removeIf(
                item -> item.getId().equals(menuItemId))) {
            throw new IllegalArgumentException("Menu item not found for this restaurant.");
        }
        menuItemService.deleteMenuItem(menuItemId);
    }

    /**
     * Adds a menu item to a restaurant through the catalog service.
     *
     * @param restaurantId the restaurant that owns the item
     * @param menuItem the menu item to add
     */
    public void addMenuItem(String restaurantId, MenuItem menuItem) {
        if (restaurantId == null
                || restaurantRepository.findById(restaurantId).isEmpty()) {
            throw new IllegalArgumentException("Restaurant not found.");
        }

        menuItemService.addMenuItem(menuItem);

        menuItemsByRestaurant
                .computeIfAbsent(restaurantId, key -> new ArrayList<>())
                .add(menuItem);
    }

    private void loadDemoCatalog() {
        Restaurant pizza = new Restaurant(
                "R001", "Pizza House", true, true, 15.0, 5.0);

        Restaurant burgers = new Restaurant(
                "R002", "Burger Station", true, true, 10.0, 3.8);

        Restaurant shawarma = new Restaurant(
                "R003", "Shawarma Corner", true, true, 12.0, 2.5);

        Restaurant healthy = new Restaurant(
                "R004", "Fresh Bowl", true, true, 10.0, 4.0);

        restaurantService.addRestaurant(pizza);
        restaurantService.addRestaurant(burgers);
        restaurantService.addRestaurant(shawarma);
        restaurantService.addRestaurant(healthy);

        addMenuItem("R001", new MenuItem(
                "M001", "Margherita Pizza", 25.0, true));

        addMenuItem("R001", new MenuItem(
                "M002", "Pepperoni Pizza", 35.0, true));

        addMenuItem("R001", new MenuItem(
                "M003", "BBQ Chicken Pizza", 40.0, false));

        addMenuItem("R002", new MenuItem(
                "M004", "Classic Burger", 20.0, true));

        addMenuItem("R002", new MenuItem(
                "M005", "Cheese Burger", 25.0, true));

        addMenuItem("R002", new MenuItem(
                "M006", "Crispy Chicken Burger", 28.0, false));

        addMenuItem("R003", new MenuItem(
                "M007", "Chicken Shawarma", 18.0, true));

        addMenuItem("R003", new MenuItem(
                "M008", "Beef Shawarma", 23.0, false));

        addMenuItem("R003", new MenuItem(
                "M009", "Shawarma Meal", 32.0, true));

        addMenuItem("R004", new MenuItem(
                "M010", "Caesar Salad", 24.0, true));

        addMenuItem("R004", new MenuItem(
                "M011", "Chicken Protein Bowl", 35.0, true));

        addMenuItem("R004", new MenuItem(
                "M012", "Fresh Orange Juice", 12.0, false));
    }

    // ---------------------------------------
    // DEMO DRIVERS
    // ---------------------------------------

    private void loadDemoDrivers() {

        addDriver(new MotorcycleDriver(
                101, "Ahmad", "0599123456", 1, 1));

        addDriver(new ElectricVehicleDriver(
                102, "Omar", "0599123457", 4, 0, 100));

        addDriver(new MotorcycleDriver(
                103, "Khaled", "0599123458", 8, 8));

        Driver yousef = new ElectricVehicleDriver(
                104, "Yousef", "0599123459", 2, 2, 90);

        yousef.updateStatus(DriverStatus.BUSY);
        addDriver(yousef);

        Driver ali = new MotorcycleDriver(
                105, "Ali", "0599123460", 3, 3);

        ali.updateStatus(DriverStatus.OFFLINE);
        addDriver(ali);

        addDriver(new ElectricVehicleDriver(
                106, "Sara", "0599123461", 6, 1, 15));

        addDriver(new ElectricVehicleDriver(
                107, "Lina", "0599123462", 0, 2, 80));
    }

    // ---------------------------------------
    // DRIVER MANAGEMENT
    // ---------------------------------------

    public void addDriver(Driver driver) {

        if (driver == null) {
            throw new IllegalArgumentException(
                    "Driver is required");
        }

        boolean duplicate = drivers.stream()
                .anyMatch(d ->
                        d.getDriverId() == driver.getDriverId());

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Driver ID already exists");
        }

        drivers.add(driver);
    }

    public void removeDriver(Driver driver) {

        if (driver == null) {
            return;
        }

        boolean assigned = orders.stream()
                .anyMatch(order ->
                        order.assignedDriver == driver
                        && !order.status.equals("Delivered")
                        && !order.status.equals("Cancelled"));

        if (assigned) {
            throw new IllegalStateException(
                    "Driver has an active delivery");
        }

        drivers.remove(driver);
    }

    // ---------------------------------------
    // ORDER CREATION
    // ---------------------------------------

    public CustomerOrder placeOrder(
            String customer,
            String phone,
            String address,
            String restaurant,
            String paymentMethod,
            List<String> items,
            double total) {

        return placeOrder(
                customer,
                phone,
                address,
                restaurant,
                paymentMethod,
                items,
                total,
                OrderType.STANDARD,
                null);
    }

    /**
     * Creates an order with its selected order type
     * and optional scheduled time.
     */
    public CustomerOrder placeOrder(
            String customer,
            String phone,
            String address,
            String restaurant,
            String paymentMethod,
            List<String> items,
            double total,
            OrderType orderType,
            LocalDateTime scheduledDeliveryTime) {

        if (customer == null || customer.isBlank()
                || phone == null || phone.isBlank()
                || address == null || address.isBlank()
                || restaurant == null || restaurant.isBlank()
                || paymentMethod == null || paymentMethod.isBlank()
                || items == null || items.isEmpty()
                || !Double.isFinite(total)
                || total < 0) {

            throw new IllegalArgumentException(
                    "Invalid order details");
        }

        new OrderTypeValidator().validate(
                orderType,
                scheduledDeliveryTime,
                Clock.systemDefaultZone());

        CustomerOrder order = new CustomerOrder(
                "FO-" + nextOrderId.getAndIncrement(),
                customer.trim(),
                phone.trim(),
                address.trim(),
                restaurant.trim(),
                paymentMethod.trim(),
                items,
                total,
                orderType,
                scheduledDeliveryTime
        );

        orders.add(order);

        PaymentRecord payment = new PaymentRecord(
                "PAY-" + order.id,
                order.id,
                order.customer,
                order.paymentMethod,
                order.total
        );

        payments.add(payment);

        notifyOrder(
                order,
                "New " + order.getOrderType().name().toLowerCase()
                + " order placed at " + order.restaurant);

        return order;
    }

    // ---------------------------------------
    // DRIVER ASSIGNMENT
    // ---------------------------------------

    public Driver assignDriver(
            CustomerOrder order,
            String strategyName,
            double pickupX,
            double pickupY,
            double distanceKm,
            double orderWeightKg) {

        if (order == null || !orders.contains(order)) {
            throw new IllegalArgumentException(
                    "Select a valid order");
        }

        if (!order.status.equals("Confirmed")) {
            throw new IllegalStateException(
                    "Only confirmed orders can be assigned");
        }

        if (order.assignedDriver != null) {
            throw new IllegalStateException(
                    "This order already has a driver");
        }

        if (!Double.isFinite(pickupX)
                || !Double.isFinite(pickupY)
                || !Double.isFinite(distanceKm)
                || !Double.isFinite(orderWeightKg)
                || distanceKm < 0
                || orderWeightKg <= 0) {

            throw new IllegalArgumentException(
                    "Invalid location, distance or weight");
        }

        List<Driver> eligibleDrivers = new ArrayList<>();

        for (Driver driver : drivers) {

            if (driver.isAvailable()
                    && driver.canDeliver(
                            distanceKm, orderWeightKg)) {

                eligibleDrivers.add(driver);
            }
        }

        if (eligibleDrivers.isEmpty()) {
            throw new IllegalStateException(
                    "No eligible available drivers");
        }

        DriverSelectionStrategy strategy;

        if ("Nearest".equals(strategyName)) {
            strategy = new NearestDriverStrategy();

        } else if ("Fastest".equals(strategyName)) {
            strategy = new FastestDriverStrategy();

        } else {
            throw new IllegalArgumentException(
                    "Unknown selection strategy");
        }

        Driver selected = strategy.selectDriver(
                eligibleDrivers,
                pickupX,
                pickupY
        );

        if (selected == null) {
            throw new IllegalStateException(
                    "No driver could be selected");
        }

        selected.updateStatus(DriverStatus.BUSY);

        order.assignedDriver = selected;
        order.selectionStrategy = strategyName;

        notifyOrder(order,
                "Driver assigned: " + selected.getName()
                + " (" + strategyName + ")");

        orders.set(orders.indexOf(order), order);

        return selected;
    }

    // ---------------------------------------
    // ORDER STATUS
    // ---------------------------------------

    public void changeOrderStatus(
            CustomerOrder order,
            String newStatus) {

        if (order == null || !orders.contains(order)) {
            throw new IllegalArgumentException(
                    "Invalid order");
        }

        if (order.status.equals("Delivered")
                || order.status.equals("Cancelled")) {

            throw new IllegalStateException(
                    "Order is already finished");
        }

        boolean allowed = switch (newStatus) {

            case "Confirmed" ->
                    order.status.equals("Pending");

            case "Preparing" ->
                    order.status.equals("Confirmed");

            case "Ready" ->
                    order.status.equals("Preparing");

            case "Out for Delivery" ->
                    order.status.equals("Ready")
                    && order.assignedDriver != null;

            case "Delivered" ->
                    order.status.equals("Out for Delivery");

            case "Cancelled" -> order.status.equals("Pending");

            default -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Invalid order status transition");
        }

        order.status = newStatus;

        notifyOrder(order,
                "Order status updated to " + newStatus);

        if (newStatus.equals("Delivered")
                && order.assignedDriver != null) {

            order.assignedDriver.completeDelivery();

            order.assignedDriver.updateStatus(
                    DriverStatus.AVAILABLE);
        }

        if (newStatus.equals("Cancelled")
                && order.assignedDriver != null) {

            order.assignedDriver.updateStatus(
                    DriverStatus.AVAILABLE);
        }

        orders.set(orders.indexOf(order), order);
    }

    // ---------------------------------------
    // PAYMENTS
    // ---------------------------------------

    public void collectCash(
            CustomerOrder order,
            double received) {

        if (order == null || !orders.contains(order)) {
            throw new IllegalArgumentException(
                    "Select an order");
        }

        PaymentRecord payment = payments.stream()
                .filter(p -> p.orderId.equals(order.id))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Payment not found"));

        if (payment.status.equals("Refunded")) {
            throw new IllegalStateException(
                    "Payment has been refunded");
        }

        if (payment.status.equals("Completed")) {
            throw new IllegalStateException(
                    "Payment already completed");
        }

        if (order.getStatus().equals("Cancelled")) {
            throw new IllegalStateException(
                    "Cancelled order cannot be paid");
        }

        if (!cashPaymentService.processPayment(
                payment.amount, received)) {

            throw new IllegalArgumentException(
                    "Cash received is insufficient");
        }

        payment.status = "Completed";

        payments.set(
                payments.indexOf(payment), payment);

        notifyOrder(order,
                "Cash payment collected: ₪"
                + String.format("%.2f", payment.amount));
    }

    public void refundPayment(PaymentRecord payment) {

        if (payment == null
                || !payments.contains(payment)) {

            throw new IllegalArgumentException(
                    "Select a payment");
        }

        if (!payment.status.equals("Completed")) {
            throw new IllegalStateException(
                    "Only completed payments can be refunded");
        }

        payment.status = "Refunded";

        payments.set(
                payments.indexOf(payment), payment);

        orders.stream()
                .filter(order ->
                        order.id.equals(payment.orderId))
                .findFirst()
                .ifPresent(order ->
                        notifyOrder(order, "Payment refunded"));
    }

    // ---------------------------------------
    // NOTIFICATIONS
    // ---------------------------------------

    private void notifyOrder(
            CustomerOrder order,
            String message) {

        NotificationRecord notification =
                new NotificationRecord(
                        "N-" + nextNotificationId++,
                        order.id,
                        order.customer,
                        message
                );

        notifications.add(0, notification);
    }

    // ---------------------------------------
    // CUSTOMER ORDER MODEL
    // ---------------------------------------

    public static final class CustomerOrder {

        public final String id;
        public final String customer;
        public final String phone;
        public final String address;
        public final String restaurant;
        public final String paymentMethod;
        public final List<String> items;
        public final double total;

        public final OrderType orderType;
        public final LocalDateTime scheduledDeliveryTime;

        private String status = "Pending";
        private Driver assignedDriver;
        private String selectionStrategy;

        private CustomerOrder(
                String id,
                String customer,
                String phone,
                String address,
                String restaurant,
                String paymentMethod,
                List<String> items,
                double total,
                OrderType orderType,
                LocalDateTime scheduledDeliveryTime) {

            this.id = id;
            this.customer = customer;
            this.phone = phone;
            this.address = address;
            this.restaurant = restaurant;
            this.paymentMethod = paymentMethod;

            this.items = Collections.unmodifiableList(
                    new ArrayList<>(items));

            this.total = total;
            this.orderType = orderType;
            this.scheduledDeliveryTime = scheduledDeliveryTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {

            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException(
                        "Status required");
            }

            this.status = status;
        }

        public Driver getAssignedDriver() {
            return assignedDriver;
        }

        public String getDriverName() {

            return assignedDriver == null
                    ? "Not Assigned"
                    : assignedDriver.getName();
        }

        public String getSelectionStrategy() {

            return selectionStrategy == null
                    ? "-"
                    : selectionStrategy;
        }

        public OrderType getOrderType() {
            return orderType;
        }

        public LocalDateTime getScheduledDeliveryTime() {
            return scheduledDeliveryTime;
        }
    }

    // ---------------------------------------
    // PAYMENT MODEL
    // ---------------------------------------

    public static final class PaymentRecord {

        public final String id;
        public final String orderId;
        public final String customer;
        public final String method;
        public final double amount;

        private String status = "Pending";

        private PaymentRecord(
                String id,
                String orderId,
                String customer,
                String method,
                double amount) {

            this.id = id;
            this.orderId = orderId;
            this.customer = customer;
            this.method = method;
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }
    }

    // ---------------------------------------
    // NOTIFICATION MODEL
    // ---------------------------------------

    public static final class NotificationRecord {

        public final String id;
        public final String orderId;
        public final String recipient;
        public final String message;

        private boolean read;

        private NotificationRecord(
                String id,
                String orderId,
                String recipient,
                String message) {

            this.id = id;
            this.orderId = orderId;
            this.recipient = recipient;
            this.message = message;
        }

        public String getStatus() {
            return read ? "Read" : "Unread";
        }

        public void setRead(boolean value) {
            read = value;
        }
    }
}