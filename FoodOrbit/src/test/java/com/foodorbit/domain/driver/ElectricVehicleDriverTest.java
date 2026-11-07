package com.foodorbit.domain.driver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ElectricVehicleDriverTest {
	@Test
	void testValidBatteryLevel() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    40, "Electric Driver", "0590000040", 1, 1, 50);
		assertNotNull(driver);
	}
	@Test
	void testCannotAcceptNegativeBattery() {
	    assertThrows(IllegalArgumentException.class, () -> {
	        new ElectricVehicleDriver(
	            41, "Electric Driver", "0590000041", 1, 1, -10);
	    });
	}
	@Test
	void testCannotAcceptBatteryAbove100() {
		assertThrows(IllegalArgumentException.class, () -> {
			new ElectricVehicleDriver(
				    42, "Electric Driver", "0590000042", 1, 1, 120);
		});
	}
	@Test
	void testCannotAcceptNaNBattery() {
		assertThrows(IllegalArgumentException.class, () -> {
			new ElectricVehicleDriver(
				    43, "Electric Driver", "0590000043", 1, 1, Double.NaN);
		});
	}
	@Test
	void testAverageSpeed() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    44, "Electric Driver", "0590000044", 1, 1, 50);
		double speed = driver.getAverageSpeedKmH();
		assertEquals(45.0, speed);
	}
	@Test
	void testCannotDeliverBeyondBatteryRange() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    45, "Electric Driver", "0590000045", 1, 1, 50);
		boolean result = driver.canDeliver(120, 20);
		assertFalse(result);
	}
	@Test
	void testCanDeliverWithinBatteryRange() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    46, "Electric Driver", "0590000046", 1, 1, 50);
		boolean result = driver.canDeliver(80, 20);
		assertTrue(result);
	}
	@Test
	void testCannotDeliverOverweightOrder() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    47, "Electric Driver", "0590000047", 1, 1, 50);
		boolean result = driver.canDeliver(50, 90);
		assertFalse(result);
	}
	@Test
	void testCanDeliverAtExactLimits() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    48, "Electric Driver", "0590000048", 1, 1, 50);
		boolean result = driver.canDeliver(100, 80);
		assertTrue(result);
	}
	@Test
	void testCannotDeliverNegativeDistance() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    49, "Electric Driver", "0590000049", 1, 1, 50);
		boolean result = driver.canDeliver(-5, 20);
		assertFalse(result);
	}
	@Test
	void testCanDeliverZeroWeight() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    50, "Electric Driver", "0590000050", 1, 1, 50);
		boolean result = driver.canDeliver(50, 0);
		assertFalse(result);
	}
	@Test
	void testCannotDeliverNegativeWeight() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    51, "Electric Driver", "0590000051", 1, 1, 50);
		boolean result = driver.canDeliver(50, -5);
		assertFalse(result);
	}
	}


