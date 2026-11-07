package com.foodorbit.application.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

public class DeliveryNotificationServiceTest {

    @Test
    void driverAssignedNotification() {
        NotificationService notificationService =
                mock(NotificationService.class);

        DeliveryNotificationService service =
                new DeliveryNotificationService(notificationService);

        service.sendDeliveryNotification(
                "O101", DeliveryEvent.DRIVER_ASSIGNED);

        verify(notificationService).notifyObservers(
                "O101", "Driver assigned");
    }

    @Test
    void outForDeliveryNotification() {
        NotificationService notificationService =
                mock(NotificationService.class);

        DeliveryNotificationService service =
                new DeliveryNotificationService(notificationService);

        service.sendDeliveryNotification(
                "O101", DeliveryEvent.OUT_FOR_DELIVERY);

        verify(notificationService).notifyObservers(
                "O101", "Out for delivery");
    }

    @Test
    void deliveredNotification() {
        NotificationService notificationService =
                mock(NotificationService.class);

        DeliveryNotificationService service =
                new DeliveryNotificationService(notificationService);

        service.sendDeliveryNotification(
                "O101", DeliveryEvent.DELIVERED);

        verify(notificationService).notifyObservers(
                "O101", "Delivered");
    }

    @Test
    void nullNotificationServiceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryNotificationService(null));
    }

    @Test
    void nullOrderIdIsRejected() {
        DeliveryNotificationService service =
                new DeliveryNotificationService(
                        mock(NotificationService.class));

        assertThrows(IllegalArgumentException.class,
                () -> service.sendDeliveryNotification(
                        null, DeliveryEvent.DELIVERED));
    }

    @Test
    void blankOrderIdIsRejected() {
        DeliveryNotificationService service =
                new DeliveryNotificationService(
                        mock(NotificationService.class));

        assertThrows(IllegalArgumentException.class,
                () -> service.sendDeliveryNotification(
                        "   ", DeliveryEvent.DELIVERED));
    }

    @Test
    void nullDeliveryEventIsRejected() {
        DeliveryNotificationService service =
                new DeliveryNotificationService(
                        mock(NotificationService.class));

        assertThrows(IllegalArgumentException.class,
                () -> service.sendDeliveryNotification("O101", null));
    }
}
