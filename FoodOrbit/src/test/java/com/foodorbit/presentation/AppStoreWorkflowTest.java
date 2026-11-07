package com.foodorbit.presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.driver.MotorcycleDriver;
import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.model.Restaurant;
import com.foodorbit.domain.order.OrderType;

class AppStoreWorkflowTest {

    private static final AtomicInteger DRIVER_IDS = new AtomicInteger(8000);

    private AppStore store() {
        return AppStore.get();
    }

    private AppStore.CustomerOrder newOrder(String suffix) {
        return store().placeOrder(
                "Customer " + suffix,
                "0590000000",
                "Nablus",
                "Restaurant " + suffix,
                "Cash",
                List.of("Meal"),
                20.0);
    }

    @Test
    void singletonAndDemoCatalogAreAvailable() {
        AppStore store = store();

        assertSame(store, AppStore.get());
        assertFalse(store.restaurants().isEmpty());
        assertFalse(store.drivers().isEmpty());

        assertTrue(store.menuItemsForRestaurant(null).isEmpty());
        assertTrue(store.menuItemsForRestaurant("unknown").isEmpty());

        assertNotNull(store.orders());
        assertNotNull(store.payments());
        assertNotNull(store.notifications());
    }

    @Test
    void catalogSupportsCreateUpdateDeleteAndRejectsInvalidReferences() {
        AppStore store = store();

        String suffix = UUID.randomUUID().toString();
        String restaurantId = "TEST-R-" + suffix;
        String itemId = "TEST-M-" + suffix;

        Restaurant restaurant = new Restaurant(
                restaurantId, "Test Restaurant", true, true, 0);

        store.addRestaurant(restaurant);

        assertTrue(store.restaurants().stream()
                .anyMatch(r -> r.getId().equals(restaurantId)));

        MenuItem item = new MenuItem(itemId, "Test Meal", 10, true);

        store.addMenuItem(restaurantId, item);

        assertEquals(1, store.menuItemsForRestaurant(restaurantId).size());

        MenuItem updated = new MenuItem(itemId, "Updated Meal", 12, false);

        store.updateMenuItem(restaurantId, updated);

        assertSame(updated, store.menuItemsForRestaurant(restaurantId).get(0));

        assertThrows(IllegalArgumentException.class,
                () -> store.addMenuItem("missing-restaurant", item));

        assertThrows(IllegalArgumentException.class,
                () -> store.updateMenuItem("missing-restaurant", updated));

        assertThrows(IllegalArgumentException.class,
                () -> store.deleteMenuItem("missing-restaurant", itemId));

        assertThrows(IllegalArgumentException.class,
                () -> store.deleteMenuItem(restaurantId, "missing-item"));

        store.deleteMenuItem(restaurantId, itemId);

        assertTrue(store.menuItemsForRestaurant(restaurantId).isEmpty());

        store.deleteRestaurant(restaurantId);

        assertFalse(store.restaurants().stream()
                .anyMatch(r -> r.getId().equals(restaurantId)));

        assertThrows(IllegalArgumentException.class,
                () -> store.deleteRestaurant(restaurantId));
    }

    @Test
    void orderCreationValidatesFieldsAndScheduledTimes() {
        AppStore store = store();

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("", "phone", "address", "restaurant",
                        "Cash", List.of("meal"), 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "", "address", "restaurant",
                        "Cash", List.of("meal"), 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "", "restaurant",
                        "Cash", List.of("meal"), 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "",
                        "Cash", List.of("meal"), 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "restaurant",
                        "", List.of("meal"), 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "restaurant",
                        "Cash", null, 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "restaurant",
                        "Cash", List.of(), 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "restaurant",
                        "Cash", List.of("meal"), -1));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "restaurant",
                        "Cash", List.of("meal"), Double.NaN));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "restaurant",
                        "Cash", List.of("meal"), 1, null, null));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "restaurant",
                        "Cash", List.of("meal"), 1, OrderType.SCHEDULED, null));

        assertThrows(IllegalArgumentException.class,
                () -> store.placeOrder("name", "phone", "address", "restaurant",
                        "Cash", List.of("meal"), 1, OrderType.STANDARD,
                        LocalDateTime.now().plusDays(1)));

        LocalDateTime scheduled = LocalDateTime.now().plusDays(2);

        AppStore.CustomerOrder scheduledOrder = store.placeOrder(
                " Scheduled Customer ",
                " 0591111111 ",
                " Address ",
                " Restaurant ",
                " Cash ",
                List.of("Meal"),
                15,
                OrderType.SCHEDULED,
                scheduled);

        assertEquals(OrderType.SCHEDULED, scheduledOrder.getOrderType());
        assertEquals(scheduled, scheduledOrder.getScheduledDeliveryTime());
        assertEquals("Pending", scheduledOrder.getStatus());
        assertEquals("Scheduled Customer", scheduledOrder.customer);
    }

    @Test
    void fullOrderDriverDeliveryAndPaymentWorkflow() {
        AppStore store = store();

        String suffix = UUID.randomUUID().toString();
        AppStore.CustomerOrder order = newOrder(suffix);

        Driver driver = new MotorcycleDriver(
                DRIVER_IDS.incrementAndGet(),
                "Workflow Driver",
                "0591234567",
                0.1,
                0.1);

        store.addDriver(driver);

        store.changeOrderStatus(order, "Confirmed");

        Driver selected = store.assignDriver(
                order, "Nearest", 0, 0, 2, 2);

        assertSame(driver, selected);
        assertEquals("Workflow Driver", order.getDriverName());
        assertEquals("Nearest", order.getSelectionStrategy());

        assertThrows(IllegalStateException.class,
                () -> store.removeDriver(driver));

        store.changeOrderStatus(order, "Preparing");
        store.changeOrderStatus(order, "Ready");
        store.changeOrderStatus(order, "Out for Delivery");
        store.changeOrderStatus(order, "Delivered");

        assertEquals("Delivered", order.getStatus());
        assertEquals(1, driver.getCompletedDeliveries());

        AppStore.PaymentRecord payment = store.payments().stream()
                .filter(p -> p.orderId.equals(order.id))
                .findFirst()
                .orElseThrow();

        store.collectCash(order, 20);

        assertEquals("Completed", payment.getStatus());

        store.refundPayment(payment);

        assertEquals("Refunded", payment.getStatus());

        store.removeDriver(driver);

        assertFalse(store.drivers().contains(driver));

        assertThrows(IllegalStateException.class,
                () -> store.changeOrderStatus(order, "Preparing"));

        assertThrows(IllegalStateException.class,
                () -> store.collectCash(order, 20));

        assertThrows(IllegalStateException.class,
                () -> store.refundPayment(payment));
    }

    @Test
    void assignmentAndPaymentRejectInvalidStatesAndInputs() {
        AppStore store = store();

        AppStore.CustomerOrder order = newOrder(UUID.randomUUID().toString());

        assertThrows(IllegalStateException.class,
                () -> store.assignDriver(order, "Nearest", 0, 0, 1, 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.assignDriver(null, "Nearest", 0, 0, 1, 1));

        store.changeOrderStatus(order, "Confirmed");

        assertThrows(IllegalArgumentException.class,
                () -> store.assignDriver(order, "Nearest", Double.NaN, 0, 1, 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.assignDriver(order, "Unknown", 0, 0, 1, 1));

        assertThrows(IllegalArgumentException.class,
                () -> store.collectCash(null, 20));

        assertThrows(IllegalArgumentException.class,
                () -> store.refundPayment(null));

        assertThrows(IllegalStateException.class,
                () -> store.changeOrderStatus(order, "Delivered"));

        assertThrows(IllegalStateException.class,
                () -> store.changeOrderStatus(order, "Unknown"));

        // The current AppStore contract treats a null driver as a no-op.
        assertDoesNotThrow(() -> store.removeDriver(null));
    }

    @Test
    void customerOrderAndNotificationRecordsExposeExpectedState() {
        AppStore.CustomerOrder order = newOrder(UUID.randomUUID().toString());

        assertEquals("Not Assigned", order.getDriverName());
        assertEquals("-", order.getSelectionStrategy());

        assertThrows(IllegalArgumentException.class,
                () -> order.setStatus(null));

        assertThrows(IllegalArgumentException.class,
                () -> order.setStatus(" "));

        AppStore.NotificationRecord notification =
                store().notifications().get(0);

        assertEquals("Unread", notification.getStatus());

        notification.setRead(true);

        assertEquals("Read", notification.getStatus());

        notification.setRead(false);

        assertEquals("Unread", notification.getStatus());
    }
}