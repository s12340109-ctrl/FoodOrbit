
package com.foodorbit.application.strategy;

import com.foodorbit.domain.order.OrderType;

public class DriverStrategyFactory {

    public DriverSelectionStrategy getStrategy(
            OrderType orderType,
            boolean containsIceCream) {

        if (orderType == null) {
            throw new IllegalArgumentException(
                    "Order type cannot be null");
        }

        if (orderType == OrderType.EXPRESS || containsIceCream) {
            return new FastestDriverStrategy();
        }

        return new NearestDriverStrategy();
    }
    public String getSelectionReason(
            OrderType orderType,
            boolean containsIceCream) {

        if (orderType == null) {
            throw new IllegalArgumentException(
                    "Order type cannot be null");
        }

        if (orderType == OrderType.EXPRESS && containsIceCream) {
            return "Express order with ice cream: fastest delivery preferred";
        }

        if (orderType == OrderType.EXPRESS) {
            return "Express order: fastest delivery preferred";
        }

        if (containsIceCream) {
            return "Ice cream order: fastest delivery preferred";
        }

        return "Regular order: nearest driver preferred";
    }
}
