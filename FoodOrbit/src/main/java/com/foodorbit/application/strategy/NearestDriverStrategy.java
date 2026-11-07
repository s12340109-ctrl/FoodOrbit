
package com.foodorbit.application.strategy;

import java.util.List;

import com.foodorbit.domain.driver.Driver;

public class NearestDriverStrategy implements DriverSelectionStrategy {

    @Override
    public Driver selectDriver(
            List<Driver> eligibleDrivers,
            double pickupX,
            double pickupY) {

        if (!Double.isFinite(pickupX)
                || !Double.isFinite(pickupY)) {
            throw new IllegalArgumentException(
                    "Pickup coordinates must be finite numbers");
        }

        if (eligibleDrivers == null || eligibleDrivers.isEmpty()) {
            return null;
        }

        Driver nearestDriver = null;
        double shortestDistance = Double.POSITIVE_INFINITY;

        for (Driver driver : eligibleDrivers) {

            if (driver == null || !driver.isAvailable()) {
                continue;
            }

            double distance = Math.hypot(
                    driver.getCurrentX() - pickupX,
                    driver.getCurrentY() - pickupY
            );

            if (!Double.isFinite(distance)) {
                continue;
            }

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestDriver = driver;
            }
        }

        return nearestDriver;
    }
}
