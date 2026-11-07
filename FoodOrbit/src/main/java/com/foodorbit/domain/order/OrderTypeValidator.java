package com.foodorbit.domain.order;

import java.time.Clock;
import java.time.LocalDateTime;

public class OrderTypeValidator {

    public void validate(
            OrderType orderType,
            LocalDateTime scheduledDeliveryTime,
            Clock clock) {

        if (orderType == null) {
            throw new IllegalArgumentException(
                    "Order type cannot be null.");
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock cannot be null.");
        }


if (orderType == OrderType.SCHEDULED) {

    if (scheduledDeliveryTime == null
            || !scheduledDeliveryTime.isAfter(
                    LocalDateTime.now(clock))) {

        throw new IllegalArgumentException(
                "Scheduled delivery time must be in the future.");
    }

} else if (scheduledDeliveryTime != null) {

    throw new IllegalArgumentException(
            "Only scheduled orders can have a delivery time.");
}

    }
}