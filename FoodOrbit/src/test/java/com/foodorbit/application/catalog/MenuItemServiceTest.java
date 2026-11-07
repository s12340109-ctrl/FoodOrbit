
package com.foodorbit.application.catalog;
import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.persistence.MenuItemRepository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MenuItemServiceTest {

    @Test
    void shouldRejectNullRepository() {

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new MenuItemService(null)
        );

        assertEquals(
            "Menu item repository cannot be null.",
            exception.getMessage()
        );
    }

@Test
void shouldRejectNullMenuItem() {

    MenuItemService service = new MenuItemService();

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.addMenuItem(null)
    );

    assertEquals(
        "Menu item cannot be null.",
        exception.getMessage()
    );
}

@Test
void shouldAddMenuItemSuccessfully() {

    MenuItemService service = new MenuItemService();

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    service.addMenuItem(menuItem);

    assertEquals(1, service.getAllMenuItems().size());

    assertSame(
        menuItem,
        service.getAllMenuItems().get(0)
    );
}

@Test
void shouldReturnOnlyAvailableMenuItems() {

    MenuItemService service = new MenuItemService();

    MenuItem availableItem = new MenuItem(
        "M001", "Chicken Burger", 25.0, true
    );

    MenuItem unavailableItem = new MenuItem(
        "M002", "Pizza", 30.0, false
    );

    service.addMenuItem(availableItem);
    service.addMenuItem(unavailableItem);

    assertEquals(2, service.getAllMenuItems().size());

    assertEquals(1, service.getAvailableMenuItems().size());

    assertSame(
        availableItem,
        service.getAvailableMenuItems().get(0)
    );
}

@Test
void shouldPreventModifyingMenuItemsList() {

    MenuItemService service = new MenuItemService();

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    service.addMenuItem(menuItem);

    assertThrows(
        UnsupportedOperationException.class,
        () -> service.getAllMenuItems().clear()
    );

    assertEquals(1, service.getAllMenuItems().size());

    assertSame(
        menuItem,
        service.getAllMenuItems().get(0)
    );
}

@Test
void shouldPreventModifyingAvailableMenuItemsList() {

    MenuItemService service = new MenuItemService();

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    service.addMenuItem(menuItem);

    assertThrows(
        UnsupportedOperationException.class,
        () -> service.getAvailableMenuItems().clear()
    );

    assertEquals(1, service.getAvailableMenuItems().size());

    assertSame(
        menuItem,
        service.getAvailableMenuItems().get(0)
    );
}
@Test
void shouldCreateServiceWithValidRepository() {

    MenuItemRepository repository = new MenuItemRepository();

    MenuItemService service = new MenuItemService(repository);

    MenuItem menuItem = new MenuItem(
        "M001",
        "Chicken Burger",
        25.0,
        true
    );

    service.addMenuItem(menuItem);

    assertEquals(1, repository.findAll().size());

    assertSame(
        menuItem,
        service.getAllMenuItems().get(0)
    );
}
}
