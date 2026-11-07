
package com.foodorbit.domain.model;

/**
 * Represents the possible lifecycle states of an order.
 */
public enum OrderStatus {

    /** The order has been created and is awaiting confirmation. */
    PENDING,

    /** The restaurant has confirmed the order. */
    CONFIRMED,

    /** The restaurant is preparing the order. */
    PREPARING,

    /** The order is ready for delivery. */
    READY,

    /** The order is on its way to the customer. */
    OUT_FOR_DELIVERY,

    /** The order has been successfully delivered. */
    DELIVERED,

    /** The order has been cancelled. */
    CANCELLED
}
