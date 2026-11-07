package com.foodorbit.application.notification;

public class DeliveryNotificationService {

    private final NotificationService notificationService;

    public DeliveryNotificationService(NotificationService notificationService) {
        if (notificationService == null) {
            throw new IllegalArgumentException(
                "Notification service cannot be null"
            );
        }

        this.notificationService = notificationService;
    }

    public void sendDeliveryNotification(String orderId, DeliveryEvent event) {

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException(
                "Order ID cannot be empty"
            );
        }

        if (event == null) {
            throw new IllegalArgumentException(
                "Delivery event cannot be null"
            );
        }

        String message = switch (event) {
            case DRIVER_ASSIGNED -> "Driver assigned";
            case OUT_FOR_DELIVERY -> "Out for delivery";
            case DELIVERED -> "Delivered";
        };

        notificationService.notifyObservers(orderId, message);
    }
}