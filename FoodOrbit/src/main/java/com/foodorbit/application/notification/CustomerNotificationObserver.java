package com.foodorbit.application.notification;

public class CustomerNotificationObserver implements NotificationObserver {

    private final String customerId;

    public CustomerNotificationObserver(String customerId) {

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException(
                "Customer ID cannot be empty"
            );
        }

        this.customerId = customerId;
    }

    @Override
    public void update(String orderId, String message) {

        System.out.println(
            "Notification to customer " + customerId
            + " | Order: " + orderId
            + " | Message: " + message
        );
    }
}