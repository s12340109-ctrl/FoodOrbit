package com.foodorbit.domain.driver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CarDriverTest {

	@Test
	void testCanDeliverValidOrder() {
		CarDriver driver = new CarDriver(
			    20, "Car Driver", "0590000020", 1, 1);
		boolean result = driver.canDeliver(20, 30);
		assertTrue(result);
	}
	@Test
	void testCannotDeliverBeyondMaxDistance() {
		CarDriver driver = new CarDriver(
			    21, "Car Driver", "0590000021", 1, 1);
		boolean result = driver.canDeliver(60, 30);
		assertFalse(result);
	}
	@Test
	void testCannotDeliverBeyondMaxWeight() {
		CarDriver driver = new CarDriver(
			    22, "Car Driver", "0590000022", 1, 1);
		boolean result = driver.canDeliver(20, 120);
		assertFalse(result);
	}
	@Test
	void testCanDeliverAtExactLimits() {
		CarDriver driver = new CarDriver(
			    23, "Car Driver", "0590000023", 1, 1);
		boolean result = driver.canDeliver(50, 100);
		assertTrue(result);
	}
		@Test
		void testCannotDeliverNegativeDistance() {
			CarDriver driver = new CarDriver(
				    24, "Car Driver", "0590000024", 1, 1);
			boolean result = driver.canDeliver(-5, 30);
			assertFalse(result);
			
	}
		@Test
		void testCannotDeliverZeroWeight() {
			CarDriver driver = new CarDriver(
				    25, "Car Driver", "0590000025", 1, 1);
			boolean result = driver.canDeliver(20, 0);
			assertFalse(result);
		}
		
	}


