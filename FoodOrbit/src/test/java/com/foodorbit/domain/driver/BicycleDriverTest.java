package com.foodorbit.domain.driver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BicycleDriverTest {
	@Test
	void testCanDeliverValidOrder() {
		BicycleDriver driver = new BicycleDriver(
			    30, "Bicycle Driver", "0590000030", 1, 1);
		boolean result = driver.canDeliver(3, 5);
		assertTrue(result);
	}
	@Test
	void testCannotDeliverBeyondMaxDistance() {
		BicycleDriver driver = new BicycleDriver(
			    31, "Bicycle Driver", "0590000031", 1, 1);
		boolean result = driver.canDeliver(7, 5);
		assertFalse(result);
	}
	@Test
	void testCannotDeliverBeyondMaxWeight() {
		BicycleDriver driver = new BicycleDriver(
			    32, "Bicycle Driver", "0590000032", 1, 1);
		boolean result = driver.canDeliver(3, 15);
		assertFalse(result);
	}
	@Test
	void testCanDeliverAtExactLimits() {
		BicycleDriver driver = new BicycleDriver(
			    33, "Bicycle Driver", "0590000033", 1, 1);
		boolean result = driver.canDeliver(5, 10);
		assertTrue(result);
	}
	@Test
	void testCannotDeliverNegativeDistance() {
		BicycleDriver driver = new BicycleDriver(
			    34, "Bicycle Driver", "0590000034", 1, 1);
		boolean result = driver.canDeliver(-2, 5);
		assertFalse(result);
	}
	@Test
	void testCannotDeliverZeroWeight() {
		BicycleDriver driver = new BicycleDriver(
			    35, "Bicycle Driver", "0590000035", 1, 1);
		boolean result = driver.canDeliver(3, 0);
		assertFalse(result);
	}
	@Test
	void testAverageSpeed() {
		BicycleDriver driver = new BicycleDriver(
			    36, "Bicycle Driver", "0590000036", 1, 1);
		double speed = driver.getAverageSpeedKmH();
		assertEquals(15.0, speed);
	}
}
