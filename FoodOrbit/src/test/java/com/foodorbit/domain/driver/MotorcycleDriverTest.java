package com.foodorbit.domain.driver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MotorcycleDriverTest {

	@Test
	void testCanDeliverValidOrder() {
		MotorcycleDriver driver = new MotorcycleDriver(
			    10, "Motorcycle Driver", "0590000010", 1, 1);
		boolean result = driver.canDeliver(10, 5);
		assertTrue(result);
	}
	@Test
	void testCannotDeliverBeyondMaxDistance() {
		MotorcycleDriver driver = new MotorcycleDriver(
			    11, "Motorcycle Driver", "0590000011", 1, 1);
		boolean result = driver.canDeliver(20, 5);
		assertFalse(result);
	}
	@Test
	void testCannotDeliverBeyondMaxWeight() {
		MotorcycleDriver driver = new MotorcycleDriver(
			    12, "Motorcycle Driver", "0590000012", 1, 1);
		boolean result = driver.canDeliver(10, 25);
		assertFalse(result);
	}
	@Test
	void testCanDeliverAtExactLimits() {
		MotorcycleDriver driver = new MotorcycleDriver(
			    13, "Motorcycle Driver", "0590000013", 1, 1);
		boolean result = driver.canDeliver(15, 20);
		assertTrue(result);
	}
	@Test
	void testCannotDeliverNegativeDistance() {
		MotorcycleDriver driver = new MotorcycleDriver(
			    14, "Motorcycle Driver", "0590000014", 1, 1);
		boolean result = driver.canDeliver(-5, 10);
		assertFalse(result);
	}
	@Test
	void testCannotDeliverZeroWeight() {
		MotorcycleDriver driver = new MotorcycleDriver(
			    15, "Motorcycle Driver", "0590000015", 1, 1);
		boolean result = driver.canDeliver(10, 0);
		assertFalse(result);
	}
	
}
