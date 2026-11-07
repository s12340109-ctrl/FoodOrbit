
package com.foodorbit.application.pricing;

public class PromotionalPricingStrategy implements PricingStrategy {

    private final double discountPercentage;

    public PromotionalPricingStrategy(double discountPercentage) {

        if (!Double.isFinite(discountPercentage)
                || discountPercentage < 0
                || discountPercentage > 100) {

            throw new IllegalArgumentException(
                    "Discount percentage must be between 0 and 100");
        }

        this.discountPercentage = discountPercentage;
    }

    @Override
    public double calculatePrice(double basePrice) {

        if (!Double.isFinite(basePrice) || basePrice < 0) {
            throw new IllegalArgumentException(
                    "Base price must be a finite non-negative number");
        }

        double finalPrice =
                basePrice * (1 - discountPercentage / 100.0);

        if (!Double.isFinite(finalPrice)) {
            throw new IllegalArgumentException(
                    "Calculated price is too large");
        }

        return finalPrice;
    }
}
