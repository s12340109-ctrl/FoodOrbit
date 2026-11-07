package com.foodorbit.application.pricing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PricingStrategyTest {

    @Test
    void normalPricing() {
        PricingStrategy strategy = new NormalPricingStrategy();

        assertEquals(20.0, strategy.calculatePrice(20), 0.001);

        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculatePrice(-5));
    }

    @Test
    void peakHourPricing() {
        PricingStrategy strategy = new PeakHourPricingStrategy(1.5);

        assertEquals(30.0, strategy.calculatePrice(20), 0.001);

        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculatePrice(-5));

        assertThrows(IllegalArgumentException.class,
                () -> new PeakHourPricingStrategy(0.5));
    }

    @Test
    void expressPricing() {
        PricingStrategy strategy = new ExpressPricingStrategy(5);

        assertEquals(25.0, strategy.calculatePrice(20), 0.001);

        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculatePrice(-5));

        assertThrows(IllegalArgumentException.class,
                () -> new ExpressPricingStrategy(-2));
    }

    @Test
    void promotionalPricing() {
        PricingStrategy strategy = new PromotionalPricingStrategy(25);

        assertEquals(15.0, strategy.calculatePrice(20), 0.001);

        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculatePrice(-5));

        assertThrows(IllegalArgumentException.class,
                () -> new PromotionalPricingStrategy(-10));

        assertThrows(IllegalArgumentException.class,
                () -> new PromotionalPricingStrategy(110));
    }
}