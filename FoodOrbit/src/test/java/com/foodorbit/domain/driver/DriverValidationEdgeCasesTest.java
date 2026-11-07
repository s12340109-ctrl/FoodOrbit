package com.foodorbit.domain.driver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DriverValidationEdgeCasesTest {

    @Test
    void driverConstructorValidatesIdentityAndCoordinates() {
        assertThrows(IllegalArgumentException.class,
                () -> new MotorcycleDriver(0, "Name", "0591", 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MotorcycleDriver(1, null, "0591", 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MotorcycleDriver(1, " ", "0591", 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MotorcycleDriver(1, "Name", null, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MotorcycleDriver(1, "Name", " ", 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MotorcycleDriver(1, "Name", "0591", Double.NaN, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MotorcycleDriver(1, "Name", "0591", 0, Double.POSITIVE_INFINITY));

        Driver driver = new MotorcycleDriver(2, " Name ", " 0592 ", 1, 2);
        assertEquals("Name", driver.getName());
        assertEquals("0592", driver.getPhoneNumber());
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());
        assertTrue(driver.isAvailable());
        assertEquals(0, driver.getCompletedDeliveries());
    }

    @Test
    void driverStatusAndLocationUpdatesValidateInputs() {
        Driver driver = new MotorcycleDriver(3, "Driver", "0593", 1, 2);
        assertThrows(IllegalArgumentException.class, () -> driver.updateStatus(null));
        assertThrows(IllegalArgumentException.class, () -> driver.updateLocation(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> driver.updateLocation(0, Double.NEGATIVE_INFINITY));

        driver.updateStatus(DriverStatus.OFFLINE);
        assertFalse(driver.isAvailable());
        driver.updateLocation(4, 5);
        assertEquals(4, driver.getCurrentX());
        assertEquals(5, driver.getCurrentY());
        driver.updateStatus(DriverStatus.AVAILABLE);
        assertTrue(driver.isAvailable());
    }

    @Test
    void completedDeliveriesIncreaseForEachSuccessfulDelivery() {
        Driver driver = new MotorcycleDriver(4, "Driver", "0594", 0, 0);
        driver.completeDelivery();
        driver.completeDelivery();
        assertEquals(2, driver.getCompletedDeliveries());
    }

    @Test
    void electricVehicleValidatesBatteryAndDeliveryBoundaries() {
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricVehicleDriver(5, "EV", "0595", 0, 0, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricVehicleDriver(6, "EV", "0596", 0, 0, 101));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricVehicleDriver(7, "EV", "0597", 0, 0, Double.NaN));

        ElectricVehicleDriver emptyBattery =
                new ElectricVehicleDriver(8, "Empty", "0598", 0, 0, 0);
        assertFalse(emptyBattery.canDeliver(0.1, 1));
        assertFalse(emptyBattery.canDeliver(-1, 1));

        ElectricVehicleDriver fullBattery =
                new ElectricVehicleDriver(9, "Full", "0599", 0, 0, 100);
        assertTrue(fullBattery.canDeliver(200, 80));
        assertFalse(fullBattery.canDeliver(200.1, 1));
        assertFalse(fullBattery.canDeliver(1, 0));
        assertFalse(fullBattery.canDeliver(1, 80.1));
        assertEquals(100, fullBattery.getBatteryLevel());
        assertEquals(45, fullBattery.getAverageSpeedKmH());
    }
}
