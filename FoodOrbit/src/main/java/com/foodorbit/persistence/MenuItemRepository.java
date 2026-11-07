
package com.foodorbit.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.foodorbit.domain.model.MenuItem;

/**
 * Stores menu items and provides access to them.
 */
public class MenuItemRepository {

    /**
     * The list that stores the menu items in the repository.
     */
    private final List<MenuItem> menuItems;

    /**
     * Creates an empty menu item repository.
     */
    public MenuItemRepository() {
        this.menuItems = new ArrayList<>();
    }

    /**
     * Saves a menu item in the repository.
     *
     * @param menuItem the menu item to save
     * @throws IllegalArgumentException if the item is null
     *         or its ID already exists
     */
    public void save(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException(
                    "Menu item cannot be null.");
        }

        if (findById(menuItem.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "A menu item with this ID already exists.");
        }

        menuItems.add(menuItem);
    }

    /**
     * Finds a menu item using its unique identifier.
     *
     * @param id the menu item identifier
     * @return an Optional containing the item if found,
     *         or an empty Optional otherwise
     */
    public Optional<MenuItem> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        for (MenuItem menuItem : menuItems) {
            if (menuItem.getId().equals(id)) {
                return Optional.of(menuItem);
            }
        }

        return Optional.empty();
    }

    /**
     * Replaces an existing menu item while preserving its identifier.
     *
     * @param menuItem the updated menu item
     */
    public void update(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be null.");
        }
        for (int i = 0; i < menuItems.size(); i++) {
            if (menuItems.get(i).getId().equals(menuItem.getId())) {
                menuItems.set(i, menuItem);
                return;
            }
        }
        throw new IllegalArgumentException("Menu item not found.");
    }

    /**
     * Removes a menu item by its identifier.
     *
     * @param id the menu item identifier
     * @return true when an item was removed
     */
    public boolean deleteById(String id) {
        return menuItems.removeIf(item -> item.getId().equals(id));
    }

    /**
     * Returns all saved menu items.
     *
     * @return an unmodifiable list of menu items
     */
    public List<MenuItem> findAll() {
        return Collections.unmodifiableList(menuItems);
    }
}
