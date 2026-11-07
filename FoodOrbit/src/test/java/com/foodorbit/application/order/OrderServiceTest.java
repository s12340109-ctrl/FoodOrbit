
package com.foodorbit.application.order;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.foodorbit.domain.model.OrderStatus;
import com.foodorbit.domain.model.Customer;
import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.model.Order;
import com.foodorbit.domain.model.OrderItem;
import com.foodorbit.domain.model.Restaurant;
import com.foodorbit.domain.order.OrderType;
import com.foodorbit.application.pricing.NormalPricingStrategy;
import com.foodorbit.application.pricing.PeakHourPricingStrategy;
import com.foodorbit.application.pricing.PromotionalPricingStrategy;
import com.foodorbit.application.pricing.ExpressPricingStrategy;
import com.foodorbit.application.pricing.PricingStrategy;

import java.time.Instant;
import java.time.ZoneId;
import java.time.Clock;
import java.lang.reflect.Field;

class OrderServiceTest {

    private OrderService orderService;
    private Customer customer;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
        customer = new Customer("C1", "yara", "1234");
        restaurant = new Restaurant(
                "R1", "Pizza House", true, true, 20.0);
        menuItem = new MenuItem("M1", "Pizza", 10.0, true);
    }

    @Test
    void shouldAddValidOrder() {
        Order order = new Order(
                "O1", customer, restaurant, "Nablus, Rafidia");
        order.addItem(new OrderItem(menuItem, 2));

        orderService.addOrder(order);

        assertEquals(1, orderService.getAllOrders().size());
        assertSame(order, orderService.findOrderById("O1"));
    }

    @Test
    void shouldRejectNullOrder() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(null)
        );
    }

    @Test
    void shouldRejectEmptyOrder() {
        Order order = new Order(
                "O1", customer, restaurant, "Nablus");

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(order)
        );
    }

    @Test
    void shouldRejectOrderBelowMinimumValue() {
        Order order = new Order(
                "O1", customer, restaurant, "Nablus");
        order.addItem(new OrderItem(menuItem, 1));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(order)
        );
    }

    @Test
    void shouldFindOrderById() {
        Order order = new Order(
                "O1", customer, restaurant, "Nablus");
        order.addItem(new OrderItem(menuItem, 2));
        orderService.addOrder(order);

        assertSame(order, orderService.findOrderById("O1"));
    }

    @Test
    void shouldReturnNullWhenOrderDoesNotExist() {
        assertNull(orderService.findOrderById("UNKNOWN"));
    }

    @Test
    void shouldReturnNullWhenSearchingWithNullId() {
        assertNull(orderService.findOrderById(null));
    }

    @Test
    void shouldNotAllowDirectModificationOfOrdersList() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> orderService.getAllOrders().clear()
        );
    }

    @Test
    void shouldRejectOrderWhenRestaurantIsClosed() {
        Restaurant closedRestaurant = new Restaurant(
                "R2", "Closed Restaurant", false, true, 20.0);

        Order order = new Order(
                "O2", customer, closedRestaurant, "Nablus");
        order.addItem(new OrderItem(menuItem, 2));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(order)
        );
    }

    @Test
    void shouldRejectOrderWhenRestaurantIsOutsideDeliveryZone() {
        Restaurant outsideZoneRestaurant = new Restaurant(
                "R3", "Far Restaurant", true, false, 20.0);

        Order order = new Order(
                "O3", customer, outsideZoneRestaurant, "Nablus");
        order.addItem(new OrderItem(menuItem, 2));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(order)
        );
    }


@Test
void shouldRejectOrderWhenMenuItemIsUnavailable() {
    MenuItem unavailableItem = new MenuItem(
            "M2", "Unavailable Pizza", 10.0, false);

    Order order = new Order(
            "O4", customer, restaurant, "Nablus");

    assertThrows(
            IllegalArgumentException.class,
            () -> order.addItem(
                    new OrderItem(unavailableItem, 2))
    );
}


    @Test
    void shouldRejectOrderWithBlankDeliveryAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("O5", customer, restaurant, "   ")
        );
    }

    @Test
    void shouldRejectOrderWithNullDeliveryAddress() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("O6", customer, restaurant, null)
        );
    }

    @Test
    void newOrderShouldStartAsPending() {
        Order order = new Order(
                "O10", customer, restaurant, "Nablus");

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void shouldAllowValidStatusTransition() {
        Order order = createValidOrder("O11");
        orderService.addOrder(order);

        orderService.changeOrderStatus(
                "O11", OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        Order order = createValidOrder("O12");
        orderService.addOrder(order);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.changeOrderStatus(
                        "O12", OrderStatus.DELIVERED)
        );

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void shouldCancelPendingOrder() {
        Order order = createValidOrder("O13");
        orderService.addOrder(order);

        orderService.cancelOrder("O13");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldRejectCancellationAfterPreparationStarts() {
        Order order = createValidOrder("O14");
        orderService.addOrder(order);

        orderService.changeOrderStatus(
                "O14", OrderStatus.CONFIRMED);

        orderService.changeOrderStatus(
                "O14", OrderStatus.PREPARING);

        assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelOrder("O14")
        );

        assertEquals(OrderStatus.PREPARING, order.getStatus());
    }

@Test
void shouldRejectCancellationWhenOrderIsReady() {
    Order order = createValidOrder("O41");
    orderService.addOrder(order);

    orderService.changeOrderStatus("O41", OrderStatus.CONFIRMED);
    orderService.changeOrderStatus("O41", OrderStatus.PREPARING);
    orderService.changeOrderStatus("O41", OrderStatus.READY);

    assertThrows(
            IllegalStateException.class,
            () -> orderService.cancelOrder("O41"));

    assertEquals(OrderStatus.READY, order.getStatus());
}

    @Test
    void shouldRejectChangesAfterOrderIsDelivered() {
        Order order = createValidOrder("O15");
        orderService.addOrder(order);

        orderService.changeOrderStatus("O15", OrderStatus.CONFIRMED);
        orderService.changeOrderStatus("O15", OrderStatus.PREPARING);
        orderService.changeOrderStatus("O15", OrderStatus.READY);
        orderService.changeOrderStatus("O15", OrderStatus.OUT_FOR_DELIVERY);
        orderService.changeOrderStatus("O15", OrderStatus.DELIVERED);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.changeOrderStatus(
                        "O15", OrderStatus.PREPARING)
        );

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    private Order createValidOrder(String orderId) {
        Order order = new Order(
                orderId, customer, restaurant, "Nablus");

        order.addItem(new OrderItem(menuItem, 2));

        return order;
    }

    @Test
    void shouldRejectScheduledOrderWithoutDeliveryTime() {
        Order order = createValidOrder("O20");
        order.setOrderType(OrderType.SCHEDULED);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(order)
        );
    }

    @Test
    void shouldRejectScheduledOrderWithPastDeliveryTime() {
        Order order = createValidOrder("O21");
        order.setOrderType(OrderType.SCHEDULED);
        order.setScheduledDeliveryTime(
                Instant.parse("2025-01-01T10:00:00Z")
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(order)
        );
    }

    @Test
    void shouldAcceptScheduledOrderWithFutureDeliveryTime() {
        Order order = createValidOrder("O22");
        order.setOrderType(OrderType.SCHEDULED);
        order.setScheduledDeliveryTime(
                java.time.LocalDateTime.now().plusDays(1)
        );

        assertDoesNotThrow(() -> orderService.addOrder(order));
    }

    // Pricing strategy tests for OrderService

    @Test
    void shouldCalculateNormalPricedTotal() {
        Order order = createValidOrder("P1");

        double total = orderService.calculatePricedTotal(
                order, new NormalPricingStrategy());

        assertEquals(20.0, total, 0.001);
    }

    @Test
    void shouldCalculatePeakHourPricedTotal() {
        Order order = createValidOrder("P2");

        double total = orderService.calculatePricedTotal(
                order, new PeakHourPricingStrategy(1.20));

        assertEquals(24.0, total, 0.001);
    }

    @Test
    void shouldCalculatePromotionalPricedTotal() {
        Order order = createValidOrder("P3");

        double total = orderService.calculatePricedTotal(
                order,new PromotionalPricingStrategy(15));

        assertEquals(17.0, total, 0.001);
    }

    @Test
    void shouldCalculateExpressPricedTotal() {
        Order order = createValidOrder("P4");

        double total = orderService.calculatePricedTotal(
                order, new ExpressPricingStrategy(5.0));

        assertEquals(25.0, total, 0.001);
    }

    @Test
    void shouldRejectNullOrderWhenCalculatingPricedTotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.calculatePricedTotal(
                        null, new NormalPricingStrategy())
        );
    }

    @Test
    void shouldRejectNullStrategyWhenCalculatingPricedTotal() {
        Order order = createValidOrder("P5");

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.calculatePricedTotal(order, null)
        );
    }


@Test
void addingOrderWithDuplicateIdThrowsException() {
    Order firstOrder = createValidOrder("O30");
    Order duplicateOrder = createValidOrder("O30");

    orderService.addOrder(firstOrder);

    assertThrows(IllegalArgumentException.class,
            () -> orderService.addOrder(duplicateOrder));
}


@Test
void shouldRejectOrderThatIsNotPendingWhenAdded() {
    Order order = createValidOrder("O31");
    order.setStatus(OrderStatus.CONFIRMED);

    assertThrows(IllegalArgumentException.class,
            () -> orderService.addOrder(order));
}

@Test
void shouldRejectNullOrderStatus() {
    Order order = createValidOrder("O32");
    orderService.addOrder(order);

    assertThrows(IllegalArgumentException.class,
            () -> orderService.changeOrderStatus("O32", null));

    assertEquals(OrderStatus.PENDING, order.getStatus());
}

@Test
void shouldRejectStatusChangeForUnknownOrder() {
    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.changeOrderStatus(
                    "UNKNOWN", OrderStatus.CONFIRMED)
    );
}


@Test
void shouldRejectCancellationOfUnknownOrder() {
    assertThrows(IllegalArgumentException.class,
            () -> orderService.cancelOrder("UNKNOWN"));
}

@Test
void shouldRejectCancellingAlreadyCancelledOrder() {
    Order order = createValidOrder("O33");
    orderService.addOrder(order);

    orderService.cancelOrder("O33");

    assertThrows(IllegalStateException.class,
            () -> orderService.cancelOrder("O33"));

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
}

@Test
void shouldRejectStatusChangeAfterOrderIsCancelled() {
    Order order = createValidOrder("O34");
    orderService.addOrder(order);

    orderService.cancelOrder("O34");

    assertThrows(IllegalArgumentException.class,
            () -> orderService.changeOrderStatus(
                    "O34", OrderStatus.CONFIRMED));

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
}

    @Test
    void shouldRejectInvalidCalculatedPrice() {
        Order order = createValidOrder("P6");

        PricingStrategy invalidStrategy = basePrice -> Double.NaN;

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.calculatePricedTotal(
                        order, invalidStrategy)
        );
    }

@Test
void shouldRejectAddingUnavailableItemAfterOrderIsRegistered() {
    Order order = createValidOrder("O40");
    orderService.addOrder(order);

    MenuItem unavailableItem = new MenuItem(
            "M2", "Unavailable Pizza", 10.0, false);

    assertThrows(
            IllegalArgumentException.class,
            () -> order.addItem(new OrderItem(unavailableItem, 1))
    );
}


    // Additional constructor and delivery-pricing coverage

    @Test
    void shouldCreateServiceWithClockOnly() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-30T10:00:00Z"), ZoneId.of("UTC"));
        OrderService service = new OrderService(fixedClock);
        assertNotNull(service);
        assertTrue(service.getAllOrders().isEmpty());
    }

    @Test
    void shouldCreateServiceWithClockAndDeliveryRate() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-30T10:00:00Z"), ZoneId.of("UTC"));
        OrderService service = new OrderService(fixedClock, 2.0);
        assertNotNull(service);
        assertTrue(service.getAllOrders().isEmpty());
    }

    @Test
    void shouldCreateServiceWithClockRateAndExpressFee() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-30T10:00:00Z"), ZoneId.of("UTC"));
        OrderService service = new OrderService(fixedClock, 2.0, 5.0);
        assertNotNull(service);
        assertTrue(service.getAllOrders().isEmpty());
    }

    @Test
    void shouldRejectNullClockInClockOnlyConstructor() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderService((Clock) null));
    }

    @Test
    void shouldRejectNullClockInClockAndRateConstructor() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderService(null, 2.0));
    }

    @Test
    void shouldRejectNegativeRateInClockAndRateConstructor() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-30T10:00:00Z"), ZoneId.of("UTC"));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderService(fixedClock, -1.0));
    }

    @Test
    void shouldFindSecondOrderAfterCheckingEarlierOrder() {
        Order first = createValidOrder("FIND1");
        Order second = createValidOrder("FIND2");
        orderService.addOrder(first);
        orderService.addOrder(second);

        assertSame(second, orderService.findOrderById("FIND2"));
    }

    @Test
    void shouldRejectNullClockInThreeArgumentConstructor() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderService(null, 2.0, 5.0));
    }

    @Test
    void shouldRejectNegativeDeliveryRateInThreeArgumentConstructor() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-30T10:00:00Z"), ZoneId.of("UTC"));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderService(fixedClock, -1.0, 5.0));
    }

    @Test
    void shouldRejectNegativeExpressFeeInThreeArgumentConstructor() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-30T10:00:00Z"), ZoneId.of("UTC"));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderService(fixedClock, 2.0, -1.0));
    }

    @Test
    void shouldCalculateStandardDeliveryFeeFromRestaurantDistance() {
        Order order = createValidOrder("FEE1");
        OrderService service = new OrderService(
                Clock.systemUTC(), 2.0, 5.0);

        double expected = restaurant.getDeliveryDistance() * 2.0;
        assertEquals(expected, service.calculateDeliveryFee(order), 0.001);
    }

    @Test
    void shouldAddExpressFeeToDeliveryFeeForExpressOrder() {
        Order order = createValidOrder("FEE2");
        order.setOrderType(OrderType.EXPRESS);
        OrderService service = new OrderService(
                Clock.systemUTC(), 2.0, 5.0);

        double expected = restaurant.getDeliveryDistance() * 2.0 + 5.0;
        assertEquals(expected, service.calculateDeliveryFee(order), 0.001);
    }

    @Test
    void shouldRejectNullOrderWhenCalculatingDeliveryFee() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.calculateDeliveryFee(null));
    }

    @Test
    void shouldCalculateFinalTotalIncludingDeliveryFeeOnce() {
        Order order = createValidOrder("FINAL1");
        OrderService service = new OrderService(
                Clock.systemUTC(), 2.0, 5.0);
        PricingStrategy strategy = new NormalPricingStrategy();

        double expected = order.getTotal()
                + restaurant.getDeliveryDistance() * 2.0;
        assertEquals(expected,
                service.calculateFinalTotal(order, strategy), 0.001);
    }

    @Test
    void shouldIncludeExpressFeeInFinalTotal() {
        Order order = createValidOrder("FINAL2");
        order.setOrderType(OrderType.EXPRESS);
        OrderService service = new OrderService(
                Clock.systemUTC(), 2.0, 5.0);

        double expected = order.getTotal()
                + restaurant.getDeliveryDistance() * 2.0 + 5.0;
        assertEquals(expected,
                service.calculateFinalTotal(order, new NormalPricingStrategy()),
                0.001);
    }

    @Test
    void shouldRejectNullOrderWhenCalculatingFinalTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.calculateFinalTotal(
                        null, new NormalPricingStrategy()));
    }

    @Test
    void shouldRejectNullStrategyWhenCalculatingFinalTotal() {
        Order order = createValidOrder("FINAL3");
        assertThrows(IllegalArgumentException.class,
                () -> orderService.calculateFinalTotal(order, null));
    }



    @Test
    void shouldRejectOrderWhenMenuItemBecomesUnavailableBeforeAdding() throws Exception {
        MenuItem initiallyAvailableItem = new MenuItem(
                "M_REFLECT", "Pizza", 10.0, true);
        Order order = new Order(
                "O_REFLECT_MENU", customer, restaurant, "Nablus");
        order.addItem(new OrderItem(initiallyAvailableItem, 2));

        // Simulate the item becoming unavailable after it was added to the order.
        Field availableField = MenuItem.class.getDeclaredField("available");
        availableField.setAccessible(true);
        availableField.setBoolean(initiallyAvailableItem, false);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.addOrder(order));
        assertNull(orderService.findOrderById("O_REFLECT_MENU"));
    }

    @Test
    void shouldRejectOrderWhenOrderTypeIsNull() throws Exception {
        Order order = createValidOrder("O_NULL_TYPE");

        // Bypass the public setter, which correctly prevents assigning null.
        Field orderTypeField = Order.class.getDeclaredField("orderType");
        orderTypeField.setAccessible(true);
        orderTypeField.set(order, null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.addOrder(order));
        assertNull(orderService.findOrderById("O_NULL_TYPE"));
    }
    @Test
    void shouldRejectOrderWithBlankId() throws Exception {
        Order order = createValidOrder("O50");

        java.lang.reflect.Field idField =
                Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(order, "   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(order));
    }
    @Test
    void shouldRejectOrderWithNullId() throws Exception {
        Order order = createValidOrder("O51");

        java.lang.reflect.Field idField =
                Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(order, null);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addOrder(order));
    }
    @Test
    void shouldProcessCashPaymentSuccessfully() {
        Order order = createValidOrder("PAY1");

        boolean result = orderService.processCashPayment(
                order,
                new NormalPricingStrategy(),
                20.0);

        assertTrue(result);
    }
    @Test
    void shouldRejectInsufficientCashPayment() {
        Order order = createValidOrder("PAY2");

        boolean result = orderService.processCashPayment(
                order,
                new NormalPricingStrategy(),
                10.0);

        assertFalse(result);
    }
    @Test
    	void shouldRejectPaymentBelowTotalWithDeliveryFee() {
    	    Restaurant deliveryRestaurant = new Restaurant(
    	            "R_PAY", "Delivery Restaurant",
    	            true, true, 0.0, 5.0
    	    );

    	    Order order = new Order(
    	            "PAY3", customer, deliveryRestaurant, "Nablus"
    	    );

    	    order.addItem(new OrderItem(menuItem, 2));

    	    OrderService service = new OrderService(
    	            java.time.Clock.systemDefaultZone(), 2.0
    	    );

    	    double finalTotal = service.calculateFinalTotal(
    	            order, new NormalPricingStrategy()
    	    );

    	    assertEquals(30.0, finalTotal, 0.001);

    	    boolean result = service.processCashPayment(
    	            order, new NormalPricingStrategy(), 20.0
    	    );

    	    assertFalse(result);
    	}

@Test
void shouldRejectNullOrderForCashPayment() {

    OrderService service = new OrderService();

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.processCashPayment(null, null, 100.0)
    );

    assertEquals(
        "Order cannot be null",
        exception.getMessage()
    );
}

}