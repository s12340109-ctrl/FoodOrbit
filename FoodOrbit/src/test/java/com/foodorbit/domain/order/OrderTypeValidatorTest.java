
package com.foodorbit.domain.order;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderTypeValidatorTest {

    private OrderTypeValidator validator;
    private Clock clock;

    @BeforeEach
    void setUp() {
        validator = new OrderTypeValidator();

        clock = Clock.fixed(
                Instant.parse("2026-10-27T10:00:00Z"),
                ZoneId.of("UTC"));
    }

    @Test
    void standardOrderWithoutScheduledTimeIsValid() {
        assertDoesNotThrow(() ->
                validator.validate(OrderType.STANDARD, null, clock));
    }

    @Test
    void expressOrderWithoutScheduledTimeIsValid() {
        assertDoesNotThrow(() ->
                validator.validate(OrderType.EXPRESS, null, clock));
    }

    @Test
    void standardOrderWithScheduledTimeIsRejected() {
        LocalDateTime future =
                LocalDateTime.now(clock).plusHours(2);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validate(OrderType.STANDARD, future, clock));
    }

    @Test
    void expressOrderWithScheduledTimeIsRejected() {
        LocalDateTime future =
                LocalDateTime.now(clock).plusHours(2);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validate(OrderType.EXPRESS, future, clock));
    }

    @Test
    void scheduledOrderWithFutureTimeIsValid() {
        LocalDateTime future =
                LocalDateTime.now(clock).plusHours(2);

        assertDoesNotThrow(() ->
                validator.validate(OrderType.SCHEDULED, future, clock));
    }

    @Test
    void scheduledOrderWithoutTimeIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validate(OrderType.SCHEDULED, null, clock));
    }

    @Test
    void scheduledOrderWithPastTimeIsRejected() {
        LocalDateTime past =
                LocalDateTime.now(clock).minusHours(1);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validate(OrderType.SCHEDULED, past, clock));
    }

    @Test
    void scheduledOrderWithCurrentTimeIsRejected() {
        LocalDateTime now = LocalDateTime.now(clock);

        assertThrows(IllegalArgumentException.class, () ->
                validator.validate(OrderType.SCHEDULED, now, clock));
    }

    @Test
    void nullOrderTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validate(null, null, clock));
    }

    @Test
    void nullClockIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validate(OrderType.STANDARD, null, null));
    }
}
