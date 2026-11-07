
package com.foodorbit.application.strategy;

import java.util.List;

import com.foodorbit.domain.driver.Driver;

public class FastestDriverStrategy implements DriverSelectionStrategy {

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

        Driver fastestDriver = null;
        double shortestTime = Double.POSITIVE_INFINITY;

        for (Driver driver : eligibleDrivers) {

            if (driver == null || !driver.isAvailable()) {
                continue;
            }

            double speed = driver.getAverageSpeedKmH();

            if (!Double.isFinite(speed) || speed <= 0) {
                continue;
            }

            double distance = Math.hypot(
                    driver.getCurrentX() - pickupX,
                    driver.getCurrentY() - pickupY
            );

            if (!Double.isFinite(distance)) {
                continue;
            }

            double estimatedTime = distance / speed;

            if (Double.isFinite(estimatedTime)
                    && estimatedTime < shortestTime) {

                shortestTime = estimatedTime;
                fastestDriver = driver;
            }
        }

        return fastestDriver;
    }
}
