
package com.foodorbit.application.order;

import com.foodorbit.domain.model.Order;
import com.foodorbit.domain.order.OrderType;
import com.foodorbit.application.pricing.PricingStrategy;

/**
 * Provides pricing operations for food orders and delivery.
 * Supports distance-based fees and additional express delivery fees.
 */
public class PricingService {

    /**
     * Delivery cost charged per kilometer.
     */
    private final double deliveryRatePerKilometer;

    /**
     * Additional delivery fee charged for express orders.
     */
    private final double expressDeliveryFee;

    /**
     * Creates a pricing service with zero delivery rates.
     */
    public PricingService() {
        this(0.0, 0.0);
    }

    /**
     * Creates a pricing service with a configurable distance rate.
     *
     * @param deliveryRatePerKilometer delivery cost per kilometer
     */
    public PricingService(double deliveryRatePerKilometer) {
        this(deliveryRatePerKilometer, 0.0);
    }

    /**
     * Creates a pricing service with configurable delivery fees.
     *
     * @param deliveryRatePerKilometer delivery cost per kilometer
     * @param expressDeliveryFee additional fee for express orders
     * @throws IllegalArgumentException if either fee is invalid
     */
    public PricingService(
            double deliveryRatePerKilometer,
            double expressDeliveryFee) {

        if (!Double.isFinite(deliveryRatePerKilometer)
                || deliveryRatePerKilometer < 0) {
            throw new IllegalArgumentException(
                    "Delivery rate must be finite and non-negative.");
        }

        if (!Double.isFinite(expressDeliveryFee)
                || expressDeliveryFee < 0) {
            throw new IllegalArgumentException(
                    "Express delivery fee must be finite and non-negative.");
        }

        this.deliveryRatePerKilometer = deliveryRatePerKilometer;
        this.expressDeliveryFee = expressDeliveryFee;
    }

    /**
     * Calculates the adjusted food total without delivery fees.
     *
     * @param order the order to calculate
     * @param strategy the pricing strategy to apply
     * @return adjusted food total
     * @throws IllegalArgumentException if the order, strategy,
     *         or calculated price is invalid
     */
    public double calculatePrice(
            Order order, PricingStrategy strategy) {

        validateOrderAndStrategy(order, strategy);

        double basePrice = order.getTotal();
        double finalPrice = strategy.calculatePrice(basePrice);

        if (!Double.isFinite(finalPrice) || finalPrice < 0) {
            throw new IllegalArgumentException(
                    "Calculated price must be finite and non-negative.");
        }

        return finalPrice;
    }

    /**
     * Calculates delivery fees based on distance and order type.
     * Express orders receive an additional delivery fee.
     *
     * @param order the order to calculate
     * @return total delivery fee
     * @throws IllegalArgumentException if the order or fee is invalid
     */
    public double calculateDeliveryFee(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null.");
        }

        if (order.getRestaurant() == null) {
            throw new IllegalArgumentException(
                    "Order restaurant cannot be null.");
        }

        double distance =
                order.getRestaurant().getDeliveryDistance();

        double distanceFee =
                distance * deliveryRatePerKilometer;

        double additionalExpressFee = 0.0;

        if (order.getOrderType() == OrderType.EXPRESS) {
            additionalExpressFee = expressDeliveryFee;
        }

        double totalDeliveryFee =
                distanceFee + additionalExpressFee;

        if (!Double.isFinite(totalDeliveryFee)
                || totalDeliveryFee < 0) {
            throw new IllegalArgumentException(
                    "Delivery fee must be finite and non-negative.");
        }

        return totalDeliveryFee;
    }

    /**
     * Calculates the final bill, including food and delivery fees.
     *
     * @param order the order to calculate
     * @param strategy the pricing strategy to apply to food
     * @return final bill
     */
    public double calculateFinalTotal(
            Order order, PricingStrategy strategy) {

        double foodTotal = calculatePrice(order, strategy);
        double deliveryFee = calculateDeliveryFee(order);
        double finalTotal = foodTotal + deliveryFee;

        if (!Double.isFinite(finalTotal) || finalTotal < 0) {
            throw new IllegalArgumentException(
                    "Final total must be finite and non-negative.");
        }

        return finalTotal;
    }

    /**
     * Validates that the order and pricing strategy are not null.
     *
     * @param order the order to validate
     * @param strategy the pricing strategy to validate
     * @throws IllegalArgumentException if the order or strategy is null
     */
    private void validateOrderAndStrategy(
            Order order, PricingStrategy strategy) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null.");
        }

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Pricing strategy cannot be null.");
        }
    }
}
