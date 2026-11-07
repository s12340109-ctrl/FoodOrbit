
package com.foodorbit.presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.foodorbit.application.pricing.ExpressPricingStrategy;
import com.foodorbit.application.pricing.NormalPricingStrategy;
import com.foodorbit.application.pricing.PeakHourPricingStrategy;
import com.foodorbit.application.pricing.PromotionalPricingStrategy;
import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.model.Restaurant;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

class RestaurantsPageDeepCoverageTest {

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);

        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException ex) {
            started.countDown();
        }

        Platform.setImplicitExit(false);

        assertTrue(started.await(15, TimeUnit.SECONDS));
    }

    @Test
    void pricingStrategyCoversAllOptions() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            setField(page, "selectedPricing", "Normal");
            assertInstanceOf(
                    NormalPricingStrategy.class,
                    invoke(page, "pricingStrategy")
            );

            setField(page, "selectedPricing", "Peak Hour");
            assertInstanceOf(
                    PeakHourPricingStrategy.class,
                    invoke(page, "pricingStrategy")
            );

            setField(page, "selectedPricing", "Promotion");
            assertInstanceOf(
                    PromotionalPricingStrategy.class,
                    invoke(page, "pricingStrategy")
            );

            setField(page, "selectedPricing", "Express Delivery");
            assertInstanceOf(
                    ExpressPricingStrategy.class,
                    invoke(page, "pricingStrategy")
            );

            setField(page, "selectedPricing", "Unknown");
            assertInstanceOf(
                    NormalPricingStrategy.class,
                    invoke(page, "pricingStrategy")
            );
        });
    }

    @Test
    void expressOrderFeeCoversAllCombinations() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            setField(page, "selectedOrderType", "Express");
            setField(page, "selectedPricing", "Normal");

            assertEquals(8.0, (double) invoke(page, "expressOrderFee"));

            setField(page, "selectedPricing", "Peak Hour");
            assertEquals(8.0, (double) invoke(page, "expressOrderFee"));

            setField(page, "selectedPricing", "Promotion");
            assertEquals(8.0, (double) invoke(page, "expressOrderFee"));

            setField(page, "selectedPricing", "Express Delivery");
            assertEquals(0.0, (double) invoke(page, "expressOrderFee"));

            setField(page, "selectedOrderType", "Standard");
            setField(page, "selectedPricing", "Normal");
            assertEquals(0.0, (double) invoke(page, "expressOrderFee"));

            setField(page, "selectedOrderType", "Scheduled");
            assertEquals(0.0, (double) invoke(page, "expressOrderFee"));
        });
    }

    @Test
    void categoryMatchesCoversAllBranches() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            Object restaurant = restaurants(page).get(0);

            setField(page, "category", "All");
            assertTrue((boolean) invoke(page, "categoryMatches", restaurant));

            setField(page, "category", "Pizza");
            assertTrue((boolean) invoke(page, "categoryMatches", restaurant));

            setField(page, "category", "Burgers");
            assertFalse((boolean) invoke(page, "categoryMatches", restaurant));
        });
    }

    @Test
    void restaurantMatchesCoversSearchConditions() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            Object restaurant = restaurants(page).get(0);

            setField(page, "searchText", "");
            assertTrue((boolean) invoke(page, "restaurantMatches", restaurant));

            setField(page, "searchText", "pizza house");
            assertTrue((boolean) invoke(page, "restaurantMatches", restaurant));

            setField(page, "searchText", "italian");
            assertTrue((boolean) invoke(page, "restaurantMatches", restaurant));

            setField(page, "searchText", "pizza");
            assertTrue((boolean) invoke(page, "restaurantMatches", restaurant));

            setField(page, "searchText", "pepperoni");
            assertTrue((boolean) invoke(page, "restaurantMatches", restaurant));

            setField(page, "searchText", "not-existing-meal");
            assertFalse((boolean) invoke(page, "restaurantMatches", restaurant));
        });
    }

    @Test
    void mealMatchesCoversSearchConditions() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            Object restaurant = restaurants(page).get(0);
            Object meal = meals(restaurant).get(0);

            setField(page, "searchText", "");
            assertTrue((boolean) invoke(
                    page, "mealMatches", restaurant, meal));

            setField(page, "searchText", "margherita");
            assertTrue((boolean) invoke(
                    page, "mealMatches", restaurant, meal));

            setField(page, "searchText", "mozzarella");
            assertTrue((boolean) invoke(
                    page, "mealMatches", restaurant, meal));

            setField(page, "searchText", "pizza house");
            assertTrue((boolean) invoke(
                    page, "mealMatches", restaurant, meal));

            setField(page, "searchText", "something-not-found");
            assertFalse((boolean) invoke(
                    page, "mealMatches", restaurant, meal));
        });
    }

    @Test
    void displayMethodsCoverDefaultCases() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            Restaurant unknownRestaurant = new Restaurant(
                    "RX999",
                    "Coverage Restaurant",
                    true,
                    true,
                    0.0,
                    0.0
            );

            Object displayedRestaurant =
                    invoke(page, "restaurantDisplay", unknownRestaurant);

            assertEquals("Other", field(displayedRestaurant, "category"));
            assertEquals("Restaurant", field(displayedRestaurant, "description"));
            assertEquals("", field(displayedRestaurant, "imageFile"));

            MenuItem unknownMeal = new MenuItem(
                    "MX999",
                    "Coverage Meal",
                    15.0,
                    true
            );

            Object displayedMeal =
                    invoke(page, "mealDisplay", unknownMeal);

            assertEquals(
                    "Freshly prepared menu item",
                    field(displayedMeal, "description")
            );

            assertFalse((boolean) field(displayedMeal, "recommended"));
            assertEquals("", field(displayedMeal, "imageFile"));
        });
    }

    @Test
    void foodImageUsesFallbackWhenResourceDoesNotExist() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            Node image = (Node) invoke(
                    page,
                    "foodImage",
                    "missing-coverage-image-999.jpg",
                    120.0,
                    80.0
            );

            assertInstanceOf(Label.class, image);
            assertEquals("Food Image", ((Label) image).getText());

            Node fallback = (Node) invoke(
                    page,
                    "missingImage",
                    100.0,
                    70.0
            );

            assertInstanceOf(Label.class, fallback);
        });
    }

    @Test
    void renderResultsHandlesMatchingAndNoMatchingResults()
            throws Exception {

        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            setField(page, "category", "All");
            setField(page, "searchText", "no-result-coverage-999");

            invoke(page, "renderResults");

            VBox content = field(page, "content");

            assertTrue(find(content, Label.class).stream()
                    .anyMatch(label ->
                            "No restaurants found.".equals(label.getText())));

            assertTrue(find(content, Label.class).stream()
                    .anyMatch(label ->
                            "No recommended meals found.".equals(label.getText())));

            setField(page, "searchText", "");
            invoke(page, "renderResults");

            assertFalse(find(content, Button.class).isEmpty());
        });
    }

    @Test
    void showMenuHandlesEmptyAndAvailableMenus() throws Exception {
        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            Restaurant model = new Restaurant(
                    "RX998",
                    "Empty Menu Restaurant",
                    true,
                    true,
                    0.0,
                    2.0
            );

            Object emptyRestaurant = createPresentationRestaurant(
                    model,
                    "Coverage",
                    "Other"
            );

            invoke(page, "showMenu", emptyRestaurant);

            VBox content = field(page, "content");

            assertTrue(find(content, Label.class).stream()
                    .anyMatch(label ->
                            "No menu items are currently available."
                                    .equals(label.getText())));

            Restaurant secondModel = new Restaurant(
                    "RX997",
                    "Available Menu Restaurant",
                    true,
                    true,
                    0.0,
                    2.0
            );

            Object restaurant = createPresentationRestaurant(
                    secondModel,
                    "Coverage",
                    "Other"
            );

            MenuItem itemModel = new MenuItem(
                    "MX997",
                    "Coverage Available Meal",
                    20.0,
                    true
            );

            Object meal = createPresentationMeal(
                    itemModel,
                    "Coverage description",
                    true
            );

            meals(restaurant).add(meal);

            invoke(page, "showMenu", restaurant);

            assertTrue(find(content, Button.class).stream()
                    .anyMatch(button ->
                            "Add to Cart".equals(button.getText())));
        });
    }

    @Test
    void refreshPageHandlesBothSelectedRestaurantStates()
            throws Exception {

        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            setField(page, "selectedRestaurant", null);

            invoke(page, "refreshPage");

            VBox content = field(page, "content");

            assertTrue(find(content, Label.class).stream()
                    .anyMatch(label ->
                            "What are you craving?".equals(label.getText())));

            Object restaurant = restaurants(page).get(0);

            setField(page, "selectedRestaurant", restaurant);

            invoke(page, "refreshPage");

            assertTrue(find(content, Button.class).stream()
                    .anyMatch(button ->
                            button.getText() != null
                            && button.getText().contains("Back to Restaurants")));
        });
    }

    @Test
    void restaurantAndMealCardsHandleDisabledStates()
            throws Exception {

        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            Restaurant closedModel = new Restaurant(
                    "RX996",
                    "Closed Coverage Restaurant",
                    false,
                    false,
                    0.0,
                    2.0
            );

            Object restaurant = createPresentationRestaurant(
                    closedModel,
                    "Coverage",
                    "Other"
            );

            VBox restaurantCard = (VBox) invoke(
                    page,
                    "restaurantCard",
                    restaurant
            );

            assertFalse(find(restaurantCard, Label.class).isEmpty());

            MenuItem availableModel = new MenuItem(
                    "MX996",
                    "Coverage Meal",
                    10.0,
                    true
            );

            Object meal = createPresentationMeal(
                    availableModel,
                    "Coverage description",
                    true
            );

            VBox mealCard = (VBox) invoke(
                    page,
                    "mealCard",
                    restaurant,
                    meal
            );

            Button addButton = find(mealCard, Button.class).stream()
                    .filter(button ->
                            "Add to Cart".equals(button.getText()))
                    .findFirst()
                    .orElseThrow();

            assertTrue(addButton.isDisabled());

            Restaurant openModel = new Restaurant(
                    "RX995",
                    "Open Coverage Restaurant",
                    true,
                    true,
                    0.0,
                    2.0
            );

            Object openRestaurant = createPresentationRestaurant(
                    openModel,
                    "Coverage",
                    "Other"
            );

            VBox enabledCard = (VBox) invoke(
                    page,
                    "mealCard",
                    openRestaurant,
                    meal
            );

            Button enabledAdd = find(enabledCard, Button.class).stream()
                    .filter(button ->
                            "Add to Cart".equals(button.getText()))
                    .findFirst()
                    .orElseThrow();

            assertFalse(enabledAdd.isDisabled());
        });
    }

    @Test
    void addToCartAddsMealAndIncreasesExistingQuantity()
            throws Exception {

        onFx(() -> {
            RestaurantsPage page = new RestaurantsPage();

            Object restaurant = restaurants(page).get(0);
            Object meal = meals(restaurant).stream()
                    .filter(m -> {
                        try {
                            return (boolean) field(field(m, "model"), "available");
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .findFirst()
                    .orElseThrow();

            List<?> cart = field(page, "cart");

            assertTrue(cart.isEmpty());

            invoke(page, "addToCart", restaurant, meal);

            assertEquals(1, cart.size());

            Object cartItem = cart.get(0);

            assertEquals(1, ((Number) field(cartItem, "quantity")).intValue());
            invoke(page, "addToCart", restaurant, meal);

            assertEquals(1, cart.size());
            assertEquals(2, ((Number) field(cartItem, "quantity")).intValue());
        });
    }

    // -------------------------------------------------
    // Reflection helpers
    // -------------------------------------------------

    private static List<?> restaurants(RestaurantsPage page)
            throws Exception {
        return field(page, "restaurants");
    }

    @SuppressWarnings("unchecked")
    private static List<Object> meals(Object restaurant) throws Exception {
        return (List<Object>) field(restaurant, "menu");
    }

    private static Object createPresentationRestaurant(
            Restaurant model,
            String description,
            String category
    ) throws Exception {

        Class<?> type = Class.forName(
                "com.foodorbit.presentation.RestaurantsPage$Restaurant"
        );

        Constructor<?> constructor = type.getDeclaredConstructor(
                Restaurant.class,
                String.class,
                String.class,
                double.class,
                String.class,
                String.class
        );

        constructor.setAccessible(true);

        return constructor.newInstance(
                model,
                description,
                category,
                4.5,
                "20-30 min",
                ""
        );
    }

    private static Object createPresentationMeal(
            MenuItem model,
            String description,
            boolean recommended
    ) throws Exception {

        Class<?> type = Class.forName(
                "com.foodorbit.presentation.RestaurantsPage$Meal"
        );

        Constructor<?> constructor = type.getDeclaredConstructor(
                MenuItem.class,
                String.class,
                boolean.class,
                String.class
        );

        constructor.setAccessible(true);

        return constructor.newInstance(
                model,
                description,
                recommended,
                ""
        );
    }

    private static Object invoke(
            Object target,
            String name,
            Object... args
    ) throws Exception {

        for (Method method : target.getClass().getDeclaredMethods()) {

            if (!method.getName().equals(name)
                    || method.getParameterCount() != args.length) {
                continue;
            }

            method.setAccessible(true);

            try {
                return method.invoke(target, args);
            } catch (IllegalArgumentException ignored) {
            }
        }

        throw new NoSuchMethodException(name);
    }

    private static void setField(
            Object target,
            String name,
            Object value
    ) throws Exception {

        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static <T> T field(
            Object target,
            String name
    ) throws Exception {

        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        T value = (T) field.get(target);

        return value;
    }

    private static <T extends Node> List<T> find(
            Parent root,
            Class<T> type
    ) {

        java.util.ArrayList<T> result = new java.util.ArrayList<>();

        collect(root, type, result);

        return result;
    }

    private static <T extends Node> void collect(
            Node node,
            Class<T> type,
            List<T> result
    ) {

        if (type.isInstance(node)) {
            result.add(type.cast(node));
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, type, result);
            }
        }
    }

    private static void onFx(ThrowingRunnable action)
            throws Exception {

        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch done = new CountDownLatch(1);

        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable ex) {
                failure.set(ex);
            } finally {
                done.countDown();
            }
        });

        assertTrue(
                done.await(30, TimeUnit.SECONDS),
                "Timed out waiting for JavaFX task"
        );

        if (failure.get() != null) {
            if (failure.get() instanceof Exception ex) {
                throw ex;
            }

            if (failure.get() instanceof Error error) {
                throw error;
            }

            throw new RuntimeException(failure.get());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
