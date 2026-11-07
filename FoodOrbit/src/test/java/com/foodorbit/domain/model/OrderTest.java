
package com.foodorbit.domain.model;

import static org.junit.jupiter.api.Assertions.*;
import com.foodorbit.domain.order.OrderType;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void shouldReturnCustomerAndDeliveryAddress() {

    	Customer customer = new Customer(
    		    "C001",
    		    "safaa",
    		    "password123"
    		);

        Restaurant restaurant = new Restaurant(
            "R001",
            "Pizza House",
            true,
            true,
            20.0,
            5.0
        );

        Order order = new Order(
            "O001",
            customer,
            restaurant,
            "Nablus"
        );

        assertSame(customer, order.getCustomer());

        assertEquals(
            "Nablus",
            order.getDeliveryAddress()
        );
    }

@Test
void shouldRejectNullDriver() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.assignDriver(null)
    );

    assertEquals(
        "Driver cannot be null",
        exception.getMessage()
    );

    assertNull(order.getAssignedDriver());
}

@Test
void shouldRejectDriverAssignmentWhenOrderIsNotReady() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    assertEquals(OrderStatus.PENDING, order.getStatus());


IllegalStateException exception = assertThrows(
    IllegalStateException.class,
    () -> order.assignDriver(
        new com.foodorbit.domain.driver.MotorcycleDriver(
            1,
            "Ahmad",
            "0599123456",
            0.0,
            0.0
        )
    )
);


    assertEquals(
        "Driver can only be assigned to a ready order",
        exception.getMessage()
    );

    assertNull(order.getAssignedDriver());
}

@Test
void shouldAssignDriverSuccessfullyWhenOrderIsReady() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setStatus(OrderStatus.CONFIRMED);
    order.setStatus(OrderStatus.PREPARING);
    order.setStatus(OrderStatus.READY);

    com.foodorbit.domain.driver.MotorcycleDriver driver =
        new com.foodorbit.domain.driver.MotorcycleDriver(
            1,
            "Ahmad",
            "0599123456",
            0.0,
            0.0
        );

    order.assignDriver(driver);

    assertSame(driver, order.getAssignedDriver());

    assertEquals(OrderStatus.READY, order.getStatus());
}

@Test
void shouldRejectAssigningSecondDriver() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setStatus(OrderStatus.CONFIRMED);
    order.setStatus(OrderStatus.PREPARING);
    order.setStatus(OrderStatus.READY);

    com.foodorbit.domain.driver.MotorcycleDriver firstDriver =
        new com.foodorbit.domain.driver.MotorcycleDriver(
            1,
            "Ahmad",
            "0599123456",
            0.0,
            0.0
        );

    com.foodorbit.domain.driver.MotorcycleDriver secondDriver =
        new com.foodorbit.domain.driver.MotorcycleDriver(
            2,
            "Omar",
            "0599876543",
            1.0,
            1.0
        );

    order.assignDriver(firstDriver);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> order.assignDriver(secondDriver)
    );

    assertEquals(
        "Order already has an assigned driver",
        exception.getMessage()
    );

    assertSame(firstDriver, order.getAssignedDriver());
}

@Test
void shouldRejectScheduledDeliveryTimeForStandardOrder() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    java.time.LocalDateTime deliveryTime =
        java.time.LocalDateTime.now().plusDays(1);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setScheduledDeliveryTime(deliveryTime)
    );

    assertEquals(
        "Set the order type to SCHEDULED before setting a delivery time.",
        exception.getMessage()
    );

    assertNull(order.getScheduledDeliveryTime());
}

@Test
void shouldSetScheduledDeliveryTimeSuccessfully() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setOrderType(
        com.foodorbit.domain.order.OrderType.SCHEDULED
    );

    java.time.LocalDateTime deliveryTime =
        java.time.LocalDateTime.now().plusDays(1);

    order.setScheduledDeliveryTime(deliveryTime);

    assertEquals(
        com.foodorbit.domain.order.OrderType.SCHEDULED,
        order.getOrderType()
    );

    assertEquals(
        deliveryTime,
        order.getScheduledDeliveryTime()
    );
}

@Test
void shouldRejectNullDeliveryTimeForScheduledOrder() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setOrderType(
        com.foodorbit.domain.order.OrderType.SCHEDULED
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setScheduledDeliveryTime(null)
    );

    assertEquals(
        "Scheduled orders must have a delivery time.",
        exception.getMessage()
    );

    assertNull(order.getScheduledDeliveryTime());
}

@Test
void shouldRejectChangingDeliveryTimeWhenOrderIsNotPending() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setOrderType(
        com.foodorbit.domain.order.OrderType.SCHEDULED
    );

    java.time.LocalDateTime originalTime =
        java.time.LocalDateTime.now().plusDays(1);

    order.setScheduledDeliveryTime(originalTime);

    order.setStatus(OrderStatus.CONFIRMED);

    java.time.LocalDateTime newTime =
        originalTime.plusHours(2);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> order.setScheduledDeliveryTime(newTime)
    );

    assertEquals(
        "Only pending orders can be modified.",
        exception.getMessage()
    );

    assertEquals(
        originalTime,
        order.getScheduledDeliveryTime()
    );
}

@Test
void shouldRejectNullOrderType() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setOrderType(null)
    );

    assertEquals(
        "Order type cannot be null.",
        exception.getMessage()
    );

    assertEquals(
        com.foodorbit.domain.order.OrderType.STANDARD,
        order.getOrderType()
    );
}

@Test
void shouldRejectChangingOrderTypeWhenNotPending() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setStatus(OrderStatus.CONFIRMED);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> order.setOrderType(
            com.foodorbit.domain.order.OrderType.SCHEDULED
        )
    );

    assertEquals(
        "Only pending orders can be modified.",
        exception.getMessage()
    );

    assertEquals(
        com.foodorbit.domain.order.OrderType.STANDARD,
        order.getOrderType()
    );
}

@Test
void shouldRejectChangingScheduledOrderToStandardWhenTimeExists() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setOrderType(
        com.foodorbit.domain.order.OrderType.SCHEDULED
    );

    java.time.LocalDateTime deliveryTime =
        java.time.LocalDateTime.now().plusDays(1);

    order.setScheduledDeliveryTime(deliveryTime);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setOrderType(
            com.foodorbit.domain.order.OrderType.STANDARD
        )
    );

    assertEquals(
        "Only scheduled orders can have a scheduled delivery time.",
        exception.getMessage()
    );

    assertEquals(
        com.foodorbit.domain.order.OrderType.SCHEDULED,
        order.getOrderType()
    );

    assertEquals(
        deliveryTime,
        order.getScheduledDeliveryTime()
    );
}

@Test
void shouldRejectNullOrderItem() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.addItem(null)
    );

    assertEquals(
        "Order item cannot be null.",
        exception.getMessage()
    );

    assertTrue(order.getItems().isEmpty());
}



@Test
void shouldRejectAddingItemWhenOrderIsNotPending() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    OrderItem orderItem = new OrderItem(menuItem, 2);

    order.setStatus(OrderStatus.CONFIRMED);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> order.addItem(orderItem)
    );

    assertEquals(
        "Cannot modify an order that is not pending.",
        exception.getMessage()
    );

    assertTrue(order.getItems().isEmpty());
}


@Test
void shouldRejectUnavailableMenuItem() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        false
    );

    OrderItem orderItem = new OrderItem(menuItem, 2);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.addItem(orderItem)
    );

    assertEquals(
        "Cannot add an unavailable menu item.",
        exception.getMessage()
    );

    assertTrue(order.getItems().isEmpty());
}

@Test
void shouldAddAvailableMenuItemSuccessfully() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    OrderItem orderItem = new OrderItem(menuItem, 2);

    order.addItem(orderItem);

    assertEquals(1, order.getItems().size());

    assertSame(
        orderItem,
        order.getItems().get(0)
    );

    assertEquals(50.0, order.getTotal(), 0.001);
}

@Test
void shouldRejectNonFiniteOrderTotal() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    MenuItem menuItem = new MenuItem(
        "M001",
        "Expensive Item",
        Double.MAX_VALUE,
        true
    );

    OrderItem firstItem = new OrderItem(menuItem, 1);
    OrderItem secondItem = new OrderItem(menuItem, 1);

    order.addItem(firstItem);
    order.addItem(secondItem);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.getTotal()
    );

    assertEquals(
        "Order total must be finite.",
        exception.getMessage()
    );
}

@Test
void shouldRejectNullOrderStatus() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setStatus(null)
    );

    assertEquals(
        "Order status cannot be null.",
        exception.getMessage()
    );

    assertEquals(
        OrderStatus.PENDING,
        order.getStatus()
    );
}

@Test
void shouldRejectInvalidStatusTransitionFromPendingToDelivered() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setStatus(OrderStatus.DELIVERED)
    );

    assertEquals(
        "Invalid order status transition from PENDING to DELIVERED.",
        exception.getMessage()
    );

    assertEquals(
        OrderStatus.PENDING,
        order.getStatus()
    );
}

@Test
void shouldRejectStatusChangeAfterDelivered() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setStatus(OrderStatus.CONFIRMED);
    order.setStatus(OrderStatus.PREPARING);
    order.setStatus(OrderStatus.READY);
    order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
    order.setStatus(OrderStatus.DELIVERED);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setStatus(OrderStatus.CANCELLED)
    );

    assertEquals(
        "Invalid order status transition from DELIVERED to CANCELLED.",
        exception.getMessage()
    );

    assertEquals(
        OrderStatus.DELIVERED,
        order.getStatus()
    );
}

@Test
void shouldRejectStatusChangeAfterCancelled() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setStatus(OrderStatus.CANCELLED);

    assertEquals(
        OrderStatus.CANCELLED,
        order.getStatus()
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setStatus(OrderStatus.CONFIRMED)
    );

    assertEquals(
        "Invalid order status transition from CANCELLED to CONFIRMED.",
        exception.getMessage()
    );

    assertEquals(
        OrderStatus.CANCELLED,
        order.getStatus()
    );
}

@Test
void shouldRejectNullOrderId() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new Order(
            null,
            customer,
            restaurant,
            "Nablus"
        )
    );

    assertEquals(
        "Order ID cannot be empty.",
        exception.getMessage()
    );
}

@Test
void shouldRejectEmptyOrderId() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new Order(
            "   ",
            customer,
            restaurant,
            "Nablus"
        )
    );

    assertEquals(
        "Order ID cannot be empty.",
        exception.getMessage()
    );
}

@Test
void shouldRejectNullCustomer() {

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new Order(
            "O001",
            null,
            restaurant,
            "Nablus"
        )
    );

    assertEquals(
        "Customer cannot be null.",
        exception.getMessage()
    );
}

@Test
void shouldRejectNullRestaurant() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new Order(
            "O001",
            customer,
            null,
            "Nablus"
        )
    );

    assertEquals(
        "Restaurant cannot be null.",
        exception.getMessage()
    );
}

@Test
void shouldRejectNullDeliveryAddress() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new Order(
            "O001",
            customer,
            restaurant,
            null
        )
    );

    assertEquals(
        "Delivery address cannot be empty.",
        exception.getMessage()
    );
}

@Test
void shouldRejectEmptyDeliveryAddress() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> new Order(
            "O001",
            customer,
            restaurant,
            "   "
        )
    );

    assertEquals(
        "Delivery address cannot be empty.",
        exception.getMessage()
    );
}

@Test
void shouldRejectInvalidTransitionFromConfirmedToDelivered() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setStatus(OrderStatus.CONFIRMED);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setStatus(OrderStatus.DELIVERED)
    );

    assertEquals(
        "Invalid order status transition from CONFIRMED to DELIVERED.",
        exception.getMessage()
    );

    assertEquals(
        OrderStatus.CONFIRMED,
        order.getStatus()
    );
}

@Test
void shouldRejectInvalidTransitionFromPreparingToDelivered() {

    Customer customer = new Customer(
        "C001",
        "safaa",
        "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Order order = new Order(
        "O001",
        customer,
        restaurant,
        "Nablus"
    );

    order.setStatus(OrderStatus.CONFIRMED);
    order.setStatus(OrderStatus.PREPARING);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setStatus(OrderStatus.DELIVERED)
    );

    assertEquals(
        "Invalid order status transition from PREPARING to DELIVERED.",
        exception.getMessage()
    );

    assertEquals(
        OrderStatus.PREPARING,
        order.getStatus()
    );
}

@Test
void shouldRejectInvalidTransitionFromReadyToDelivered() {

    Customer customer = new Customer(
        "C001", "safaa", "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001", "Pizza House",
        true, true, 20.0, 5.0
    );

    Order order = new Order(
        "O001", customer, restaurant, "Nablus"
    );

    order.setStatus(OrderStatus.CONFIRMED);
    order.setStatus(OrderStatus.PREPARING);
    order.setStatus(OrderStatus.READY);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setStatus(OrderStatus.DELIVERED)
    );

    assertEquals(
        "Invalid order status transition from READY to DELIVERED.",
        exception.getMessage()
    );

    assertEquals(OrderStatus.READY, order.getStatus());
}

@Test
void shouldRejectInvalidTransitionFromOutForDelivery() {

    Customer customer = new Customer(
        "C001", "safaa", "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001", "Pizza House",
        true, true, 20.0, 5.0
    );

    Order order = new Order(
        "O001", customer, restaurant, "Nablus"
    );

    order.setStatus(OrderStatus.CONFIRMED);
    order.setStatus(OrderStatus.PREPARING);
    order.setStatus(OrderStatus.READY);
    order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> order.setStatus(OrderStatus.CANCELLED)
    );

    assertEquals(
        "Invalid order status transition from OUT_FOR_DELIVERY to CANCELLED.",
        exception.getMessage()
    );

    assertEquals(
        OrderStatus.OUT_FOR_DELIVERY,
        order.getStatus()
    );
}

@Test
void shouldAllowNullDeliveryTimeForStandardOrder() {

    Customer customer = new Customer(
        "C001", "safaa", "password123"
    );

    Restaurant restaurant = new Restaurant(
        "R001", "Pizza House",
        true, true, 20.0, 5.0
    );

    Order order = new Order(
        "O001", customer, restaurant, "Nablus"
    );

    order.setScheduledDeliveryTime(null);

    assertEquals(
        OrderType.STANDARD,
        order.getOrderType()
    );

    assertNull(order.getScheduledDeliveryTime());
}





}
