package com.foodorbit.application.strategy;

import java.util.List;

import com.foodorbit.domain.driver.Driver;

public interface DriverSelectionStrategy {

    Driver selectDriver(List<Driver> eligibleDrivers,
                        double pickupX, double pickupY);
}
