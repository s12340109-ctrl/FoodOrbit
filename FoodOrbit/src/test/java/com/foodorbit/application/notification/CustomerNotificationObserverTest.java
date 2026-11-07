package com.foodorbit.application.notification;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class CustomerNotificationObserverTest {

    @Test
    void validCustomerReceivesNotification() {
        CustomerNotificationObserver observer =
                new CustomerNotificationObserver("C101");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            observer.update("O500", "Driver assigned");

            String result = output.toString();

            assertTrue(result.contains("C101"));
            assertTrue(result.contains("O500"));
            assertTrue(result.contains("Driver assigned"));

        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void nullCustomerIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerNotificationObserver(null));
    }

    @Test
    void emptyCustomerIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerNotificationObserver(""));
    }

    @Test
    void blankCustomerIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerNotificationObserver("   "));
    }
}