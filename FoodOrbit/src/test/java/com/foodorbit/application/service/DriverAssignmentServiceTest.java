package com.foodorbit.application.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.foodorbit.domain.model.Order;
import com.foodorbit.domain.model.Customer;
import com.foodorbit.domain.model.Restaurant;
import com.foodorbit.domain.model.OrderStatus;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.foodorbit.application.strategy.NearestDriverStrategy;
import com.foodorbit.domain.driver.BicycleDriver;
import com.foodorbit.domain.driver.CarDriver;
import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.driver.DriverStatus;
import com.foodorbit.domain.driver.ElectricVehicleDriver;
import com.foodorbit.domain.driver.MotorcycleDriver;
import com.foodorbit.domain.order.OrderType;
class DriverAssignmentServiceTest {

	@Test
	void testAssignDriver() {
		DriverAssignmentService service =
		        new DriverAssignmentService(new NearestDriverStrategy());
		Driver result = service.assignDriver(
		        Collections.emptyList(), 0, 0, 5, 2);

		assertNull(result);
	}
	@Test
	void testSelectNearestAvailableDriver() {
		CarDriver driver1 = new CarDriver(
		        1, "Driver One", "0590000001", 1, 1);
		CarDriver driver2 = new CarDriver(
		        2, "Driver Two", "0590000002", 5, 5);
		DriverAssignmentService service =
		        new DriverAssignmentService(new NearestDriverStrategy());

		Driver selectedDriver = service.assignDriver(
		        List.of(driver1, driver2), 0, 0, 5, 2);
		assertSame(driver1, selectedDriver);
		assertEquals(DriverStatus.BUSY, selectedDriver.getStatus());
	}
	@Test
	void testBusyDriverIsNotSelected() {
		CarDriver busyDriver = new CarDriver(
			    1, "Busy Driver", "0590000001", 1, 1);

			busyDriver.updateStatus(DriverStatus.BUSY);
			CarDriver availableDriver = new CarDriver(
				    2, "Available Driver", "0590000002", 5, 5);
			DriverAssignmentService service =
				    new DriverAssignmentService(new NearestDriverStrategy());
			Driver selectedDriver = service.assignDriver(
				    List.of(busyDriver, availableDriver), 0, 0, 5, 2);
			assertSame(availableDriver, selectedDriver);
			
	}
	@Test
	void testDriverCannotExceedMaximumDistance() {
		BicycleDriver driver = new BicycleDriver(
			    3, "Bicycle Driver", "0590000003", 1, 1);
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		Driver selectedDriver = service.assignDriver(
			    List.of(driver), 0, 0, 1000, 2);
		assertNull(selectedDriver);
	}
	@Test
	void testDriverCannotExceedMaximumWeight() {
		BicycleDriver driver = new BicycleDriver(
			    4, "Bicycle Driver", "0590000004", 1, 1);
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		Driver selectedDriver = service.assignDriver(
			    List.of(driver), 0, 0, 5, 1000);
		assertNull(selectedDriver);
	}
	@Test
	void testElectricVehicleBatteryRange() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    5, "Electric Driver", "0590000005", 1, 1, 50);
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		Driver selectedDriver = service.assignDriver(
			    List.of(driver), 0, 0, 120, 5);
		assertNull(selectedDriver);
	}
	@Test
	void testElectricVehicleWithEnoughBattery() {
		ElectricVehicleDriver driver = new ElectricVehicleDriver(
			    6, "Electric Driver", "0590000006", 1, 1, 80);
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		Driver selectedDriver = service.assignDriver(
			    List.of(driver), 0, 0, 120, 5);
		assertSame(driver, selectedDriver);
	}
	@Test
	void testNoEligibleDrivers() {
		BicycleDriver driver = new BicycleDriver(
			    7, "Bicycle Driver", "0590000007", 1, 1);
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		Driver selectedDriver = service.assignDriver(
			    List.of(driver), 0, 0, 20, 2);
		assertNull(selectedDriver);
	}
	@Test
	void testDriverBecomesBusyAfterAssignment() {
		CarDriver driver = new CarDriver(
			    8, "Car Driver", "0590000008", 1, 1);
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		Driver selectedDriver = service.assignDriver(
			    List.of(driver), 0, 0, 5, 2);
		assertEquals(DriverStatus.BUSY, driver.getStatus());
	}
	@Test
	void testDriverCannotReceiveTwoOrders() {
		CarDriver driver = new CarDriver(
			    9, "Car Driver", "0590000009", 1, 1);
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		Driver firstOrderDriver = service.assignDriver(
			    List.of(driver), 0, 0, 5, 2);
		Driver secondOrderDriver = service.assignDriver(
			    List.of(driver), 0, 0, 5, 2);
		assertNull(secondOrderDriver);
		assertSame(driver, firstOrderDriver);
	}
	@Test
	void testRejectsNullStrategy() {
		assertThrows(IllegalArgumentException.class, () -> {
		    new DriverAssignmentService(null);
		});
	}
	@Test
	void testAssignDriverWithNullList() {
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		Driver result = service.assignDriver(null, 0, 0, 5, 2);
		assertNull(result);
	}
	@Test
	void testAssignDriverWithNullElement() {
		DriverAssignmentService service =
			    new DriverAssignmentService(new NearestDriverStrategy());
		List<Driver> drivers = new ArrayList<>();
		drivers.add(null);
		Driver result = service.assignDriver(drivers, 0, 0, 5, 2);
		assertNull(result);
	}

@Test
void testStandardOrderSelectsNearestDriver() {

    CarDriver nearDriver = new CarDriver(
            20, "Near Driver", "0590000020", 1, 1);

    CarDriver farDriver = new CarDriver(
            21, "Far Driver", "0590000021", 5, 5);

    DriverAssignmentService service =
            new DriverAssignmentService(
                    new NearestDriverStrategy());

    Driver selectedDriver = service.assignDriverByOrderType(
            List.of(nearDriver, farDriver),
            0, 0,
            5, 2,
            OrderType.STANDARD,
            false);

    assertSame(nearDriver, selectedDriver);
    assertEquals(DriverStatus.BUSY, nearDriver.getStatus());
}

@Test
void testExpressOrderSelectsFastestDriver() {

    BicycleDriver nearDriver = new BicycleDriver(
            22, "Near Bicycle", "0590000022", 2, 0);

    MotorcycleDriver fastDriver = new MotorcycleDriver(
            23, "Fast Motorcycle", "0590000023", 3, 0);

    DriverAssignmentService service =
            new DriverAssignmentService(
                    new NearestDriverStrategy());

    Driver selectedDriver = service.assignDriverByOrderType(
            List.of(nearDriver, fastDriver),
            0, 0,
            5, 2,
            OrderType.EXPRESS,
            false);

    assertSame(fastDriver, selectedDriver);
    assertEquals(DriverStatus.BUSY, fastDriver.getStatus());
}

@Test
void testIceCreamOrderSelectsFastestDriver() {

    BicycleDriver nearDriver = new BicycleDriver(
            24, "Near Bicycle", "0590000024", 2, 0);

    MotorcycleDriver fastDriver = new MotorcycleDriver(
            25, "Fast Motorcycle", "0590000025", 3, 0);

    DriverAssignmentService service =
            new DriverAssignmentService(
                    new NearestDriverStrategy());

    Driver selectedDriver = service.assignDriverByOrderType(
            List.of(nearDriver, fastDriver),
            0, 0,
            5, 2,
            OrderType.STANDARD,
            true);

    assertSame(fastDriver, selectedDriver);
    assertEquals(DriverStatus.BUSY, fastDriver.getStatus());
    assertEquals(DriverStatus.AVAILABLE, nearDriver.getStatus());
}

@Test
void testAssignDriverByOrderTypeRejectsNullType() {

    CarDriver driver = new CarDriver(
            26, "Test Driver", "0590000026", 1, 1);

    DriverAssignmentService service =
            new DriverAssignmentService(
                    new NearestDriverStrategy());

    assertThrows(IllegalArgumentException.class, () ->
            service.assignDriverByOrderType(
                    List.of(driver),
                    0, 0,
                    5, 2,
                    null,
                    false));

    assertEquals(DriverStatus.AVAILABLE, driver.getStatus());
}
@Test
void expressOrderAssignmentReasonIsCorrect() {

    DriverAssignmentService service =
            new DriverAssignmentService(
                    new NearestDriverStrategy());

    String reason = service.getAssignmentReason(
            OrderType.EXPRESS, false);

    assertEquals(
            "Express order: fastest delivery preferred",
            reason);
}
@Test
void expressIceCreamAssignmentReasonIsCorrect() {

    DriverAssignmentService service =
            new DriverAssignmentService(
                    new NearestDriverStrategy());

    String reason = service.getAssignmentReason(
            OrderType.EXPRESS, true);

    assertEquals(
            "Express order with ice cream: fastest delivery preferred",
            reason);
}
@Test
void testAssignDriverToReadyOrder() {

    Customer customer = new Customer(
            "C1", "Sara", "123456");

    Restaurant restaurant = new Restaurant(
            "R1", "FoodOrbit Restaurant",
            true, true, 0.0, 5.0);

    Order order = new Order(
            "O1", customer, restaurant, "Nablus");

    order.setStatus(OrderStatus.CONFIRMED);
    order.setStatus(OrderStatus.PREPARING);
    order.setStatus(OrderStatus.READY);

    CarDriver driver = new CarDriver(
            30, "Test Driver", "0590000030", 1, 1);

    DriverAssignmentService service =
            new DriverAssignmentService(
                    new NearestDriverStrategy());

    Driver selectedDriver = service.assignDriverToOrder(
            order,
            List.of(driver),
            0, 0,
            5, 2,
            false);

    assertSame(driver, selectedDriver);
    assertSame(driver, order.getAssignedDriver());
    assertEquals(DriverStatus.BUSY, driver.getStatus());
}
@Test
void testCannotAssignDriverToPendingOrder() {

    Customer customer = new Customer(
            "C2", "Ahmad", "123456");

    Restaurant restaurant = new Restaurant(
            "R2", "Test Restaurant",
            true, true, 0.0, 5.0);

    Order order = new Order(
            "O2", customer, restaurant, "Nablus");

    CarDriver driver = new CarDriver(
            31, "Test Driver", "0590000031", 1, 1);

    DriverAssignmentService service =
            new DriverAssignmentService(
                    new NearestDriverStrategy());

    assertThrows(IllegalStateException.class, () ->
            service.assignDriverToOrder(
                    order, List.of(driver),
                    0, 0, 5, 2, false));

    assertNull(order.getAssignedDriver());
    assertEquals(DriverStatus.AVAILABLE, driver.getStatus());
}
@Test
void testCannotAssignTwoDriversToSameOrder() {

    Customer customer = new Customer("C3", "Lina", "123456");

    Restaurant restaurant = new Restaurant(
            "R3", "Test Restaurant", true, true, 0.0, 5.0);

    Order order = new Order(
            "O3", customer, restaurant, "Nablus");

    order.setStatus(OrderStatus.CONFIRMED);
    order.setStatus(OrderStatus.PREPARING);
    order.setStatus(OrderStatus.READY);

    CarDriver driver1 = new CarDriver(
            32, "Driver One", "0590000032", 1, 1);

    CarDriver driver2 = new CarDriver(
            33, "Driver Two", "0590000033", 2, 2);

    DriverAssignmentService service =
            new DriverAssignmentService(new NearestDriverStrategy());

    service.assignDriverToOrder(
            order, List.of(driver1), 0, 0, 5, 2, false);

    assertThrows(IllegalStateException.class, () ->
            service.assignDriverToOrder(
                    order, List.of(driver2), 0, 0, 5, 2, false));

    assertSame(driver1, order.getAssignedDriver());
    assertEquals(DriverStatus.AVAILABLE, driver2.getStatus());
}
}

