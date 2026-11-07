
package com.foodorbit.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.foodorbit.domain.model.Restaurant;

class RestaurantRepositoryTest {

    @Test
    void shouldSaveRestaurantSuccessfully() {

        RestaurantRepository repository = new RestaurantRepository();

        Restaurant restaurant = new Restaurant(
            "R001",
            "Pizza House",
            true,
            true,
            20.0,
            5.0
        );

        repository.save(restaurant);

        assertEquals(1, repository.findAll().size());

        assertSame(
            restaurant,
            repository.findById("R001").orElse(null)
        );
    }

@Test
void shouldRejectDuplicateRestaurantId() {

    RestaurantRepository repository = new RestaurantRepository();

    Restaurant firstRestaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    Restaurant secondRestaurant = new Restaurant(
        "R001",
        "Burger House",
        false,
        true,
        15.0,
        3.0
    );

    repository.save(firstRestaurant);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> repository.save(secondRestaurant)
    );

    assertEquals(
        "A restaurant with this ID already exists.",
        exception.getMessage()
    );

    assertEquals(1, repository.findAll().size());

    assertSame(
        firstRestaurant,
        repository.findById("R001").orElse(null)
    );
}

@Test
void shouldRejectNullRestaurant() {

    RestaurantRepository repository = new RestaurantRepository();

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> repository.save(null)
    );

    assertEquals(
        "Restaurant cannot be null.",
        exception.getMessage()
    );

    assertTrue(repository.findAll().isEmpty());
}

@Test
void shouldReturnEmptyWhenRestaurantNotFound() {

    RestaurantRepository repository = new RestaurantRepository();

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    repository.save(restaurant);

    assertTrue(repository.findById("R999").isEmpty());

    assertEquals(1, repository.findAll().size());
}

@Test
void shouldReturnEmptyWhenIdIsNull() {

    RestaurantRepository repository = new RestaurantRepository();

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    repository.save(restaurant);

    assertTrue(repository.findById(null).isEmpty());

    assertEquals(1, repository.findAll().size());
}

@Test
void shouldPreventModifyingRestaurantsList() {

    RestaurantRepository repository = new RestaurantRepository();

    Restaurant restaurant = new Restaurant(
        "R001",
        "Pizza House",
        true,
        true,
        20.0,
        5.0
    );

    repository.save(restaurant);

    assertThrows(
        UnsupportedOperationException.class,
        () -> repository.findAll().clear()
    );

    assertEquals(1, repository.findAll().size());

    assertSame(
        restaurant,
        repository.findById("R001").orElse(null)
    );
}

}
