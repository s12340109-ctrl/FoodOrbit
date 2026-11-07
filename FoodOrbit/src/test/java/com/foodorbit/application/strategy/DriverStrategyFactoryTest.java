
package com.foodorbit.application.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.foodorbit.domain.order.OrderType;

class DriverStrategyFactoryTest {

    private DriverStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DriverStrategyFactory();
    }

    @Test
    void standardOrderUsesNearestStrategy() {
        DriverSelectionStrategy strategy =
                factory.getStrategy(OrderType.STANDARD, false);

        assertInstanceOf(NearestDriverStrategy.class, strategy);
    }

    @Test
    void expressOrderUsesFastestStrategy() {
        DriverSelectionStrategy strategy =
                factory.getStrategy(OrderType.EXPRESS, false);

        assertInstanceOf(FastestDriverStrategy.class, strategy);
    }

    @Test
    void scheduledOrderUsesNearestStrategy() {
        DriverSelectionStrategy strategy =
                factory.getStrategy(OrderType.SCHEDULED, false);

        assertInstanceOf(NearestDriverStrategy.class, strategy);
    }

    @Test
    void iceCreamOrderUsesFastestStrategy() {
        DriverSelectionStrategy strategy =
                factory.getStrategy(OrderType.STANDARD, true);

        assertInstanceOf(FastestDriverStrategy.class, strategy);
    }

    @Test
    void scheduledIceCreamOrderUsesFastestStrategy() {
        DriverSelectionStrategy strategy =
                factory.getStrategy(OrderType.SCHEDULED, true);

        assertInstanceOf(FastestDriverStrategy.class, strategy);
    }

    @Test
    void nullOrderTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                factory.getStrategy(null, false));
    }

    @Test
    void nullOrderTypeWithIceCreamIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                factory.getStrategy(null, true));
    }
    @Test
    void expressIceCreamOrderUsesFastestStrategy() {

        DriverSelectionStrategy strategy =
                factory.getStrategy(OrderType.EXPRESS, true);

        assertInstanceOf(FastestDriverStrategy.class, strategy);
    }
    @Test
    void expressIceCreamSelectionReasonIsCorrect() {

        String reason = factory.getSelectionReason(
                OrderType.EXPRESS, true);

        assertEquals(
                "Express order with ice cream: fastest delivery preferred",
                reason);
    }
    @Test
    void expressOrderSelectionReasonIsCorrect() {

        String reason = factory.getSelectionReason(
                OrderType.EXPRESS, false);

        assertEquals(
                "Express order: fastest delivery preferred",
                reason);
    }
    @Test
    void iceCreamSelectionReasonIsCorrect() {

        String reason = factory.getSelectionReason(
                OrderType.STANDARD, true);

        assertEquals(
                "Ice cream order: fastest delivery preferred",
                reason);
    }
    @Test
    void regularOrderSelectionReasonIsCorrect() {

        String reason = factory.getSelectionReason(
                OrderType.STANDARD, false);

        assertEquals(
                "Regular order: nearest driver preferred",
                reason);
    }
    @Test
    void nullOrderTypeSelectionReasonIsRejected() {

        assertThrows(IllegalArgumentException.class, () ->
                factory.getSelectionReason(null, false));
    }
}
