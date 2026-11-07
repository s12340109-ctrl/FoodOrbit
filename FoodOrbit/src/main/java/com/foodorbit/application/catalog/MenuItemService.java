
package com.foodorbit.application.catalog;

import java.util.ArrayList; 
import java.util.Collections;
import java.util.List;

import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.persistence.MenuItemRepository;

/**
 * Provides services for managing and browsing menu items.
 */
public class MenuItemService {

    /**
     * Repository used to store and retrieve menu items.
     */
    private final MenuItemRepository menuItemRepository;

    /**
     * Creates the service with a new menu item repository.
     */
    public MenuItemService() {
        this.menuItemRepository = new MenuItemRepository();
    }

    /**
     * Creates the service using an existing repository.
     *
     * @param menuItemRepository the repository to use
     * @throws IllegalArgumentException if the repository is null
     */
    public MenuItemService(MenuItemRepository menuItemRepository) {
        if (menuItemRepository == null) {
            throw new IllegalArgumentException(
                    "Menu item repository cannot be null.");
        }

        this.menuItemRepository = menuItemRepository;
    }

    /**
     * Adds a menu item to the menu.
     *
     * @param menuItem the menu item to add
     * @throws IllegalArgumentException if the menu item is null
     */
    public void addMenuItem(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException(
                    "Menu item cannot be null.");
        }

        menuItemRepository.save(menuItem);
    }

    /** Updates a menu item that is already registered. */
    public void updateMenuItem(MenuItem menuItem) {
        menuItemRepository.update(menuItem);
    }

    /** Removes a menu item from the catalog. */
    public boolean deleteMenuItem(String id) {
        return menuItemRepository.deleteById(id);
    }

    /**
     * Returns all menu items.
     *
     * @return an unmodifiable list of all menu items
     */
    public List<MenuItem> getAllMenuItems() {
        return Collections.unmodifiableList(
                new ArrayList<>(menuItemRepository.findAll()));
    }

    /**
     * Returns only menu items that are available for ordering.
     *
     * @return an unmodifiable list of available menu items
     */
    public List<MenuItem> getAvailableMenuItems() {
        List<MenuItem> availableItems = new ArrayList<>();

        for (MenuItem menuItem : menuItemRepository.findAll()) {
            if (menuItem.isAvailable()) {
                availableItems.add(menuItem);
            }
        }

        return Collections.unmodifiableList(availableItems);
    }
}
