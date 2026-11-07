
package com.foodorbit.application.service;
import com.foodorbit.domain.model.Order;
import java.util.List;
import com.foodorbit.domain.order.OrderType;
import com.foodorbit.application.strategy.DriverStrategyFactory;
import java.util.ArrayList;

import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.driver.DriverStatus;
import com.foodorbit.application.strategy.DriverSelectionStrategy;

/**
 * Manages driver assignment for customer delivery orders.
 *
 * Selects eligible and available drivers based on delivery
 * distance, order weight, and the selected assignment strategy.
 *
 * Supports automatic strategy selection according to order type
 * and time-sensitive deliveries, such as ice cream orders.
 *
 * @author FoodOrbit Team
 * @version 1.0
 */

public class DriverAssignmentService {
	private final DriverSelectionStrategy strategy;

/**
 * Creates a driver assignment service using the specified
 * driver selection strategy.
 *
 * @param strategy the strategy used to select an eligible driver
 * @throws IllegalArgumentException if the strategy is null
 */

	public DriverAssignmentService(DriverSelectionStrategy strategy) {
	    if (strategy == null) {
	        throw new IllegalArgumentException("Strategy cannot be null");
	    }

	    this.strategy = strategy;
	}

/**
 * Assigns an eligible and available driver to a delivery order.
 *
 * Filters drivers based on their availability, maximum delivery
 * distance, and supported order weight. The configured selection
 * strategy chooses a driver from the eligible candidates.
 *
 * The selected driver's status is changed to BUSY.
 *
 * @param drivers the list of candidate drivers
 * @param pickupX the X coordinate of the pickup location
 * @param pickupY the Y coordinate of the pickup location
 * @param deliveryDistanceKm the delivery distance in kilometers
 * @param orderWeightKg the weight of the order in kilograms
 * @return the selected driver, or null if no eligible driver is found
 */

	public Driver assignDriver(List<Driver> drivers,
            double pickupX,
            double pickupY,
            double deliveryDistanceKm,
            double orderWeightKg) {
		if (drivers == null || drivers.isEmpty()) {
		    return null;
		}

		List<Driver> eligibleDrivers = new ArrayList<>();
		for (Driver driver : drivers) {
		    if (driver != null
		            && driver.isAvailable()
		            && driver.canDeliver(deliveryDistanceKm, orderWeightKg)) {

		        eligibleDrivers.add(driver);
		    }
		}
		    Driver selectedDriver = strategy.selectDriver(
		            eligibleDrivers, pickupX, pickupY);

		    if (selectedDriver != null) {
		        selectedDriver.updateStatus(DriverStatus.BUSY);
		    }

		    return selectedDriver;
		}

/**
 * Assigns a driver based on the order type and delivery requirements.
 *
 * Automatically selects the fastest driver strategy for EXPRESS
 * orders or orders containing ice cream. For other order types,
 * the nearest driver strategy is used.
 *
 * Only available drivers who satisfy the delivery distance
 * and order weight requirements are considered.
 * The selected driver's status is changed to BUSY.
 *
 * @param drivers the list of candidate drivers
 * @param pickupX the X coordinate of the pickup location
 * @param pickupY the Y coordinate of the pickup location
 * @param deliveryDistanceKm the delivery distance in kilometers
 * @param orderWeightKg the weight of the order in kilograms
 * @param orderType the type of order (STANDARD, EXPRESS, or SCHEDULED)
 * @param containsIceCream true if the order contains ice cream
 * @return the selected driver, or null if no eligible driver is found
 * @throws IllegalArgumentException if orderType is null
 */


public Driver assignDriverByOrderType(
        List<Driver> drivers,
        double pickupX,
        double pickupY,
        double deliveryDistanceKm,
        double orderWeightKg,
        OrderType orderType,
        boolean containsIceCream) {

    DriverStrategyFactory factory = new DriverStrategyFactory();

    DriverSelectionStrategy selectedStrategy =
            factory.getStrategy(orderType, containsIceCream);

    DriverAssignmentService assignmentService =
            new DriverAssignmentService(selectedStrategy);

    return assignmentService.assignDriver(
            drivers,
            pickupX,
            pickupY,
            deliveryDistanceKm,
            orderWeightKg);
}
public String getAssignmentReason(
        OrderType orderType,
        boolean containsIceCream) {

    DriverStrategyFactory factory =
            new DriverStrategyFactory();

    return factory.getSelectionReason(
            orderType, containsIceCream);
}
public Driver assignDriverToOrder(
        Order order,
        List<Driver> drivers,
        double pickupX,
        double pickupY,
        double deliveryDistanceKm,
        double orderWeightKg,
        boolean containsIceCream) {

    if (order == null) {
        throw new IllegalArgumentException("Order cannot be null");
    }

    if (order.getStatus() != com.foodorbit.domain.model.OrderStatus.READY) {
        throw new IllegalStateException("Order must be READY");
    }

    if (order.getAssignedDriver() != null) {
        throw new IllegalStateException("Order already has a driver");
    }

    Driver driver = assignDriverByOrderType(
            drivers,
            pickupX,
            pickupY,
            deliveryDistanceKm,
            orderWeightKg,
            order.getOrderType(),
            containsIceCream);

    if (driver != null) {
        order.assignDriver(driver);
    }

    return driver;
}
}

