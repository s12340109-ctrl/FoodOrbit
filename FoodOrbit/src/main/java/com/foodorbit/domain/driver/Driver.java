
package com.foodorbit.domain.driver;

public abstract class Driver {

    private final int driverId;
    private final String name;
    private final String phoneNumber;

    private DriverStatus status;

    private double currentX;
    private double currentY;

    private int completedDeliveries;

    protected Driver(int driverId, String name,
                     String phoneNumber,
                     double currentX, double currentY) {

        if (driverId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid driver ID");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Driver name is required");
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number is required");
        }

        validateLocation(currentX, currentY);

        this.driverId = driverId;
        this.name = name.trim();
        this.phoneNumber = phoneNumber.trim();
        this.currentX = currentX;
        this.currentY = currentY;
        this.status = DriverStatus.AVAILABLE;
        this.completedDeliveries = 0;
    }

    public int getDriverId() {
        return driverId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public double getCurrentX() {
        return currentX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public int getCompletedDeliveries() {
        return completedDeliveries;
    }

    public boolean isAvailable() {
        return status == DriverStatus.AVAILABLE;
    }

    public void updateStatus(DriverStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Driver status cannot be null");
        }

        this.status = newStatus;
    }

    public void updateLocation(double x, double y) {

        validateLocation(x, y);

        this.currentX = x;
        this.currentY = y;
    }

    private void validateLocation(double x, double y) {

        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Driver coordinates must be finite numbers");
        }
    }

    public void completeDelivery() {

        if (completedDeliveries == Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Completed deliveries limit reached");
        }

        completedDeliveries++;
    }

    public abstract boolean canDeliver(
            double distanceKm,
            double orderWeightKg
    );

    public abstract double getAverageSpeedKmH();
}
