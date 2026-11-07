package com.foodorbit.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.model.Restaurant;

class RepositoryEdgeCasesTest {

    @Test
    void restaurantRepositoryCoversNullDuplicateMissingAndDeleteCases() {
        RestaurantRepository repository = new RestaurantRepository();
        Restaurant first = new Restaurant("R1", "First", true, true, 10, 2);

        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        repository.save(first);
        assertThrows(IllegalArgumentException.class, () -> repository.save(first));

        assertTrue(repository.findById("R1").isPresent());
        assertTrue(repository.findById(null).isEmpty());
        assertTrue(repository.findById("missing").isEmpty());

        Restaurant replacement = new Restaurant("R1", "Updated", false, true, 15, 3);
        repository.update(replacement);
        assertSame(replacement, repository.findById("R1").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> repository.update(null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.update(new Restaurant("R2", "Unknown", true, true, 0)));

        assertFalse(repository.deleteById("missing"));
        assertTrue(repository.deleteById("R1"));
        assertFalse(repository.deleteById("R1"));
        assertTrue(repository.findAll().isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> repository.findAll().add(first));
    }

    @Test
    void menuItemRepositoryCoversNullDuplicateMissingAndDeleteCases() {
        MenuItemRepository repository = new MenuItemRepository();
        MenuItem first = new MenuItem("M1", "Burger", 12, true);

        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        repository.save(first);
        assertThrows(IllegalArgumentException.class, () -> repository.save(first));

        assertSame(first, repository.findById("M1").orElseThrow());
        assertTrue(repository.findById(null).isEmpty());
        assertTrue(repository.findById("missing").isEmpty());

        MenuItem replacement = new MenuItem("M1", "Updated Burger", 14, false);
        repository.update(replacement);
        assertSame(replacement, repository.findById("M1").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> repository.update(null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.update(new MenuItem("M2", "Unknown", 1, true)));

        assertFalse(repository.deleteById("missing"));
        assertTrue(repository.deleteById("M1"));
        assertFalse(repository.deleteById("M1"));
        assertTrue(repository.findAll().isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> repository.findAll().add(first));
    }
}
