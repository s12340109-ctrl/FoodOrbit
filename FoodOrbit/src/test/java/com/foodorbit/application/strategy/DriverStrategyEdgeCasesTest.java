package com.foodorbit.application.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.foodorbit.domain.driver.CarDriver;
import com.foodorbit.domain.driver.Driver;
import com.foodorbit.domain.driver.DriverStatus;
import com.foodorbit.domain.driver.MotorcycleDriver;

class DriverStrategyEdgeCasesTest {

    @Test
    void nearestStrategyHandlesEmptyNullAndInvalidCoordinates() {
        NearestDriverStrategy strategy = new NearestDriverStrategy();

        assertNull(strategy.selectDriver(null, 0, 0));
        assertNull(strategy.selectDriver(Collections.emptyList(), 0, 0));

        assertThrows(IllegalArgumentException.class,
                () -> strategy.selectDriver(Collections.emptyList(), Double.NaN, 0));

        assertThrows(IllegalArgumentException.class,
                () -> strategy.selectDriver(Collections.emptyList(), 0, Double.POSITIVE_INFINITY));
    }

    @Test
    void nearestStrategySkipsNullBusyAndNonFiniteDistanceCandidates() {
        Driver busy = new CarDriver(1, "Busy", "0591", 1, 1);
        busy.updateStatus(DriverStatus.BUSY);

        Driver nearest = new CarDriver(2, "Nearest", "0592", 2, 2);
        Driver far = new CarDriver(3, "Far", "0593", 8, 8);

        Driver result = new NearestDriverStrategy().selectDriver(
                Arrays.asList(null, busy, far, nearest), 0, 0);

        assertSame(nearest, result);

        Driver huge = new CarDriver(
                4, "Huge", "0594",
                Double.MAX_VALUE, Double.MAX_VALUE);

        assertNull(new NearestDriverStrategy().selectDriver(
                List.of(huge), -Double.MAX_VALUE, -Double.MAX_VALUE));
    }

    @Test
    void fastestStrategyHandlesEmptyNullAndInvalidCoordinates() {
        FastestDriverStrategy strategy = new FastestDriverStrategy();

        assertNull(strategy.selectDriver(null, 0, 0));
        assertNull(strategy.selectDriver(Collections.emptyList(), 0, 0));

        assertThrows(IllegalArgumentException.class,
                () -> strategy.selectDriver(Collections.emptyList(), Double.NEGATIVE_INFINITY, 0));

        assertThrows(IllegalArgumentException.class,
                () -> strategy.selectDriver(Collections.emptyList(), 0, Double.NaN));
    }

    @Test
    void fastestStrategyChoosesMinimumEstimatedTravelTime() {
        Driver nearbySlow = new CarDriver(10, "Nearby", "05910", 10, 0);

        Driver fartherFast = new MotorcycleDriver(
                11, "Fast", "05911", 4, 0);

        Driver busyFast = new MotorcycleDriver(
                12, "Busy", "05912", 1, 0);

        busyFast.updateStatus(DriverStatus.BUSY);

        Driver selected = new FastestDriverStrategy().selectDriver(
                Arrays.asList(null, busyFast, nearbySlow, fartherFast),
                0, 0);

        assertSame(fartherFast, selected);
    }
}