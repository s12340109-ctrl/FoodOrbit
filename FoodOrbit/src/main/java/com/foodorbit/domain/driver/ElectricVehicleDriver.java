package com.foodorbit.domain.driver;

public class ElectricVehicleDriver extends Driver {
	private static final double MAX_RANGE_KM = 200.0;
	private static final double MAX_WEIGHT_KG = 80.0;
	private static final double AVERAGE_SPEED_KMH = 45.0;


	private double batteryLevel;

	public ElectricVehicleDriver(int driverId, String name, String phoneNumber, double currentX, double currentY, double batteryLevel) {
		super(driverId, name, phoneNumber, currentX, currentY);
		if (batteryLevel < 0 || batteryLevel > 100
		        || Double.isNaN(batteryLevel)) {
		    throw new IllegalArgumentException("Invalid battery level");
		}

		this.batteryLevel = batteryLevel;
	}

	@Override
	public boolean canDeliver(double distanceKm, double orderWeightKg) {
		
		double availableRangeKm = MAX_RANGE_KM * batteryLevel / 100.0;

		return distanceKm >= 0
		        && distanceKm <= availableRangeKm
		        && orderWeightKg > 0
		        && orderWeightKg <= MAX_WEIGHT_KG;
	}

	@Override
	public double getAverageSpeedKmH() {
		
		return AVERAGE_SPEED_KMH;
	
	}
	/**
	 * Returns the current battery charge percentage.
	 *
	 * @return battery level from 0 to 100
	 */
	public double getBatteryLevel() {

	    return batteryLevel;
	}
	}
