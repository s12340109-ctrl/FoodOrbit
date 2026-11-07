package com.foodorbit.application.notification;

public interface NotificationObserver {

    void update(String orderId, String message);

}