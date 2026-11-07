package com.foodorbit.application.strategy;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.foodorbit.domain.driver.Driver;

class NearestDriverStrategyTest {

	@Test
	void testNullDriverList() {
		NearestDriverStrategy strategy = new NearestDriverStrategy();
		Driver result = strategy.selectDriver(null, 0, 0);
		assertNull(result);
	}

}
