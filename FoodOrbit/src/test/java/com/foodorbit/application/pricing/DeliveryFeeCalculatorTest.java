package com.foodorbit.application.pricing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DeliveryFeeCalculatorTest {

    private final DeliveryFeeCalculator calculator =
            new DeliveryFeeCalculator();

    @Test
    void calculateDeliveryFee_validDistance() {
        double result = calculator.calculateDeliveryFee(5, 2);

        assertEquals(10.0, result, 0.001);
    }

    @Test
    void calculateDeliveryFee_zeroDistance() {
        double result = calculator.calculateDeliveryFee(0, 2);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    void calculateDeliveryFee_negativeDistance() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateDeliveryFee(-5, 2);
        });
    }

    @Test
    void calculateDeliveryFee_negativeRate() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateDeliveryFee(5, -2);
        });
    }
}