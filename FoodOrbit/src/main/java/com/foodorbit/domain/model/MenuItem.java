
package com.foodorbit.domain.model;

/**
 * Represents a menu item offered by a restaurant.
 */
public class MenuItem {

    /** The unique identifier of the menu item. */
    private final String id;

    /** The name of the menu item. */
    private final String name;

    /** The price of the menu item. */
    private final double price;

    /** Indicates whether the menu item is currently available. */
    private final boolean available;

    /**
     * Creates a menu item with the specified information.
     *
     * @param id the unique menu item identifier
     * @param name the menu item name
     * @param price the item price
     * @param available whether the item is currently available
     * @throws IllegalArgumentException if the ID or name is empty,
     *         or if the price is negative or not finite
     */
    public MenuItem(String id, String name, double price, boolean available) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Menu item ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Menu item name cannot be empty.");
        }
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException(
                    "Menu item price must be finite and non-negative.");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.price = price;
        this.available = available;
    }

    /**
     * Returns the menu item identifier.
     *
     * @return the item ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the menu item name.
     *
     * @return the item name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the menu item price.
     *
     * @return the item price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Checks whether the item is available for ordering.
     *
     * @return true if the item is available
     */
    public boolean isAvailable() {
        return available;
    }
}
