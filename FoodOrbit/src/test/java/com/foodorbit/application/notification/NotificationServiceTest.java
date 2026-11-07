package com.foodorbit.application.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

public class NotificationServiceTest {

    @Test
    void notifyRegisteredObserver() {
        NotificationService service = new NotificationService();
        NotificationObserver observer = mock(NotificationObserver.class);

        service.addObserver(observer);
        service.notifyObservers("O101", "Driver assigned");

        verify(observer).update("O101", "Driver assigned");
    }

    @Test
    void removedObserverDoesNotReceiveNotification() {
        NotificationService service = new NotificationService();
        NotificationObserver observer = mock(NotificationObserver.class);

        service.addObserver(observer);
        service.removeObserver(observer);
        service.notifyObservers("O101", "Delivered");

        verify(observer, never()).update(anyString(), anyString());
    }

    @Test
    void duplicateObserverReceivesOnlyOneNotification() {
        NotificationService service = new NotificationService();
        NotificationObserver observer = mock(NotificationObserver.class);

        service.addObserver(observer);
        service.addObserver(observer);
        service.notifyObservers("O101", "Out for delivery");

        verify(observer, times(1))
                .update("O101", "Out for delivery");
    }

    @Test
    void nullObserverIsRejected() {
        NotificationService service = new NotificationService();

        assertThrows(IllegalArgumentException.class,
                () -> service.addObserver(null));
    }

    @Test
    void multipleObserversReceiveNotification() {
        NotificationService service = new NotificationService();

        NotificationObserver first = mock(NotificationObserver.class);
        NotificationObserver second = mock(NotificationObserver.class);

        service.addObserver(first);
        service.addObserver(second);

        service.notifyObservers("O102", "Delivered");

        verify(first).update("O102", "Delivered");
        verify(second).update("O102", "Delivered");
    }
}