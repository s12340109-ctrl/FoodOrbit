
package com.foodorbit.application.catalog;
import com.foodorbit.domain.model.Restaurant;
import static org.junit.jupiter.api.Assertions.*;
import com.foodorbit.persistence.RestaurantRepository;
import org.junit.jupiter.api.Test;
class RestaurantServiceTest {

    @Test
    void shouldRejectNullRepository() {

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RestaurantService(null)
        );

        assertEquals(
            "Restaurant repository cannot be null.",
            exception.getMessage()
        );
    }

@Test
void shouldRejectNullRestaurant() {

    RestaurantService service = new RestaurantService();

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.addRestaurant(null)
    );

    assertEquals(
        "Restaurant cannot be null.",
        exception.getMessage()
    );
}

@Test
void shouldAddRestaurantSuccessfully() {

    RestaurantService service = new RestaurantService();

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    service.addRestaurant(restaurant);

    assertEquals(1, service.getAllRestaurants().size());

    assertSame(
        restaurant,
        service.getAllRestaurants().get(0)
    );
}


@Test
void shouldReturnOnlyOpenRestaurants() {

    RestaurantService service = new RestaurantService();

    Restaurant openRestaurant = new Restaurant(
        "R001", "Pizza House", true, true, 20.0, 5.0
    );

    Restaurant closedRestaurant = new Restaurant(
        "R002", "Burger House", false, true, 15.0, 3.0
    );

    service.addRestaurant(openRestaurant);
    service.addRestaurant(closedRestaurant);

    assertEquals(2, service.getAllRestaurants().size());

    assertEquals(1, service.getOpenRestaurants().size());

    assertSame(
        openRestaurant,
        service.getOpenRestaurants().get(0)
    );
}

@Test
void shouldPreventModifyingRestaurantsList() {

    RestaurantService service = new RestaurantService();

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    service.addRestaurant(restaurant);

    assertThrows(
        UnsupportedOperationException.class,
        () -> service.getAllRestaurants().clear()
    );

    assertEquals(1, service.getAllRestaurants().size());
}

@Test
void shouldPreventModifyingOpenRestaurantsList() {

    RestaurantService service = new RestaurantService();

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    service.addRestaurant(restaurant);

    assertThrows(
        UnsupportedOperationException.class,
        () -> service.getOpenRestaurants().clear()
    );

    assertEquals(1, service.getOpenRestaurants().size());

    assertSame(
        restaurant,
        service.getOpenRestaurants().get(0)
    );
}

@Test
void shouldCreateServiceWithValidRepository() {

    RestaurantRepository repository = new RestaurantRepository();

    RestaurantService service = new RestaurantService(repository);

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    service.addRestaurant(restaurant);

    assertEquals(1, repository.findAll().size());

    assertSame(
        restaurant,
        service.getAllRestaurants().get(0)
    );
}



}
