package com.foodorbit.domain.driver;

public class CarDriver extends Driver {
	private static final double MAX_DISTANCE_KM = 50.0;
	private static final double MAX_WEIGHT_KG = 100.0;
	private static final double AVERAGE_SPEED_KMH = 50.0;
	public CarDriver(int driverId, String name, String phoneNumber, double currentX, double currentY) {
		super(driverId, name, phoneNumber, currentX, currentY);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canDeliver(double distanceKm, double orderWeightKg) {
		// TODO Auto-generated method stub
		return distanceKm >= 0
		        && distanceKm <= MAX_DISTANCE_KM
		        && orderWeightKg > 0
		        && orderWeightKg <= MAX_WEIGHT_KG;
	}

	@Override
	public double getAverageSpeedKmH() {
		// TODO Auto-generated method stub
		return AVERAGE_SPEED_KMH;
	}

}
