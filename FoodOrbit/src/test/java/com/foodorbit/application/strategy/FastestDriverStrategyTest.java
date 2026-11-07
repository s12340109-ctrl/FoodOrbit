package com.foodorbit.application.strategy;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.foodorbit.domain.driver.CarDriver;
import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.driver.MotorcycleDriver;
class FastestDriverStrategyTest {

	@Test
	void testSelectFastestDriver() { 
		CarDriver driver1 = new CarDriver(
			    1, "Driver One", "0590000001", 1.1, 0);
		MotorcycleDriver driver2 = new MotorcycleDriver(
			    2, "Driver Two", "0590000002", 1, 0);
		FastestDriverStrategy strategy = new FastestDriverStrategy();
		Driver selectedDriver = strategy.selectDriver(
			    List.of(driver1, driver2), 0, 0);
		assertSame(driver1, selectedDriver);
	}
	@Test
	void testBusyDriverIsNotSelected() {

	}
	@Test
	void testEmptyDriverList() {
		FastestDriverStrategy strategy = new FastestDriverStrategy();
		Driver result = strategy.selectDriver(List.of(), 0, 0);
		assertNull(result);
	}
	@Test
	void testNullDriverList() {
		FastestDriverStrategy strategy = new FastestDriverStrategy();
		Driver result = strategy.selectDriver(null, 0, 0);
		assertNull(result);
	}
	@Test
	void testDriverWithZeroSpeed() {
		Driver driver = Mockito.mock(Driver.class);
		Mockito.when(driver.getAverageSpeedKmH()).thenReturn(0.0);
		FastestDriverStrategy strategy = new FastestDriverStrategy();
		Driver result = strategy.selectDriver(List.of(driver), 0, 0);
		assertNull(result);
	}
	@Test
	void testDriverWithNegativeSpeed() {
		Driver driver = Mockito.mock(Driver.class);
		Mockito.when(driver.getAverageSpeedKmH()).thenReturn(-10.0);
		FastestDriverStrategy strategy = new FastestDriverStrategy();
		Driver result = strategy.selectDriver(List.of(driver), 0, 0);
		assertNull(result);
	}
	@Test
	void testDriverWithNaNSpeed() {
		Driver driver = Mockito.mock(Driver.class);
		Mockito.when(driver.getAverageSpeedKmH()).thenReturn(Double.NaN);
		FastestDriverStrategy strategy = new FastestDriverStrategy();
		Driver result = strategy.selectDriver(List.of(driver), 0, 0);
		assertNull(result);
	}
	@Test
	void testDriverWithInfiniteSpeed() {
		Driver driver = Mockito.mock(Driver.class);
		Mockito.when(driver.getAverageSpeedKmH()).thenReturn(Double.POSITIVE_INFINITY);
		FastestDriverStrategy strategy = new FastestDriverStrategy();
		Driver result = strategy.selectDriver(List.of(driver), 0, 0);
		assertNull(result);
	}
}
