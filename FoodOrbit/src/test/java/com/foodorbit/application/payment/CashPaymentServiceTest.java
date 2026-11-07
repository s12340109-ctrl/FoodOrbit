package com.foodorbit.application.payment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CashPaymentServiceTest {

    private final CashPaymentService paymentService =
            new CashPaymentService();

    @Test
    void paymentWithExactAmount() {
        assertTrue(paymentService.processPayment(50, 50));
    }

    @Test
    void paymentWithExtraAmount() {
        assertTrue(paymentService.processPayment(50, 60));
    }

    @Test
    void paymentWithInsufficientAmount() {
        assertFalse(paymentService.processPayment(50, 30));
    }

    @Test
    void paymentWithNegativePaidAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(50, -5));
    }

    @Test
    void paymentWithZeroTotalAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(0, 20));
    }

    @Test
    void paymentWithNegativeTotalAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(-10, 20));
    }
    @Test
    void paymentWithNaNAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(Double.NaN, 50));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(50, Double.NaN));
    }
    @Test
    void paymentWithInfinityAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(
                        Double.POSITIVE_INFINITY, 50));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(
                        50, Double.POSITIVE_INFINITY));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(
                        Double.NEGATIVE_INFINITY, 50));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(
                        50, Double.NEGATIVE_INFINITY));
    } 
}
