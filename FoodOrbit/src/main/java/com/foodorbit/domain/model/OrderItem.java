
package com.foodorbit.domain.model;

/**
 * Represents a menu item and its quantity within an order.
 */
public class OrderItem {

    /** The menu item included in the order. */
    private final MenuItem menuItem;

    /** The quantity of the menu item ordered. */
    private final int quantity;

    /** The unit price captured when the order item was created. */
    private final double unitPrice;

    /**
     * Creates an order item using the menu item's current price.
     *
     * @param menuItem the menu item being ordered
     * @param quantity the quantity ordered
     * @throws IllegalArgumentException if the item is null,
     *         the quantity is not positive, or the price is negative
     */
    public OrderItem(MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new IllegalArgumentException(
                    "Menu item cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }
        if (!Double.isFinite(menuItem.getPrice())
                || menuItem.getPrice() < 0) {
            throw new IllegalArgumentException(
                    "Menu item price must be finite and non-negative.");
        }
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.unitPrice = menuItem.getPrice();
    }

    /**
     * Returns the menu item.
     *
     * @return the menu item
     */
    public MenuItem getMenuItem() {
        return menuItem;
    }

    /**
     * Returns the ordered quantity.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the unit price captured when the order item was created.
     *
     * @return the unit price
     */
    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Calculates the subtotal for this order item.
     *
     * @return unit price multiplied by quantity
     * @throws IllegalArgumentException if the subtotal is not finite
     */
    public double getSubtotal() {
        double subtotal = unitPrice * quantity;
        if (!Double.isFinite(subtotal)) {
            throw new IllegalArgumentException(
                    "Order item subtotal must be finite.");
        }
        return subtotal;
    }
}
