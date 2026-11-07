package com.foodorbit.application.pricing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PricingEdgeCasesTest {

    @Test
    void deliveryFeeRejectsNonFiniteInputsAndOverflow() {
        DeliveryFeeCalculator calculator = new DeliveryFeeCalculator();
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDeliveryFee(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDeliveryFee(Double.POSITIVE_INFINITY, 1));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDeliveryFee(1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDeliveryFee(1, Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateDeliveryFee(Double.MAX_VALUE, Double.MAX_VALUE));
        assertEquals(0, calculator.calculateDeliveryFee(0, 0));
    }

    @Test
    void promotionalPricingValidatesDiscountAndBasePrice() {
        assertThrows(IllegalArgumentException.class, () -> new PromotionalPricingStrategy(-0.01));
        assertThrows(IllegalArgumentException.class, () -> new PromotionalPricingStrategy(100.01));
        assertThrows(IllegalArgumentException.class, () -> new PromotionalPricingStrategy(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new PromotionalPricingStrategy(Double.POSITIVE_INFINITY));

        PromotionalPricingStrategy noDiscount = new PromotionalPricingStrategy(0);
        PromotionalPricingStrategy fullDiscount = new PromotionalPricingStrategy(100);
        assertEquals(100, noDiscount.calculatePrice(100));
        assertEquals(0, fullDiscount.calculatePrice(100));
        assertThrows(IllegalArgumentException.class, () -> noDiscount.calculatePrice(-1));
        assertThrows(IllegalArgumentException.class, () -> noDiscount.calculatePrice(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> noDiscount.calculatePrice(Double.MAX_VALUE * 2));
    }

    @Test
    void expressPricingValidatesFeeAndBasePrice() {
        assertThrows(IllegalArgumentException.class, () -> new ExpressPricingStrategy(-1));
        assertThrows(IllegalArgumentException.class, () -> new ExpressPricingStrategy(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new ExpressPricingStrategy(Double.POSITIVE_INFINITY));

        ExpressPricingStrategy freeExpress = new ExpressPricingStrategy(0);
        assertEquals(20, freeExpress.calculatePrice(20));
        assertThrows(IllegalArgumentException.class, () -> freeExpress.calculatePrice(-1));
        assertThrows(IllegalArgumentException.class, () -> freeExpress.calculatePrice(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> new ExpressPricingStrategy(Double.MAX_VALUE).calculatePrice(Double.MAX_VALUE));
    }

    @Test
    void peakHourPricingValidatesMultiplierAndBasePrice() {
        assertThrows(IllegalArgumentException.class, () -> new PeakHourPricingStrategy(0.99));
        assertThrows(IllegalArgumentException.class, () -> new PeakHourPricingStrategy(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new PeakHourPricingStrategy(Double.POSITIVE_INFINITY));

        PeakHourPricingStrategy normalMultiplier = new PeakHourPricingStrategy(1);
        assertEquals(20, normalMultiplier.calculatePrice(20));
        assertThrows(IllegalArgumentException.class, () -> normalMultiplier.calculatePrice(-1));
        assertThrows(IllegalArgumentException.class, () -> normalMultiplier.calculatePrice(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new PeakHourPricingStrategy(2).calculatePrice(Double.MAX_VALUE));
    }

    @Test
    void normalPricingRejectsInvalidBasePrices() {
        NormalPricingStrategy strategy = new NormalPricingStrategy();
        assertEquals(0, strategy.calculatePrice(0));
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrice(-1));
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrice(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> strategy.calculatePrice(Double.NEGATIVE_INFINITY));
    }
}
