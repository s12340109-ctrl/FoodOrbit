
package com.foodorbit.application.pricing;

public class ExpressPricingStrategy implements PricingStrategy {

    private final double expressFee;

    public ExpressPricingStrategy(double expressFee) {

        if (!Double.isFinite(expressFee) || expressFee < 0) {
            throw new IllegalArgumentException(
                    "Express fee must be a finite non-negative number");
        }

        this.expressFee = expressFee;
    }

    @Override
    public double calculatePrice(double basePrice) {

        if (!Double.isFinite(basePrice) || basePrice < 0) {
            throw new IllegalArgumentException(
                    "Base price must be a finite non-negative number");
        }

        double finalPrice = basePrice + expressFee;

        if (!Double.isFinite(finalPrice)) {
            throw new IllegalArgumentException(
                    "Calculated price is too large");
        }

        return finalPrice;
    }
}
