
package com.foodorbit.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.foodorbit.domain.model.MenuItem;

class MenuItemRepositoryTest {

    @Test
    void shouldSaveMenuItemSuccessfully() {

        MenuItemRepository repository = new MenuItemRepository();

        MenuItem menuItem = new MenuItem(
            "M001",
            "Chicken Burger",
            25.0,
            true
        );

        repository.save(menuItem);

        assertEquals(1, repository.findAll().size());

        assertSame(
            menuItem,
            repository.findById("M001").orElse(null)
        );
    }

@Test
void shouldRejectDuplicateMenuItemId() {

    MenuItemRepository repository = new MenuItemRepository();

    MenuItem firstItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    MenuItem secondItem = new MenuItem(
        "M001",
        "Pizza",
        30.0,
        true
    );

    repository.save(firstItem);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> repository.save(secondItem)
    );

    assertEquals(
        "A menu item with this ID already exists.",
        exception.getMessage()
    );

    assertEquals(1, repository.findAll().size());
    assertSame(firstItem, repository.findById("M001").orElse(null));
}

@Test
void shouldRejectNullMenuItem() {

    MenuItemRepository repository = new MenuItemRepository();

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> repository.save(null)
    );

    assertEquals(
        "Menu item cannot be null.",
        exception.getMessage()
    );

    assertTrue(repository.findAll().isEmpty());
}

@Test
void shouldReturnEmptyWhenMenuItemNotFound() {

    MenuItemRepository repository = new MenuItemRepository();

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    repository.save(menuItem);

    assertTrue(repository.findById("M999").isEmpty());

    assertEquals(1, repository.findAll().size());
}

@Test
void shouldReturnEmptyWhenIdIsNull() {

    MenuItemRepository repository = new MenuItemRepository();

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    repository.save(menuItem);

    assertTrue(repository.findById(null).isEmpty());

    assertEquals(1, repository.findAll().size());
}

@Test
void shouldPreventModifyingMenuItemsList() {

    MenuItemRepository repository = new MenuItemRepository();

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    repository.save(menuItem);

    assertThrows(
        UnsupportedOperationException.class,
        () -> repository.findAll().clear()
    );

    assertEquals(1, repository.findAll().size());

    assertSame(
        menuItem,
        repository.findById("M001").orElse(null)
    );
}

}
