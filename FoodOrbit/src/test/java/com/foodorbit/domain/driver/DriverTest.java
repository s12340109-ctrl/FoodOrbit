package com.foodorbit.domain.driver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DriverTest {

	@Test
	void testGetDriverId() {
		CarDriver driver = new CarDriver(
			    60, "Test Driver", "0590000060", 1, 1);
		int id = driver.getDriverId();
		assertEquals(60, id);
	}
	@Test
	void testGetName() {
		CarDriver driver = new CarDriver(
			    61, "Test Driver", "0590000061", 1, 1);
		String name = driver.getName();
		assertEquals("Test Driver", name);
	}
	@Test
	void testGetPhoneNumber() {
		CarDriver driver = new CarDriver(
			    62, "Test Driver", "0590000062", 1, 1);
		String phone = driver.getPhoneNumber();
		assertEquals("0590000062", phone);
	}
	@Test
	void testGetCompletedDeliveriesInitiallyZero() {
		CarDriver driver = new CarDriver(
			    63, "Test Driver", "0590000063", 1, 1);
		int completed = driver.getCompletedDeliveries();
		assertEquals(0, completed);
	}
	@Test
	void testCompleteDeliveryIncreasesCount() {
		CarDriver driver = new CarDriver(
			    64, "Test Driver", "0590000064", 1, 1);
		driver.completeDelivery();
		assertEquals(1, driver.getCompletedDeliveries());
	}
	@Test
	void testUpdateLocation() {
		CarDriver driver = new CarDriver(
			    65, "Test Driver", "0590000065", 1, 1);
		driver.updateLocation(5, 8);
		assertEquals(5.0, driver.getCurrentX());
		assertEquals(8.0, driver.getCurrentY());
	}
	@Test
	void testUpdateStatusToBusy() {
		CarDriver driver = new CarDriver(
			    66, "Test Driver", "0590000066", 1, 1);
		driver.updateStatus(DriverStatus.BUSY);
		assertEquals(DriverStatus.BUSY, driver.getStatus());
	}
	@Test
	void testUpdateStatusToAvailable() {
		CarDriver driver = new CarDriver(
			    67, "Test Driver", "0590000067", 1, 1);
		driver.updateStatus(DriverStatus.BUSY);
		driver.updateStatus(DriverStatus.AVAILABLE);
		assertEquals(DriverStatus.AVAILABLE, driver.getStatus());
	}
	@Test
	void testUpdateStatusRejectsNull() {
		CarDriver driver = new CarDriver(
			    68, "Test Driver", "0590000068", 1, 1);
		assertThrows(IllegalArgumentException.class, () -> {
		    driver.updateStatus(null);
		});
	}
	@Test
	void testRejectsZeroDriverId() {
		assertThrows(IllegalArgumentException.class, () -> {
		    new CarDriver(0, "Test Driver", "0590000069", 1, 1);
		});
	}
	@Test
	void testRejectsNegativeDriverId() {
		assertThrows(IllegalArgumentException.class, () -> {
		    new CarDriver(-5, "Test Driver", "0590000070", 1, 1);
		});
	}
	@Test
	void testRejectsNullName() {
		assertThrows(IllegalArgumentException.class, () -> {
		    new CarDriver(71, null, "0590000071", 1, 1);
		});
	}
	@Test
	void testRejectsBlankName() {
		assertThrows(IllegalArgumentException.class, () -> {
		    new CarDriver(72, "", "0590000072", 1, 1);
		});
	}
	@Test
	void testRejectsNullPhoneNumber() {
		assertThrows(IllegalArgumentException.class, () -> {
		    new CarDriver(73, "Test Driver", null, 1, 1);
		});
	}
	@Test
	void testRejectsBlankPhoneNumber() {
		assertThrows(IllegalArgumentException.class, () -> {
		    new CarDriver(74, "Test Driver", "", 1, 1);
		});
	}
	
	}
	


