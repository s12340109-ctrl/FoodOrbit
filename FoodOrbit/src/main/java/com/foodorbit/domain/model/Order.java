
package com.foodorbit.domain.model;
import com.foodorbit.domain.order.OrderType;
import com.foodorbit.domain.driver.Driver;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a customer's order in the SufraGo system.
 */
public class Order {

    /** The unique identifier of the order. */
    private final String id;

    /** The customer who placed the order. */
    private final Customer customer;

    /** The restaurant receiving the order. */
    private final Restaurant restaurant;

    /** The address where the order should be delivered. */
    private final String deliveryAddress;

    /** The list of items included in the order. */
    private final List<OrderItem> items;

    /** The current status of the order. */
    private OrderStatus status;

    /** The type of the order, such as standard or scheduled. */
    private OrderType orderType;

    /** The requested delivery time for a scheduled order. */
    private LocalDateTime scheduledDeliveryTime;
    private Driver assignedDriver;

    /**
     * Creates an empty order for a customer and restaurant.
     *
     * @param id the unique order identifier
     * @param customer the customer who placed the order
     * @param restaurant the restaurant receiving the order
     * @param deliveryAddress the customer's delivery address
     * @throws IllegalArgumentException if an argument is invalid
     */
    public Order(
            String id,
            Customer customer,
            Restaurant restaurant,
            String deliveryAddress) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Order ID cannot be empty.");
        }
        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null.");
        }
        if (restaurant == null) {
            throw new IllegalArgumentException(
                    "Restaurant cannot be null.");
        }
        if (deliveryAddress == null
                || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Delivery address cannot be empty.");
        }

        this.id = id.trim();
        this.customer = customer;
        this.restaurant = restaurant;
        this.deliveryAddress = deliveryAddress.trim();
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderType = OrderType.STANDARD;
        this.scheduledDeliveryTime = null;
    }

    /**
     * Returns the order identifier.
     *
     * @return the order ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the customer who placed the order.
     *
     * @return the order customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Returns the restaurant receiving the order.
     *
     * @return the restaurant associated with the order
     */
    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * Returns the delivery address of the order.
     *
     * @return the delivery address
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * Returns the current order status.
     *
     * @return the current order status
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Updates the order status only when the lifecycle transition
     * is allowed.
     *
     * @param status the new status
     * @throws IllegalArgumentException if the status is null
     *         or the transition is not allowed
     */
    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null.");
        }

        boolean validTransition = false;

        switch (this.status) {
            case PENDING:
                validTransition =
                        status == OrderStatus.CONFIRMED
                        || status == OrderStatus.CANCELLED;
                break;

            case CONFIRMED:
                validTransition =
                        status == OrderStatus.PREPARING;
                break;

            case PREPARING:
                validTransition =
                        status == OrderStatus.READY;
                break;

            case READY:
                validTransition =
                        status == OrderStatus.OUT_FOR_DELIVERY;
                break;

            case OUT_FOR_DELIVERY:
                validTransition =
                        status == OrderStatus.DELIVERED;
                break;

            case DELIVERED:
            case CANCELLED:
                validTransition = false;
                break;

            default:
                validTransition = false;
                break;
        }

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid order status transition from "
                    + this.status + " to " + status + ".");
        }

        this.status = status;
    }

    /**
     * Returns the current order type.
     *
     * @return the order type
     */
    public OrderType getOrderType() {
        return orderType;
    }

    /**
     * Sets the order type while the order is pending.
     *
     * Scheduled orders must have a delivery time.
     * Other order types cannot have a scheduled delivery time.
     *
     * @param orderType the type to assign
     * @throws IllegalArgumentException if the type is null
     *         or its scheduled-time configuration is invalid
     * @throws IllegalStateException if the order is not pending
     */
    public void setOrderType(OrderType orderType) {
        if (orderType == null) {
            throw new IllegalArgumentException(
                    "Order type cannot be null.");
        }
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending orders can be modified.");
        }
        if (orderType != OrderType.SCHEDULED
                && scheduledDeliveryTime != null) {
            throw new IllegalArgumentException(
                    "Only scheduled orders can have a scheduled delivery time.");
        }

        this.orderType = orderType;
    }

    /**
     * Returns the scheduled delivery time.
     *
     * @return the scheduled delivery time, or null if not set
     */
    public LocalDateTime getScheduledDeliveryTime() {
        return scheduledDeliveryTime;
    }

    /**
     * Sets the requested scheduled delivery time while pending.
     * The service layer validates that the time is in the future.
     *
     * @param scheduledDeliveryTime the requested delivery time
     * @throws IllegalArgumentException if the time is null for a
     *         scheduled order, or supplied for a non-scheduled order
     * @throws IllegalStateException if the order is not pending
     */
    public void setScheduledDeliveryTime(
            LocalDateTime scheduledDeliveryTime) {

        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending orders can be modified.");
        }
        if (orderType == OrderType.SCHEDULED
                && scheduledDeliveryTime == null) {
            throw new IllegalArgumentException(
                    "Scheduled orders must have a delivery time.");
        }
        if (orderType != OrderType.SCHEDULED
                && scheduledDeliveryTime != null) {
            throw new IllegalArgumentException(
                    "Set the order type to SCHEDULED before setting a delivery time.");
        }

        this.scheduledDeliveryTime = scheduledDeliveryTime;
    }

    /**
     * Adds an item to the order.
     *
     * @param item the order item to add
     * @throws IllegalArgumentException if the item is null or unavailable
     * @throws IllegalStateException if the order is not pending
     */
    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Order item cannot be null.");
        }
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot modify an order that is not pending.");
        }
        if (!item.getMenuItem().isAvailable()) {
            throw new IllegalArgumentException(
                    "Cannot add an unavailable menu item.");
        }

        items.add(item);
    }

    /**
     * Returns an unmodifiable list of order items.
     *
     * @return the order items
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Calculates the total price of all order items.
     *
     * @return the total before delivery fees or discounts
     * @throws IllegalArgumentException if the total is not finite
     */
    public double getTotal() {
        double total = 0.0;

        for (OrderItem item : items) {
            total += item.getSubtotal();

            if (!Double.isFinite(total)) {
                throw new IllegalArgumentException(
                        "Order total must be finite.");
            }
        }

        return total;
    }
    public Driver getAssignedDriver() {
        return assignedDriver;
    }
    public void assignDriver(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }

        if (status != OrderStatus.READY) {
            throw new IllegalStateException(
                    "Driver can only be assigned to a ready order");
        }

        if (assignedDriver != null) {
            throw new IllegalStateException(
                    "Order already has an assigned driver");
        }

        this.assignedDriver = driver;
    }
}
