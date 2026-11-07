
package com.foodorbit.domain.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void shouldCreateOrderItemSuccessfully() {

        MenuItem menuItem = new MenuItem(
            "M001", "Pizza", 25.0, true
        );

        OrderItem orderItem = new OrderItem(menuItem, 2);

        assertSame(menuItem, orderItem.getMenuItem());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(25.0, orderItem.getUnitPrice(), 0.001);
        assertEquals(50.0, orderItem.getSubtotal(), 0.001);
    }

@Test
void shouldRejectNullMenuItem() {

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new OrderItem(null, 2)
    );

    assertEquals(
        "Menu item cannot be null.",
        exception.getMessage()
    );
}

@Test
void shouldRejectZeroQuantity() {

    MenuItem menuItem = new MenuItem(
        "M001", "Pizza", 25.0, true
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new OrderItem(menuItem, 0)
    );

    assertEquals(
        "Quantity must be greater than zero.",
        exception.getMessage()
    );
}

@Test
void shouldRejectNegativeQuantity() {

    MenuItem menuItem = new MenuItem(
        "M001", "Pizza", 25.0, true
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new OrderItem(menuItem, -1)
    );

    assertEquals(
        "Quantity must be greater than zero.",
        exception.getMessage()
    );
}

@Test
void shouldRejectNonFiniteSubtotal() {

    MenuItem menuItem = new MenuItem(
        "M002", "Expensive Pizza",
        Double.MAX_VALUE, true
    );

    OrderItem orderItem = new OrderItem(menuItem, 2);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> orderItem.getSubtotal()
    );

    assertEquals(
        "Order item subtotal must be finite.",
        exception.getMessage()
    );
}

@Test
void shouldAcceptZeroPrice() {

    MenuItem menuItem = new MenuItem(
        "M003", "Free Item", 0.0, true
    );

    OrderItem orderItem = new OrderItem(menuItem, 2);

    assertEquals(0.0, orderItem.getUnitPrice(), 0.001);
    assertEquals(0.0, orderItem.getSubtotal(), 0.001);
}

}
