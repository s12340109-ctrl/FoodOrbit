
package com.foodorbit.application.pricing;

public class PeakHourPricingStrategy implements PricingStrategy {

    private final double peakMultiplier;

    public PeakHourPricingStrategy(double peakMultiplier) {

        if (!Double.isFinite(peakMultiplier) || peakMultiplier < 1.0) {
            throw new IllegalArgumentException(
                    "Peak multiplier must be finite and at least 1.0");
        }

        this.peakMultiplier = peakMultiplier;
    }

    @Override
    public double calculatePrice(double basePrice) {

        if (!Double.isFinite(basePrice) || basePrice < 0) {
            throw new IllegalArgumentException(
                    "Base price must be a finite non-negative number");
        }

        double finalPrice = basePrice * peakMultiplier;

        if (!Double.isFinite(finalPrice)) {
            throw new IllegalArgumentException(
                    "Calculated price is too large");
        }

        return finalPrice;
    }
}
