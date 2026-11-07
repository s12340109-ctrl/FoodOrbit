
package com.foodorbit.application.pricing;

public class DeliveryFeeCalculator {

    public double calculateDeliveryFee(
            double distanceKm,
            double ratePerKm) {

        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException(
                    "Distance must be a finite non-negative number");
        }

        if (!Double.isFinite(ratePerKm) || ratePerKm < 0) {
            throw new IllegalArgumentException(
                    "Rate must be a finite non-negative number");
        }

        double deliveryFee = distanceKm * ratePerKm;

        if (!Double.isFinite(deliveryFee)) {
            throw new IllegalArgumentException(
                    "Calculated delivery fee is too large");
        }

        return deliveryFee;
    }
}
