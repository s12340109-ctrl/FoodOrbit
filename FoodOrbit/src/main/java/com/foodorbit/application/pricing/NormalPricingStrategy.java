
package com.foodorbit.application.pricing;

public class NormalPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(double basePrice) {

        if (!Double.isFinite(basePrice) || basePrice < 0) {
            throw new IllegalArgumentException(
                    "Base price must be a finite non-negative number");
        }

        return basePrice;
    }
}
