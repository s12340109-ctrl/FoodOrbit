
package com.foodorbit.persistence;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.foodorbit.domain.model.Restaurant;

/**
 * Stores and retrieves restaurants in SufraGo.
 */
public class RestaurantRepository {

    /**
     * The list that stores the restaurants in the repository.
     */
    private final List<Restaurant> restaurants = new ArrayList<>();

    /**
     * Saves a restaurant.
     *
     * @param restaurant the restaurant to save
     * @throws IllegalArgumentException if the restaurant is null
     *         or its ID already exists
     */
    public void save(Restaurant restaurant) {

        if (restaurant == null) {
            throw new IllegalArgumentException(
                    "Restaurant cannot be null.");
        }

        if (findById(restaurant.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "A restaurant with this ID already exists.");
        }

        restaurants.add(restaurant);
    }

    /**
     * Finds a restaurant by its ID.
     *
     * @param id the restaurant ID
     * @return the restaurant if found, otherwise empty
     */
    public Optional<Restaurant> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        for (Restaurant restaurant : restaurants) {
            if (restaurant.getId().equals(id)) {
                return Optional.of(restaurant);
            }
        }

        return Optional.empty();
    }

    /**
     * Replaces an existing restaurant while preserving its identifier.
     *
     * @param restaurant the updated restaurant
     */
    public void update(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null.");
        }
        for (int i = 0; i < restaurants.size(); i++) {
            if (restaurants.get(i).getId().equals(restaurant.getId())) {
                restaurants.set(i, restaurant);
                return;
            }
        }
        throw new IllegalArgumentException("Restaurant not found.");
    }

    /**
     * Removes a restaurant by its identifier.
     *
     * @param id the restaurant identifier
     * @return true when a restaurant was removed
     */
    public boolean deleteById(String id) {
        return restaurants.removeIf(item -> item.getId().equals(id));
    }

    /**
     * Returns all saved restaurants.
     *
     * @return an unmodifiable list of restaurants
     */
    public List<Restaurant> findAll() {
        return Collections.unmodifiableList(restaurants);
    }

}
