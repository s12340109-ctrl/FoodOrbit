package com.foodorbit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.foodorbit.domain.order.OrderType;

class DomainModelValidationTest {

    @Test
    void userRejectsEachNullOrBlankRequiredFieldAndTrimsTextFields() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, "u", "p", "r"));
        assertThrows(IllegalArgumentException.class, () -> new User(" ", "u", "p", "r"));
        assertThrows(IllegalArgumentException.class, () -> new User("1", null, "p", "r"));
        assertThrows(IllegalArgumentException.class, () -> new User("1", " ", "p", "r"));
        assertThrows(IllegalArgumentException.class, () -> new User("1", "u", null, "r"));
        assertThrows(IllegalArgumentException.class, () -> new User("1", "u", "p", null));
        assertThrows(IllegalArgumentException.class, () -> new User("1", "u", "p", " "));

        User user = new User(" 1 ", " name ", " password ", " CUSTOMER ");
        assertEquals("1", user.getId());
        assertEquals("name", user.getUsername());
        assertEquals(" password ", user.getPassword());
        assertEquals("CUSTOMER", user.getRole());
    }

    @Test
    void menuItemRejectsInvalidIdentityAndPriceValues() {
        assertThrows(IllegalArgumentException.class, () -> new MenuItem(null, "Pizza", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem(" ", "Pizza", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem("1", null, 1, true));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem("1", " ", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem("1", "Pizza", -1, true));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem("1", "Pizza", Double.NaN, true));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem("1", "Pizza", Double.POSITIVE_INFINITY, true));

        MenuItem item = new MenuItem(" M1 ", " Pizza ", 0, false);
        assertEquals("M1", item.getId());
        assertEquals("Pizza", item.getName());
        assertEquals(0, item.getPrice());
        assertFalse(item.isAvailable());
    }

    @Test
    void restaurantRejectsInvalidIdentityAndNumericValues() {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(null, "R", true, true, 0));
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(" ", "R", true, true, 0));
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("1", null, true, true, 0));
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("1", " ", true, true, 0));
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("1", "R", true, true, -1));
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("1", "R", true, true, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("1", "R", true, true, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Restaurant("1", "R", true, true, 0, Double.POSITIVE_INFINITY));

        Restaurant restaurant = new Restaurant(" R1 ", " Cafe ", false, false, 0);
        assertEquals(0, restaurant.getDeliveryDistance());
        assertFalse(restaurant.isOpen());
        assertFalse(restaurant.isWithinDeliveryZone());
    }

    @Test
    void orderRejectsInvalidConstructorArgumentsAndProtectsItemList() {
        Customer customer = new Customer("C1", "user", "pass");
        Restaurant restaurant = new Restaurant("R1", "Cafe", true, true, 0);
        assertThrows(IllegalArgumentException.class, () -> new Order(null, customer, restaurant, "Address"));
        assertThrows(IllegalArgumentException.class, () -> new Order(" ", customer, restaurant, "Address"));
        assertThrows(IllegalArgumentException.class, () -> new Order("O1", null, restaurant, "Address"));
        assertThrows(IllegalArgumentException.class, () -> new Order("O1", customer, null, "Address"));
        assertThrows(IllegalArgumentException.class, () -> new Order("O1", customer, restaurant, null));
        assertThrows(IllegalArgumentException.class, () -> new Order("O1", customer, restaurant, " "));

        Order order = new Order("O1", customer, restaurant, "Address");
        assertEquals(0, order.getTotal());
        assertThrows(UnsupportedOperationException.class,
                () -> order.getItems().add(new OrderItem(new MenuItem("M1", "Pizza", 5, true), 1)));
    }

    @Test
    void orderCoversNullItemsUnavailableItemsAndPendingOnlyChanges() {
        Order order = new Order("O1", new Customer("C1", "u", "p"),
                new Restaurant("R1", "Cafe", true, true, 0), "Address");

        assertThrows(IllegalArgumentException.class, () -> order.addItem(null));
        OrderItem unavailable = new OrderItem(new MenuItem("M1", "Unavailable", 5, false), 1);
        assertThrows(IllegalArgumentException.class, () -> order.addItem(unavailable));

        order.setOrderType(OrderType.SCHEDULED);
        assertThrows(IllegalArgumentException.class, () -> order.setScheduledDeliveryTime(null));
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        order.setScheduledDeliveryTime(future);
        assertEquals(future, order.getScheduledDeliveryTime());

        assertThrows(IllegalArgumentException.class, () -> order.setOrderType(OrderType.STANDARD));
        order.setStatus(OrderStatus.CONFIRMED);
        assertThrows(IllegalStateException.class, () -> order.setOrderType(OrderType.EXPRESS));
        assertThrows(IllegalStateException.class, () -> order.setScheduledDeliveryTime(future));
        assertThrows(IllegalStateException.class,
                () -> order.addItem(new OrderItem(new MenuItem("M2", "Available", 2, true), 1)));
    }
}
