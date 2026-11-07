
package com.foodorbit.application.catalog;

import java.util.ArrayList;
import com.foodorbit.domain.model.Restaurant;
import java.util.Collections;
import java.util.List;

import com.foodorbit.domain.model.Restaurant;
import com.foodorbit.persistence.RestaurantRepository;

/**
 * Provides services for browsing restaurants in SufraGo.
 */
public class RestaurantService {

    /**
     * Repository used to store and retrieve restaurants.
     */
    private final RestaurantRepository restaurantRepository;

    /**
     * Creates the service with a new restaurant repository.
     */
    public RestaurantService() {
        this.restaurantRepository = new RestaurantRepository();
    }

    /**
     * Creates the service using an existing repository.
     *
     * @param restaurantRepository the repository to use
     * @throws IllegalArgumentException if the repository is null
     */
    public RestaurantService(RestaurantRepository restaurantRepository) {
        if (restaurantRepository == null) {
            throw new IllegalArgumentException(
                    "Restaurant repository cannot be null.");
        }

        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Adds a restaurant to the system.
     *
     * @param restaurant the restaurant to add
     * @throws IllegalArgumentException if the restaurant is null
     */
    public void addRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException(
                    "Restaurant cannot be null.");
        }

        restaurantRepository.save(restaurant);
    }

    /** Updates a restaurant that is already registered. */
    public void updateRestaurant(Restaurant restaurant) {
        restaurantRepository.update(restaurant);
    }

    /** Removes a restaurant from the catalog. */
    public boolean deleteRestaurant(String id) {
        return restaurantRepository.deleteById(id);
    }

    /**
     * Returns all restaurants registered in the system.
     *
     * @return an unmodifiable list of all restaurants
     */
    public List<Restaurant> getAllRestaurants() {
        return Collections.unmodifiableList(
                new ArrayList<>(restaurantRepository.findAll()));
    }

    /**
     * Returns only restaurants that are currently open.
     *
     * @return an unmodifiable list of open restaurants
     */
    public List<Restaurant> getOpenRestaurants() {
        List<Restaurant> openRestaurants = new ArrayList<>();

        for (Restaurant restaurant : restaurantRepository.findAll()) {
            if (restaurant.isOpen()) {
                openRestaurants.add(restaurant);
            }
        }

        return Collections.unmodifiableList(openRestaurants);
    }
}
