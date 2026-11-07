package com.foodorbit.application.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private final List<NotificationObserver> observers = new ArrayList<>();

    public void addObserver(NotificationObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String orderId, String message) {
        for (NotificationObserver observer : observers) {
            observer.update(orderId, message);
        }
    }
}