
package com.foodorbit.application.order;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.foodorbit.application.payment.CashPaymentService;
import com.foodorbit.domain.model.Order;
import com.foodorbit.domain.model.OrderItem;
import com.foodorbit.domain.model.OrderStatus;
import com.foodorbit.domain.order.OrderType;
import com.foodorbit.application.pricing.PricingStrategy;
/**
 * Provides services for creating, pricing, and managing customer orders.
 */
public class OrderService {

    /**
     * Stores all orders registered in the system.
     */
    private final List<Order> orders;

    /**
     * Clock used to validate scheduled delivery times.
     */
    private final Clock clock;

    /**
     * Service responsible for calculating order prices and delivery fees.
     */
    private final PricingService pricingService;

    /**
     * Creates an order service using the system clock
     * and a zero delivery rate.
     */
    public OrderService() {
        this(Clock.systemDefaultZone(), 0.0);
    }

    /**
     * Creates an order service using the supplied clock
     * and a zero delivery rate.
     *
     * @param clock clock used to validate scheduled delivery times
     */
    public OrderService(Clock clock) {
        this(clock, 0.0);
    }

    /**
     * Creates an order service with configurable delivery fees.
     *
     * @param clock clock used to validate scheduled delivery times
     * @param deliveryRatePerKilometer delivery cost per kilometer
     * @param expressDeliveryFee additional fee for express orders
     */
    public OrderService(
            Clock clock,
            double deliveryRatePerKilometer,
            double expressDeliveryFee) {

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock cannot be null.");
        }

        this.orders = new ArrayList<>();
        this.clock = clock;
        this.pricingService = new PricingService(
                deliveryRatePerKilometer,
                expressDeliveryFee);
    }

    /**
     * Creates an order service with a configurable delivery rate.
     *
     * @param clock clock used to validate scheduled delivery times
     * @param deliveryRatePerKilometer delivery cost per kilometer
     * @throws IllegalArgumentException if the clock is null
     *         or the delivery rate is invalid
     */
    public OrderService(
            Clock clock, double deliveryRatePerKilometer) {

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock cannot be null.");
        }

        this.orders = new ArrayList<>();
        this.clock = clock;
        this.pricingService =
                new PricingService(deliveryRatePerKilometer);
    }

    /**
     * Adds an order after validating its requirements.
     *
     * @param order the order to add
     * @throws IllegalArgumentException if the order is invalid
     */
    public void addOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null.");
        }

        if (order.getId() == null
                || order.getId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Order ID cannot be empty.");
        }

        if (findOrderById(order.getId()) != null) {
            throw new IllegalArgumentException(
                    "An order with this ID already exists.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "A new order must have Pending status.");
        }

        if (order.getOrderType() == null) {
            throw new IllegalArgumentException(
                    "Order type cannot be null.");
        }

        if (order.getOrderType() == OrderType.SCHEDULED) {
            LocalDateTime scheduledTime =
                    order.getScheduledDeliveryTime();
            LocalDateTime currentTime = LocalDateTime.now(clock);

            if (scheduledTime == null
                    || !scheduledTime.isAfter(currentTime)) {
                throw new IllegalArgumentException(
                        "Scheduled delivery time must be in the future.");
            }
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Order cannot be empty.");
        }

        if (!order.getRestaurant().isOpen()) {
            throw new IllegalArgumentException(
                    "Restaurant is closed.");
        }

        if (!order.getRestaurant().isWithinDeliveryZone()) {
            throw new IllegalArgumentException(
                    "Restaurant is outside the delivery zone.");
        }

        for (OrderItem item : order.getItems()) {
            if (!item.getMenuItem().isAvailable()) {
                throw new IllegalArgumentException(
                        "Menu item is not available: "
                                + item.getMenuItem().getName());
            }
        }

        if (order.getTotal()
                < order.getRestaurant().getMinimumOrderValue()) {
            throw new IllegalArgumentException(
                    "Order does not meet the minimum order value.");
        }

        orders.add(order);
    }

    /**
     * Calculates the order food total using a pricing strategy.
     * Delivery fees are not included.
     *
     * @param order the order to price
     * @param strategy the pricing strategy to apply
     * @return adjusted food total
     */
    public double calculatePricedTotal(
            Order order, PricingStrategy strategy) {
        return pricingService.calculatePrice(order, strategy);
    }

    /**
     * Calculates the delivery fee based on delivery distance.
     *
     * @param order the order to calculate
     * @return delivery fee
     */
    public double calculateDeliveryFee(Order order) {
        return pricingService.calculateDeliveryFee(order);
    }

    /**
     * Calculates the final bill, including the adjusted food total
     * and delivery fee exactly once.
     *
     * @param order the order to calculate
     * @param strategy the pricing strategy to apply
     * @return final bill
     */
    public double calculateFinalTotal(
            Order order, PricingStrategy strategy) {
        return pricingService.calculateFinalTotal(order, strategy);
    }

    /**
     * Returns all registered orders.
     *
     * @return an unmodifiable list of orders
     */
    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(orders);
    }

    /**
     * Finds an order by its identifier.
     *
     * @param orderId the order identifier
     * @return the matching order, or null if not found
     */
    public Order findOrderById(String orderId) {
        if (orderId == null) {
            return null;
        }

        for (Order order : orders) {
            if (order.getId().equals(orderId)) {
                return order;
            }
        }

        return null;
    }

    /**
     * Changes an order to a valid next lifecycle state.
     *
     * @param orderId the identifier of the order
     * @param newStatus the requested status
     * @throws IllegalArgumentException if the order is not found,
     *         the status is null, or the transition is invalid
     */
    public void changeOrderStatus(
            String orderId, OrderStatus newStatus) {

        Order order = findOrderById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found.");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "New status cannot be null.");
        }

        order.setStatus(newStatus);
    }

    /**
     * Cancels an order only while it is pending.
     *
     * @param orderId the identifier of the order
     * @throws IllegalArgumentException if the order is not found
     * @throws IllegalStateException if the order is not pending
     */
    public void cancelOrder(String orderId) {
        Order order = findOrderById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending orders can be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
    }
    public boolean processCashPayment(
            Order order,
            PricingStrategy strategy,
            double paidAmount) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null");
        }

        double finalTotal = calculateFinalTotal(order, strategy);

        CashPaymentService paymentService =
                new CashPaymentService();

        return paymentService.processPayment(
                finalTotal, paidAmount);
    }
}
