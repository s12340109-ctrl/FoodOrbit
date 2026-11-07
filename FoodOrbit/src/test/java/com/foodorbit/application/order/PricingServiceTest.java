package com.foodorbit.application.order;

import com.foodorbit.domain.order.OrderType;
import com.foodorbit.domain.model.Customer;
import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.model.Order;
import com.foodorbit.domain.model.OrderItem;
import com.foodorbit.domain.model.Restaurant;

import com.foodorbit.application.pricing.NormalPricingStrategy;
import com.foodorbit.application.pricing.PeakHourPricingStrategy;
import com.foodorbit.application.pricing.ExpressPricingStrategy;
import com.foodorbit.application.pricing.PromotionalPricingStrategy;
import com.foodorbit.application.pricing.PricingStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private PricingService pricingService;
    private Order order;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();

        Customer customer = new Customer(
                "C1", "yara", "1234");

        Restaurant restaurant = new Restaurant(
                "R1", "Test Restaurant", true, true, 0.0,
                5.0);

        order = new Order(
                "O1", customer, restaurant, "Test Address");

        MenuItem item = new MenuItem(
                "M1", "Pizza", 100.0, true);

        order.addItem(new OrderItem(item, 1));
    }

    @Test
    void calculatePriceWithNormalStrategyReturnsBaseTotal() {
        double result = pricingService.calculatePrice(
                order, new NormalPricingStrategy());

        assertEquals(100.0, result, 0.001);
    }

    @Test
    void calculatePriceRejectsNullOrder() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingService.calculatePrice(
                        null, new NormalPricingStrategy()));
    }

    @Test
    void calculatePriceRejectsNullStrategy() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingService.calculatePrice(order, null));
    }

    @Test
    void calculatePriceRejectsInvalidStrategyResult() {
        PricingStrategy invalidStrategy = price -> Double.NaN;

        assertThrows(
                IllegalArgumentException.class,
                () -> pricingService.calculatePrice(
                        order, invalidStrategy));
    }

    @Test
    void calculateDeliveryFeeUsesDistanceAndRate() {
        PricingService service = new PricingService(2.0);

        double result = service.calculateDeliveryFee(order);

        assertEquals(10.0, result, 0.001);
    }

    @Test
    void calculateDeliveryFeeWithDefaultRateReturnsZero() {
        double result = pricingService.calculateDeliveryFee(order);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    void calculateDeliveryFeeRejectsNullOrder() {
        assertThrows(
                IllegalArgumentException.class,
                () -> pricingService.calculateDeliveryFee(null));
    }

    @Test
    void calculateFinalTotalIncludesFoodAndDeliveryFeeOnce() {
        PricingService service = new PricingService(2.0);

        double result = service.calculateFinalTotal(
                order, new NormalPricingStrategy());

        assertEquals(110.0, result, 0.001);
    }

    @Test
    void calculateFinalTotalAppliesPricingStrategyToFoodOnly() {
        PricingService service = new PricingService(2.0);

        PricingStrategy discountStrategy = price -> price * 0.5;

        double result = service.calculateFinalTotal(
                order, discountStrategy);

        assertEquals(60.0, result, 0.001);
    }

    @Test
    void constructorRejectsNegativeDeliveryRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PricingService(-1.0));
    }

    @Test
    void constructorRejectsInfiniteDeliveryRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PricingService(Double.POSITIVE_INFINITY));
    }

    @Test
    void expressOrderIncludesAdditionalDeliveryFee() {
        PricingService service = new PricingService(2.0, 7.0);
        order.setOrderType(com.foodorbit.domain.order.OrderType.EXPRESS);

        double result = service.calculateDeliveryFee(order);

        assertEquals(17.0, result, 0.001);
    }

    @Test
    void standardOrderDoesNotIncludeExpressFee() {
        PricingService service = new PricingService(2.0, 7.0);
        order.setOrderType(com.foodorbit.domain.order.OrderType.STANDARD);

        double result = service.calculateDeliveryFee(order);

        assertEquals(10.0, result, 0.001);
    }

    @Test
    void expressFinalTotalIncludesAdditionalFeeOnce() {
        PricingService service = new PricingService(2.0, 7.0);
        order.setOrderType(com.foodorbit.domain.order.OrderType.EXPRESS);

        double result = service.calculateFinalTotal(
                order, new NormalPricingStrategy());

        assertEquals(117.0, result, 0.001);
    }

    @Test
    void constructorRejectsNegativeExpressDeliveryFee() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PricingService(2.0, -1.0));
    }

    @Test
    void constructorRejectsInfiniteExpressDeliveryFee() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PricingService(
                        2.0, Double.POSITIVE_INFINITY));
    }

@Test
void calculatePriceWithPeakHourStrategyAppliesIncrease() {
    PricingService service = new PricingService();

    double result = service.calculatePrice(
            order, new PeakHourPricingStrategy(1.20));

    assertEquals(120.0, result, 0.001);
}

@Test
void calculatePriceWithExpressStrategyAppliesIncrease() {
    PricingService service = new PricingService();

    double result = service.calculatePrice(
            order, new ExpressPricingStrategy(25.0));

    assertEquals(125.0, result, 0.001);
}

@Test
void calculatePriceWithPromotionalStrategyAppliesDiscount() {
    PricingService service = new PricingService();

    double result = service.calculatePrice(
            order, new PromotionalPricingStrategy(20));

    assertEquals(80.0, result, 0.001);
}

@Test
void calculateFinalTotalRejectsInvalidStrategyResult() {
    PricingService service = new PricingService();

    PricingStrategy invalidStrategy =
            price -> Double.POSITIVE_INFINITY;

    assertThrows(
            IllegalArgumentException.class,
            () -> service.calculateFinalTotal(
                    order, invalidStrategy));
}

@Test
void constructorRejectsNaNDeliveryRate() {
    assertThrows(
            IllegalArgumentException.class,
            () -> new PricingService(Double.NaN));
}

@Test
void constructorRejectsNaNExpressDeliveryFee() {
    assertThrows(
            IllegalArgumentException.class,
            () -> new PricingService(2.0, Double.NaN));
}

@Test
void promotionalStrategyAllowsFullDiscount() {
    PricingService service = new PricingService();

    double result = service.calculatePrice(
            order, new PromotionalPricingStrategy(100));

    assertEquals(0.0, result, 0.001);
}

@Test
void expressOrderAddsDeliverySurchargeOnlyOnce() {
    PricingService service = new PricingService(2.0, 7.0);
    order.setOrderType(OrderType.EXPRESS);

    double result = service.calculateFinalTotal(
            order, new NormalPricingStrategy());

    assertEquals(117.0, result, 0.001);
}
@Test
void scheduledOrderDoesNotIncludeExpressFee() {
    PricingService service = new PricingService(2.0, 7.0);
    order.setOrderType(OrderType.SCHEDULED);

    double result = service.calculateDeliveryFee(order);

    assertEquals(10.0, result, 0.001);
}
@Test
void calculateDeliveryFeeRejectsNullRestaurant() throws Exception {
    java.lang.reflect.Field field =
            Order.class.getDeclaredField("restaurant");

    field.setAccessible(true);
    field.set(order, null);

    assertThrows(
            IllegalArgumentException.class,
            () -> pricingService.calculateDeliveryFee(order));
}
@Test
void calculateDeliveryFeeRejectsInfiniteResult() {
    Restaurant farRestaurant = new Restaurant(
            "R2",
            "Far Restaurant",
            true,
            true,
            0.0,
            Double.MAX_VALUE);

    Customer customer = new Customer("C2", "Test", "1234");

    Order largeOrder = new Order(
            "O2", customer, farRestaurant, "Test Address");

    PricingService service =
            new PricingService(Double.MAX_VALUE, 0.0);

    assertThrows(
            IllegalArgumentException.class,
            () -> service.calculateDeliveryFee(largeOrder));
}
@Test
void calculateFinalTotalRejectsInfiniteResult() {
    PricingService service =
            new PricingService(Double.MAX_VALUE, 0.0);

    PricingStrategy maximumPriceStrategy =
            price -> Double.MAX_VALUE;

    assertThrows(
            IllegalArgumentException.class,
            () -> service.calculateFinalTotal(
                    order, maximumPriceStrategy));
}
@Test
void calculatePriceRejectsNegativeStrategyResult() {
    PricingStrategy negativeStrategy = price -> -1.0;

    assertThrows(IllegalArgumentException.class,
            () -> pricingService.calculatePrice(order, negativeStrategy));
}
@Test
void calculateFinalTotalRejectsNegativeTotal() {
    PricingStrategy negativeStrategy = price -> -1.0;

    assertThrows(IllegalArgumentException.class,
            () -> pricingService.calculateFinalTotal(order, negativeStrategy));
}
@Test
void calculateDeliveryFeeAcceptsZero() {
    PricingService service = new PricingService(0.0, 0.0);

    assertEquals(0.0, service.calculateDeliveryFee(order));
}
@Test
void calculateFinalTotalAcceptsZero() {
    PricingStrategy zeroStrategy = price -> 0.0;

    PricingService service = new PricingService(0.0, 0.0);

    assertEquals(0.0,
            service.calculateFinalTotal(order, zeroStrategy));
}
@Test
void calculateDeliveryFeeRejectsNegativeResult() throws Exception {
    java.lang.reflect.Field field =
            PricingService.class.getDeclaredField(
                    "deliveryRatePerKilometer");

    field.setAccessible(true);
    field.setDouble(pricingService, -1.0);

    assertThrows(
            IllegalArgumentException.class,
            () -> pricingService.calculateDeliveryFee(order));
}
@Test
void calculateFinalTotalRejectsNegativeResult() throws Exception {
    java.lang.reflect.Field field =
            PricingService.class.getDeclaredField(
                    "deliveryRatePerKilometer");

    field.setAccessible(true);
    field.setDouble(pricingService, -1.0);

    PricingStrategy zeroStrategy = price -> 0.0;

    assertThrows(
            IllegalArgumentException.class,
            () -> pricingService.calculateFinalTotal(
                    order, zeroStrategy));
}
@Test
void calculateFinalTotalRejectsOverflowFromFiniteValues() {

    PricingService service = new PricingService() {
        @Override
        public double calculatePrice(
                Order order, PricingStrategy strategy) {
            return Double.MAX_VALUE;
        }

        @Override
        public double calculateDeliveryFee(Order order) {
            return Double.MAX_VALUE;
        }
    };

    assertThrows(
            IllegalArgumentException.class,
            () -> service.calculateFinalTotal(
                    order, new NormalPricingStrategy()));
}
@Test
void calculateFinalTotalRejectsNegativeValueDirectly() {

    PricingService service = new PricingService() {
        @Override
        public double calculatePrice(
                Order order, PricingStrategy strategy) {
            return -1.0;
        }

        @Override
        public double calculateDeliveryFee(Order order) {
            return 0.0;
        }
    };

    assertThrows(
            IllegalArgumentException.class,
            () -> service.calculateFinalTotal(
                    order, new NormalPricingStrategy()));
}
}
