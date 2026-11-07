package com.foodorbit.presentation;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.foodorbit.application.pricing.DeliveryFeeCalculator;
import com.foodorbit.application.pricing.ExpressPricingStrategy;
import com.foodorbit.application.pricing.PricingStrategy;
import com.foodorbit.application.pricing.NormalPricingStrategy;
import com.foodorbit.application.pricing.PeakHourPricingStrategy;
import com.foodorbit.application.pricing.PromotionalPricingStrategy;
import com.foodorbit.domain.model.MenuItem;
import com.foodorbit.domain.order.OrderType;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class RestaurantsPage extends VBox {

    private static final String GREEN = "#103B36";
    private static final String MUTED = "#647B77";

    private static final double DELIVERY_RATE_PER_KM = 2.0;
    private static final double EXPRESS_FEE = 8.0;
    private static final double PEAK_MULTIPLIER = 1.25;
    private static final double PROMOTIONAL_DISCOUNT = 10.0;

    private static final java.util.concurrent.atomic.AtomicInteger
            NEXT_RESTAURANT_ID =
            new java.util.concurrent.atomic.AtomicInteger(100);

    private final List<Restaurant> restaurants = new ArrayList<>();
    private final List<CartItem> cart = new ArrayList<>();
    private final VBox content = new VBox(20);

    private final DeliveryFeeCalculator deliveryFeeCalculator =
            new DeliveryFeeCalculator();

    private final String customerUsername;

    private Restaurant selectedRestaurant;
    private String category = "All";
    private String searchText = "";

    private String selectedPricing = "Normal";
    private String selectedOrderType = "Standard";

    private double deliveryDistanceKm = 2.5;

    public RestaurantsPage() {
        this(null);
    }

    /** Creates the restaurant catalog for a customer or manager session. */
    public RestaurantsPage(String customerUsername) {
        this.customerUsername = customerUsername;

        setStyle("-fx-background-color: #F4F9F7;");
        getStyleClass().add("main-content");

        initializeData();

        content.setPadding(new Insets(26));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background: #F4F9F7; " +
                "-fx-background-color: transparent;"
        );

        getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        showRestaurants();
    }

    private void initializeData() {

        restaurants.clear();

        for (com.foodorbit.domain.model.Restaurant model
                : AppStore.get().restaurants()) {

            Restaurant restaurant = restaurantDisplay(model);

            for (MenuItem item
                    : AppStore.get().menuItemsForRestaurant(model.getId())) {

                restaurant.menu.add(mealDisplay(item));
            }

            restaurants.add(restaurant);
        }
    }

    private Restaurant restaurantDisplay(
            com.foodorbit.domain.model.Restaurant model) {

        return switch (model.getId()) {
            case "R001" -> new Restaurant(
                    model,
                    "Italian & Pizza",
                    "Pizza",
                    4.8,
                    "25-35 min",
                    "pizza-house.jpg");

            case "R002" -> new Restaurant(
                    model,
                    "Burgers & Fast Food",
                    "Burgers",
                    4.7,
                    "20-30 min",
                    "burger-station.jpg");

            case "R003" -> new Restaurant(
                    model,
                    "Arabic & Street Food",
                    "Arabic",
                    4.9,
                    "15-25 min",
                    "shawarma-corner.jpg");

            case "R004" -> new Restaurant(
                    model,
                    "Healthy & Salads",
                    "Healthy",
                    4.6,
                    "20-30 min",
                    "fresh-bowl.jpg");

            default -> new Restaurant(
                    model,
                    "Restaurant",
                    "Other",
                    0.0,
                    "N/A",
                    "");
        };
    }

    private Meal mealDisplay(MenuItem model) {

        String description;
        boolean recommended;
        String imageFile;

        switch (model.getId()) {
            case "M001" -> {
                description = "Mozzarella, tomato sauce and basil";
                recommended = true;
                imageFile = "margherita.jpg";
            }
            case "M002" -> {
                description = "Cheese and pepperoni slices";
                recommended = true;
                imageFile = "pepperoni.jpg";
            }
            case "M003" -> {
                description = "Chicken, BBQ sauce and cheese";
                recommended = false;
                imageFile = "bbq-chicken.jpg";
            }
            case "M004" -> {
                description = "Beef, lettuce, tomato and sauce";
                recommended = true;
                imageFile = "classic-burger.jpg";
            }
            case "M005" -> {
                description = "Beef with melted cheese";
                recommended = true;
                imageFile = "cheese-burger.jpg";
            }
            case "M006" -> {
                description = "Crispy chicken and creamy sauce";
                recommended = false;
                imageFile = "crispy-chicken.jpg";
            }
            case "M007" -> {
                description = "Chicken wrap with garlic sauce";
                recommended = true;
                imageFile = "chicken-shawarma.jpg";
            }
            case "M008" -> {
                description = "Beef wrap with tahini sauce";
                recommended = false;
                imageFile = "beef-shawarma.jpg";
            }
            case "M009" -> {
                description = "Shawarma served with fries";
                recommended = true;
                imageFile = "shawarma-meal.jpg";
            }
            case "M010" -> {
                description = "Lettuce, croutons and Caesar dressing";
                recommended = true;
                imageFile = "caesar-salad.jpg";
            }
            case "M011" -> {
                description = "Chicken, rice and vegetables";
                recommended = true;
                imageFile = "chicken-bowl.jpg";
            }
            case "M012" -> {
                description = "Freshly squeezed orange juice";
                recommended = false;
                imageFile = "orange-juice.jpg";
            }
            default -> {
                description = "Freshly prepared menu item";
                recommended = false;
                imageFile = "";
            }
        }

        return new Meal(model, description, recommended, imageFile);
    }

    private void showRestaurants() {

        selectedRestaurant = null;
        content.getChildren().clear();

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox heading = new VBox(
                5,
                title("What are you craving?"),
                subtitle(
                        "Discover delicious food and your favorite restaurants")
        );

        HBox.setHgrow(heading, Priority.ALWAYS);

        header.getChildren().addAll(
                heading,
                cartButton()
        );

        VBox banner = new VBox(10);
        banner.setPadding(new Insets(25));
        banner.setStyle(
                "-fx-background-color: " + GREEN + ";" +
                "-fx-background-radius: 18;"
        );

        Label bannerTitle = new Label(
                "Delicious food, delivered to you");

        bannerTitle.setStyle(
                "-fx-font-size: 23px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );

        Label bannerText = new Label(
                "Choose a restaurant, explore the menu " +
                "and order your favorite meals.");

        bannerText.setWrapText(true);
        bannerText.setStyle("-fx-text-fill: #D8E9E4;");

        banner.getChildren().addAll(
                bannerTitle,
                bannerText
        );

        TextField search = input(
                "Search restaurants or meals...");

        search.setText(searchText);

        search.textProperty().addListener(
                (obs, oldValue, newValue) -> {
                    searchText = newValue;
                    renderResults();
                }
        );

        HBox filters = new HBox(10);

        for (String option : new String[]{
                "All", "Pizza", "Burgers", "Arabic", "Healthy"
        }) {

            Button button = option.equals(category)
                    ? primary(option)
                    : secondary(option);

            button.setOnAction(e -> {
                category = option;
                showRestaurants();
            });

            filters.getChildren().add(button);
        }

        content.getChildren().addAll(
                header,
                banner,
                search,
                filters
        );

        renderResults();
    }

    private void renderResults() {

        while (content.getChildren().size() > 4) {
            content.getChildren().remove(4);
        }

        content.getChildren().add(
                section("Explore Restaurants"));

        FlowPane restaurantGrid = grid();

        for (Restaurant restaurant : restaurants) {

            if (restaurant.model.isOpen()
                    && categoryMatches(restaurant)
                    && restaurantMatches(restaurant)) {

                restaurantGrid.getChildren().add(
                        restaurantCard(restaurant));
            }
        }

        if (restaurantGrid.getChildren().isEmpty()) {
            restaurantGrid.getChildren().add(
                    subtitle("No restaurants found."));
        }

        content.getChildren().add(restaurantGrid);

        content.getChildren().add(
                section("Recommended for You"));

        FlowPane recommendedGrid = grid();

        for (Restaurant restaurant : restaurants) {

            if (!restaurant.model.isOpen()
                    || !categoryMatches(restaurant)) {
                continue;
            }

            for (Meal meal : restaurant.menu) {

                if (meal.model.isAvailable()
                        && meal.recommended
                        && mealMatches(restaurant, meal)) {

                    recommendedGrid.getChildren().add(
                            mealCard(restaurant, meal));
                }
            }
        }

        if (recommendedGrid.getChildren().isEmpty()) {
            recommendedGrid.getChildren().add(
                    subtitle("No recommended meals found."));
        }

        content.getChildren().add(recommendedGrid);
    }

    private boolean categoryMatches(Restaurant restaurant) {

        return category.equals("All")
                || category.equals(restaurant.category);
    }

    private boolean restaurantMatches(Restaurant restaurant) {

        String query = searchText.trim()
                .toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            return true;
        }

        if (restaurant.name.toLowerCase(Locale.ROOT)
                .contains(query)
                || restaurant.description.toLowerCase(Locale.ROOT)
                .contains(query)
                || restaurant.category.toLowerCase(Locale.ROOT)
                .contains(query)) {

            return true;
        }

        for (Meal meal : restaurant.menu) {
            if (mealMatches(restaurant, meal)) {
                return true;
            }
        }

        return false;
    }

    private boolean mealMatches(
            Restaurant restaurant,
            Meal meal) {

        String query = searchText.trim()
                .toLowerCase(Locale.ROOT);

        return query.isEmpty()
                || meal.name.toLowerCase(Locale.ROOT)
                        .contains(query)
                || meal.description.toLowerCase(Locale.ROOT)
                        .contains(query)
                || restaurant.name.toLowerCase(Locale.ROOT)
                        .contains(query);
    }

    private FlowPane grid() {

        FlowPane pane = new FlowPane();
        pane.setHgap(18);
        pane.setVgap(18);
        pane.setPrefWrapLength(900);

        return pane;
    }

    private VBox restaurantCard(Restaurant restaurant) {

        VBox card = card(245);

        Button explore = primary("Explore Menu");
        explore.setMaxWidth(Double.MAX_VALUE);
        explore.setOnAction(e -> showMenu(restaurant));

        String availabilityText = restaurant.model.isOpen()
                ? (restaurant.model.isWithinDeliveryZone()
                        ? "Open • Accepting orders"
                        : "Outside delivery zone")
                : "Currently closed";

        card.getChildren().addAll(
                foodImage(restaurant.imageFile, 245, 145),
                strong(restaurant.name),
                subtitle(restaurant.description),
                subtitle(
                        "★ " + restaurant.rating +
                        "    •    " + restaurant.deliveryTime),
                subtitle(availabilityText),
                subtitle("Minimum order: " + money(
                        restaurant.model.getMinimumOrderValue())),
                explore
        );

        return card;
    }

    private VBox mealCard(
            Restaurant restaurant,
            Meal meal) {

        VBox card = card(245);

        Label badge = new Label("RECOMMENDED");
        badge.setStyle(
                "-fx-text-fill: #157347;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;"
        );

        Button view = secondary("View Restaurant");
        view.setMaxWidth(Double.MAX_VALUE);
        view.setOnAction(e -> showMenu(restaurant));

        Button add = primary(meal.model.isAvailable()
                ? "Add to Cart" : "Currently Unavailable");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setDisable(!meal.model.isAvailable()
                || !restaurant.model.isOpen()
                || !restaurant.model.isWithinDeliveryZone());
        add.setOnAction(e -> addToCart(restaurant, meal));

        card.getChildren().addAll(
                foodImage(meal.imageFile, 245, 135),
                badge,
                strong(meal.name),
                subtitle("Available at: " + restaurant.name),
                subtitle(meal.description),
                strong(money(meal.price)),
                view,
                add
        );

        return card;
    }

    private void showMenu(Restaurant restaurant) {

        if (selectedRestaurant != restaurant) {
            deliveryDistanceKm = restaurant.model.getDeliveryDistance();
        }

        selectedRestaurant = restaurant;
        content.getChildren().clear();

        Button back = secondary("← Back to Restaurants");
        back.setOnAction(e -> showRestaurants());

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(
                5,
                title(restaurant.name),
                subtitle(
                        restaurant.description +
                        "  •  ★ " + restaurant.rating +
                        "  •  " + restaurant.deliveryTime +
                        "  •  " + (restaurant.model.isOpen()
                                ? "Open" : "Closed"))
        );

        HBox.setHgrow(info, Priority.ALWAYS);

        header.getChildren().addAll(
                info,
                cartButton()
        );

        FlowPane menuGrid = grid();

        for (Meal meal : restaurant.menu) {

            if (!meal.model.isAvailable()) {
                continue;
            }

            VBox card = card(245);

            Button add = primary(meal.model.isAvailable()
                    ? "Add to Cart" : "Currently Unavailable");
            add.setMaxWidth(Double.MAX_VALUE);
            add.setDisable(!meal.model.isAvailable()
                    || !restaurant.model.isOpen()
                    || !restaurant.model.isWithinDeliveryZone());
            add.setOnAction(e -> addToCart(restaurant, meal));

            card.getChildren().addAll(
                    foodImage(meal.imageFile, 245, 145),
                    strong(meal.name),
                    subtitle(meal.description),
                    strong(money(meal.price)),
                    subtitle(meal.model.isAvailable()
                            ? "Available" : "Currently unavailable"),
                    add
            );

            menuGrid.getChildren().add(card);
        }

        if (menuGrid.getChildren().isEmpty()) {
            menuGrid.getChildren().add(
                    subtitle("No menu items are currently available."));
        }

        content.getChildren().addAll(
                back,
                header,
                foodImage(restaurant.imageFile, 760, 210),
                section("Our Menu"),
                menuGrid
        );
    }

    private void addToCart(
            Restaurant restaurant,
            Meal meal) {

        if (!restaurant.model.isOpen()) {
            showAlert("Restaurant Closed",
                    "This restaurant is currently closed.");
            return;
        }

        if (!restaurant.model.isWithinDeliveryZone()) {
            showAlert("Outside Delivery Zone",
                    "This restaurant does not deliver to your area.");
            return;
        }

        if (!meal.model.isAvailable()) {
            showAlert("Item Unavailable",
                    "This menu item is currently unavailable.");
            return;
        }

        if (!cart.isEmpty()
                && cart.get(0).restaurant != restaurant) {

            Alert alert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Your cart contains meals from another " +
                    "restaurant. Clear the cart?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            alert.setHeaderText("Switch Restaurant");

            if (alert.showAndWait().orElse(ButtonType.NO)
                    != ButtonType.YES) {
                return;
            }

            cart.clear();
        }

        if (cart.isEmpty()) {
            deliveryDistanceKm = restaurant.model.getDeliveryDistance();
        }

        for (CartItem item : cart) {

            if (item.restaurant == restaurant
                    && item.meal == meal) {

                item.quantity++;
                refreshPage();
                return;
            }
        }

        cart.add(new CartItem(restaurant, meal));
        refreshPage();
    }

    private void refreshPage() {

        if (selectedRestaurant == null) {
            showRestaurants();
        } else {
            showMenu(selectedRestaurant);
        }
    }

    private void showCart() {

        content.getChildren().clear();

        Button back = secondary("← Continue Shopping");
        back.setOnAction(e -> refreshPage());

        content.getChildren().addAll(
                back,
                title("Your Cart")
        );

        if (cart.isEmpty()) {
            content.getChildren().add(
                    subtitle("Your cart is empty."));
            return;
        }

        VBox items = new VBox(12);

        for (CartItem item : new ArrayList<>(cart)) {

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12));
            row.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 12;"
            );

            VBox details = new VBox(
                    5,
                    strong(item.meal.name),
                    subtitle(item.restaurant.name),
                    subtitle(money(item.meal.price))
            );

            HBox.setHgrow(details, Priority.ALWAYS);

            Button minus = secondary("−");
            Button plus = secondary("+");

            Label quantity = strong(
                    String.valueOf(item.quantity));

            minus.setOnAction(e -> {
                item.quantity--;

                if (item.quantity <= 0) {
                    cart.remove(item);
                }

                showCart();
            });

            plus.setOnAction(e -> {
                item.quantity++;
                showCart();
            });

            row.getChildren().addAll(
                    foodImage(item.meal.imageFile, 90, 75),
                    details,
                    minus,
                    quantity,
                    plus,
                    strong(money(
                            item.meal.price * item.quantity))
            );

            items.getChildren().add(row);
        }

        Button checkout = primary("Proceed to Checkout");
        checkout.setOnAction(e -> showCheckout());

        content.getChildren().addAll(
                items,
                orderSummary(),
                checkout
        );
    }

    // ---------------------------------------
    // CHECKOUT
    // ---------------------------------------

    private void showCheckout() {

        if (cart.isEmpty()) {
            showCart();
            return;
        }

        content.getChildren().clear();

        Button back = secondary("← Back to Cart");
        back.setOnAction(e -> showCart());

        TextField customer = input("Customer Username / Name");

        if (customerUsername != null) {
            customer.setText(customerUsername);
            customer.setEditable(false);
        }

        TextField phone = input("Phone Number");
        TextField address = input("Delivery Address");

        TextField distance = input("Delivery Distance (km)");
        distance.setText(String.valueOf(deliveryDistanceKm));

        // Order Type
        ComboBox<String> orderType = new ComboBox<>();

        orderType.getItems().addAll(
                "Standard",
                "Express",
                "Scheduled"
        );

        orderType.setValue(selectedOrderType);
        orderType.setMaxWidth(Double.MAX_VALUE);

        Label orderTypeHint = subtitle(
                "Standard: regular delivery with no extra order fee.");

        // Scheduled Delivery Date
        DatePicker scheduledDate = new DatePicker(
                LocalDate.now().plusDays(1));

        scheduledDate.setPromptText("Delivery date");
        scheduledDate.setMaxWidth(Double.MAX_VALUE);

        // Scheduled Delivery Time
        ComboBox<String> scheduledTime = new ComboBox<>();

        for (int hour = 0; hour < 24; hour++) {

            scheduledTime.getItems().add(
                    String.format(Locale.US, "%02d:00", hour));

            scheduledTime.getItems().add(
                    String.format(Locale.US, "%02d:30", hour));
        }

        scheduledTime.setValue("18:00");
        scheduledTime.setMaxWidth(Double.MAX_VALUE);

        VBox scheduledBox = new VBox(
                8,
                section("Scheduled Delivery Date"),
                scheduledDate,
                section("Scheduled Delivery Time"),
                scheduledTime
        );

        scheduledBox.setVisible(false);
        scheduledBox.setManaged(false);

        // Dynamic Pricing
        ComboBox<String> pricing = new ComboBox<>();

        pricing.getItems().addAll(
                "Normal",
                "Peak Hour",
                "Express Delivery",
                "Promotion"
        );

        pricing.setValue(selectedPricing);
        pricing.setMaxWidth(Double.MAX_VALUE);

        // Payment Method
        ComboBox<String> payment = new ComboBox<>();

        payment.getItems().add("Cash on Delivery");

        payment.setValue("Cash on Delivery");
        payment.setMaxWidth(Double.MAX_VALUE);

        // Checkout Form
        VBox form = card(450);

        form.getChildren().addAll(
                section("Delivery Information"),
                customer,
                phone,
                address,

                section("Order Type"),
                orderType,
                orderTypeHint,
                scheduledBox,

                section("Delivery Distance"),
                distance,

                section("Pricing Strategy"),
                pricing,

                section("Payment Method"),
                payment
        );

        VBox summaryContainer = new VBox();

        // Update Summary Dynamically
        Runnable updateSummary = () -> {

            try {
                double enteredDistance =
                        Double.parseDouble(
                                distance.getText().trim());

                if (!Double.isFinite(enteredDistance)
                        || enteredDistance < 0) {
                    return;
                }

                deliveryDistanceKm = enteredDistance;
                selectedPricing = pricing.getValue();
                selectedOrderType = orderType.getValue();

                boolean scheduled =
                        "Scheduled".equals(selectedOrderType);

                orderTypeHint.setText(switch (selectedOrderType) {
                    case "Express" -> "Express: priority delivery with an additional "
                            + money(EXPRESS_FEE) + " express fee.";
                    case "Scheduled" -> "Scheduled: choose a future delivery date and time. "
                            + "No additional order-type fee is charged.";
                    default -> "Standard: regular delivery with no additional order-type fee.";
                });

                scheduledBox.setVisible(scheduled);
                scheduledBox.setManaged(scheduled);

                summaryContainer.getChildren().setAll(
                        orderSummary()
                );

            } catch (NumberFormatException ignored) {
            }
        };

        pricing.setOnAction(e -> updateSummary.run());

        orderType.setOnAction(e -> updateSummary.run());

        distance.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        updateSummary.run()
        );

        updateSummary.run();

        // Confirm Order
        Button confirm = primary("Confirm Order");
        confirm.setMaxWidth(Double.MAX_VALUE);

        confirm.setOnAction(e -> {

            // Read and trim customer information
            String customerName = customer.getText().trim();
            String phoneNumber = phone.getText().trim();
            String deliveryAddress = address.getText().trim();

            // Validate customer name
            if (customerName.length() < 2) {

                checkoutWarning(
                        "Please enter a valid customer name (at least 2 characters)."
                );

                customer.requestFocus();
                return;
            }

            // Normalize phone number before validation
            String normalizedPhone =
                    phoneNumber.replaceAll("[\\s()\\-]", "");

            if (!normalizedPhone.matches("\\+?[0-9]{7,15}")) {

                checkoutWarning(
                        "Enter a valid phone number using 7 to 15 digits."
                );

                phone.requestFocus();
                return;
            }

            // Validate delivery address
            if (deliveryAddress.length() < 5) {

                checkoutWarning(
                        "Please enter a more complete delivery address."
                );

                address.requestFocus();
                return;
            }

            // Validate cart
            if (cart.isEmpty()) {

                checkoutWarning(
                        "Your cart is empty. Add at least one available meal."
                );

                showCart();
                return;
            }

            double enteredDistance;

            try {
                enteredDistance = Double.parseDouble(
                        distance.getText().trim());

                if (!Double.isFinite(enteredDistance)
                        || enteredDistance < 0) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException ex) {

                checkoutWarning(
                        "Please enter a valid delivery distance."
                );

                distance.requestFocus();
                return;
            }

            deliveryDistanceKm = enteredDistance;

            selectedPricing = pricing.getValue();
            selectedOrderType = orderType.getValue();

            // Validate Order Type and Scheduled Delivery
            LocalDateTime requestedDeliveryTime = null;

            OrderType selectedType;

            try {

                selectedType = OrderType.valueOf(
                        selectedOrderType.toUpperCase(Locale.ROOT));

                if (selectedType == OrderType.SCHEDULED) {

                    if (scheduledDate.getValue() == null
                            || scheduledTime.getValue() == null) {

                        throw new IllegalArgumentException(
                                "Please select a delivery date and time.");
                    }

                    requestedDeliveryTime = LocalDateTime.of(
                            scheduledDate.getValue(),
                            LocalTime.parse(scheduledTime.getValue()));

                    if (!requestedDeliveryTime.isAfter(
                            LocalDateTime.now())) {

                        throw new IllegalArgumentException(
                                "Scheduled delivery must be in the future.");
                    }
                }

            } catch (RuntimeException ex) {

                checkoutWarning(
                        ex.getMessage() == null
                                ? "Please select a valid order type and schedule."
                                : ex.getMessage()
                );

                return;
            }

            double minimumOrder =
                    cart.get(0).restaurant.model.getMinimumOrderValue();

            if (subtotal() < minimumOrder) {

                checkoutWarning(
                        "The minimum order for "
                        + cart.get(0).restaurant.name
                        + " is " + money(minimumOrder) + "."
                );

                return;
            }

            List<String> items = new ArrayList<>();

            for (CartItem item : cart) {
                items.add(
                        item.quantity + " x " + item.meal.name);
            }

            try {

                AppStore.CustomerOrder saved =
                        AppStore.get().placeOrder(
                                customerName,
                                phoneNumber,
                                deliveryAddress,
                                cart.get(0).restaurant.name,
                                payment.getValue(),
                                items,
                                orderTotal(),
                                selectedType,
                                requestedDeliveryTime
                        );

                cart.clear();
                selectedRestaurant = null;

                // Success Message
                Alert success = new Alert(
                        Alert.AlertType.INFORMATION,

                        "Order " + saved.id +
                        " created successfully.\n" +

                        "Status: " + saved.getStatus() + "\n" +

                        "Order Type: " + saved.getOrderType() + "\n" +

                        (saved.getScheduledDeliveryTime() == null
                                ? ""
                                : "Scheduled For: "
                                  + saved.getScheduledDeliveryTime().format(
                                          DateTimeFormatter.ofPattern(
                                                  "yyyy-MM-dd HH:mm"))
                                  + "\n") +

                        "Total: " + money(saved.total) + "\n\n" +

                        "Open Orders to manage this order."
                );

                success.setHeaderText("Order Created");
                success.showAndWait();

                showRestaurants();

            } catch (IllegalArgumentException
                    | IllegalStateException ex) {

                new Alert(
                        Alert.AlertType.ERROR,
                        ex.getMessage()
                ).showAndWait();
            }
        });

        form.getChildren().add(confirm);

        content.getChildren().addAll(
                back,
                title("Checkout"),
                form,
                summaryContainer
        );
    }

    // ---------------------------------------
    // CHECKOUT VALIDATION WARNING
    // ---------------------------------------

    private void checkoutWarning(String message) {

        Alert warning = new Alert(
                Alert.AlertType.WARNING
        );

        warning.setTitle("Checkout Validation");

        warning.setHeaderText(
                "Please check your information"
        );

        warning.setContentText(message);

        warning.showAndWait();
    }

    // ---------------------------------------
    // ORDER SUMMARY
    // ---------------------------------------

    private VBox orderSummary() {

        VBox summary = card(450);

        summary.getChildren().addAll(
                section("Order Summary"),

                summaryRow("Subtotal", subtotal()),

                summaryRow(
                        "Pricing Adjustment",
                        pricingAdjustment()
                ),

                summaryRow(
                        "Express Delivery Fee",
                        expressOrderFee()
                ),

                summaryRow(
                        "Delivery Fee",
                        deliveryFee()
                ),

                summaryRow(
                        "Total",
                        orderTotal()
                )
        );

        return summary;
    }

    private HBox summaryRow(
            String name,
            double amount) {

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = subtitle(name);
        HBox.setHgrow(label, Priority.ALWAYS);

        row.getChildren().addAll(
                label,
                strong(money(amount))
        );

        return row;
    }

    // ---------------------------------------
    // PRICE CALCULATIONS
    // ---------------------------------------

    private double subtotal() {

        double total = 0;

        for (CartItem item : cart) {
            total += item.meal.price * item.quantity;
        }

        return total;
    }

    private double deliveryFee() {

        if (cart.isEmpty()) {
            return 0;
        }

        return deliveryFeeCalculator.calculateDeliveryFee(
                deliveryDistanceKm,
                DELIVERY_RATE_PER_KM
        );
    }

    private PricingStrategy pricingStrategy() {

        return switch (selectedPricing) {

            case "Peak Hour" ->
                    new PeakHourPricingStrategy(PEAK_MULTIPLIER);

            case "Promotion" ->
                    new PromotionalPricingStrategy(
                            PROMOTIONAL_DISCOUNT);

            case "Express Delivery" ->
                    new ExpressPricingStrategy(EXPRESS_FEE);

            default ->
                    new NormalPricingStrategy();
        };
    }

    private double pricedSubtotal() {

        return pricingStrategy().calculatePrice(
                subtotal()
        );
    }

    private double pricingAdjustment() {

        return pricedSubtotal() - subtotal();
    }

    private double expressOrderFee() {

        return "Express".equals(selectedOrderType)
                && !"Express Delivery".equals(selectedPricing)
                ? EXPRESS_FEE
                : 0.0;
    }

    private double orderTotal() {

        return pricedSubtotal()
                + expressOrderFee()
                + deliveryFee();
    }

    // ---------------------------------------
    // CART BUTTON
    // ---------------------------------------

    private Button cartButton() {

        int count = 0;

        for (CartItem item : cart) {
            count += item.quantity;
        }

        Button button = primary(
                "Cart (" + count + ")");

        button.setOnAction(e -> showCart());

        return button;
    }

    // ---------------------------------------
    // IMAGES
    // ---------------------------------------

    private Node foodImage(
            String fileName,
            double width,
            double height) {

        URL url = getClass().getResource(
                "/images/" + fileName);

        if (url == null) {
            return missingImage(width, height);
        }

        Image image = new Image(
                url.toExternalForm(),
                false
        );

        if (image.isError()) {
            return missingImage(width, height);
        }

        ImageView view = new ImageView(image);

        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        view.setSmooth(true);

        Rectangle clip = new Rectangle(
                width,
                height
        );

        clip.setArcWidth(18);
        clip.setArcHeight(18);

        view.setClip(clip);

        return view;
    }

    private Node missingImage(
            double width,
            double height) {

        Label label = new Label("Food Image");

        label.setPrefSize(width, height);
        label.setAlignment(Pos.CENTER);

        label.setStyle(
                "-fx-background-color: #E8F5EF;" +
                "-fx-text-fill: #103B36;" +
                "-fx-background-radius: 12;"
        );

        return label;
    }

    // ---------------------------------------
    // UI COMPONENTS
    // ---------------------------------------

    private VBox card(double width) {

        VBox card = new VBox(12);

        card.setPrefWidth(width);
        card.setMaxWidth(width);
        card.setPadding(new Insets(14));

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #E4EEEA;" +
                "-fx-border-radius: 16;"
        );

        return card;
    }

    private Label title(String text) {

        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + GREEN + ";"
        );

        return label;
    }

    private Label section(String text) {

        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + GREEN + ";"
        );

        return label;
    }

    private Label strong(String text) {

        Label label = new Label(text);
        label.setWrapText(true);

        label.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + GREEN + ";"
        );

        return label;
    }

    private Label subtitle(String text) {

        Label label = new Label(text);
        label.setWrapText(true);

        label.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: " + MUTED + ";"
        );

        return label;
    }

    private Button primary(String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color: " + GREEN + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10 17;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        return button;
    }

    private Button secondary(String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color: #E8F3EF;" +
                "-fx-text-fill: " + GREEN + ";" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 10 17;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
        );

        return button;
    }

    private TextField input(String placeholder) {

        TextField field = new TextField();

        field.setPromptText(placeholder);
        field.setPrefHeight(42);

        field.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #D9E7E1;" +
                "-fx-border-radius: 10;" +
                "-fx-padding: 10;"
        );

        return field;
    }

    private void showAlert(String header, String message) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                message);

        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private String money(double amount) {

        return String.format(
                Locale.US,
                "₪%.2f",
                amount
        );
    }

    // ---------------------------------------
    // ADD RESTAURANT
    // ---------------------------------------

    public void addRestaurantCard(
            String name,
            String description) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Restaurant name cannot be empty.");
        }

        String id = "R" + NEXT_RESTAURANT_ID.getAndIncrement();

        com.foodorbit.domain.model.Restaurant model =
                new com.foodorbit.domain.model.Restaurant(
                        id,
                        name.trim(),
                        true,
                        true,
                        0.0,
                        0.0);

        AppStore.get().addRestaurant(model);

        restaurants.add(new Restaurant(
                model,
                description == null || description.isBlank()
                        ? "Restaurant"
                        : description.trim(),
                "Other",
                0.0,
                "N/A",
                ""));

        if (selectedRestaurant == null) {
            showRestaurants();
        }
    }

    // ---------------------------------------
    // RESTAURANT PRESENTATION MODEL
    // ---------------------------------------

    private static class Restaurant {

        private final com.foodorbit.domain.model.Restaurant model;
        private final String name;
        private final String description;
        private final String category;
        private final double rating;
        private final String deliveryTime;
        private final String imageFile;

        private final List<Meal> menu = new ArrayList<>();

        private Restaurant(
                com.foodorbit.domain.model.Restaurant model,
                String description,
                String category,
                double rating,
                String deliveryTime,
                String imageFile) {

            this.model = model;
            this.name = model.getName();
            this.description = description;
            this.category = category;
            this.rating = rating;
            this.deliveryTime = deliveryTime;
            this.imageFile = imageFile;
        }
    }

    // ---------------------------------------
    // MEAL PRESENTATION MODEL
    // ---------------------------------------

    private static class Meal {

        private final MenuItem model;
        private final String name;
        private final String description;
        private final double price;
        private final boolean recommended;
        private final String imageFile;

        private Meal(
                MenuItem model,
                String description,
                boolean recommended,
                String imageFile) {

            this.model = model;
            this.name = model.getName();
            this.description = description;
            this.price = model.getPrice();
            this.recommended = recommended;
            this.imageFile = imageFile;
        }
    }

    // ---------------------------------------
    // CART ITEM MODEL
    // ---------------------------------------

    private static class CartItem {

        private final Restaurant restaurant;
        private final Meal meal;

        private int quantity = 1;

        private CartItem(
                Restaurant restaurant,
                Meal meal) {

            this.restaurant = restaurant;
            this.meal = meal;
        }
    }
}