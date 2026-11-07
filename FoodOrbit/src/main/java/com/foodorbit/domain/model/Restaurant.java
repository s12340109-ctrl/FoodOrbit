
package com.foodorbit.domain.model;

/**
 * Represents a restaurant in the SufraGo food delivery system.
 */
public class Restaurant {

    /** The unique identifier of the restaurant. */
    private final String id;

    /** The name of the restaurant. */
    private final String name;

    /** Indicates whether the restaurant is currently open. */
    private final boolean open;

    /** Indicates whether the restaurant serves the delivery zone. */
    private final boolean withinDeliveryZone;

    /** The minimum order value required by the restaurant. */
    private final double minimumOrderValue;

    /** The delivery distance from the restaurant in kilometers. */
    private final double deliveryDistance;

    /**
     * Creates a restaurant using the existing constructor.
     * The delivery distance defaults to zero until specified.
     */
    public Restaurant(String id, String name, boolean open,
                      boolean withinDeliveryZone,
                      double minimumOrderValue) {
        this(id, name, open, withinDeliveryZone,
                minimumOrderValue, 0.0);
    }

    /**
     * Creates a restaurant with delivery distance information.
     *
     * @param id the unique restaurant identifier
     * @param name the restaurant name
     * @param open whether the restaurant is currently open
     * @param withinDeliveryZone whether the restaurant serves the delivery zone
     * @param minimumOrderValue the minimum order value required
     * @param deliveryDistance delivery distance in kilometers
     */
    public Restaurant(String id, String name, boolean open,
                      boolean withinDeliveryZone,
                      double minimumOrderValue,
                      double deliveryDistance) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Restaurant ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Restaurant name cannot be empty.");
        }
        if (!Double.isFinite(minimumOrderValue)
                || minimumOrderValue < 0) {
            throw new IllegalArgumentException(
                    "Minimum order value must be finite and non-negative.");
        }
        if (!Double.isFinite(deliveryDistance)
                || deliveryDistance < 0) {
            throw new IllegalArgumentException(
                    "Delivery distance must be finite and non-negative.");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.open = open;
        this.withinDeliveryZone = withinDeliveryZone;
        this.minimumOrderValue = minimumOrderValue;
        this.deliveryDistance = deliveryDistance;
    }

    /**
     * Returns the restaurant identifier.
     *
     * @return the restaurant ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the restaurant name.
     *
     * @return the restaurant name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether the restaurant is currently open.
     *
     * @return true if the restaurant is open
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Checks whether the restaurant serves the delivery zone.
     *
     * @return true if the restaurant is within the delivery zone
     */
    public boolean isWithinDeliveryZone() {
        return withinDeliveryZone;
    }

    /**
     * Returns the minimum order value required by the restaurant.
     *
     * @return the minimum order value
     */
    public double getMinimumOrderValue() {
        return minimumOrderValue;
    }

    /**
     * Returns the delivery distance in kilometers.
     *
     * @return delivery distance in kilometers
     */
    public double getDeliveryDistance() {
        return deliveryDistance;
    }

}
