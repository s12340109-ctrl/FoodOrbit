
package com.foodorbit.presentation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class FoodOrbitAppTest {

	@BeforeAll
	static void initializeJavaFX() throws Exception {

	    CountDownLatch latch = new CountDownLatch(1);

	    try {
	        Platform.startup(latch::countDown);
	    } catch (IllegalStateException ex) {
	        // JavaFX Toolkit was already initialized.
	        Platform.runLater(latch::countDown);
	    }

	    assertTrue(
	            latch.await(15, TimeUnit.SECONDS),
	            "JavaFX Toolkit did not start"
	    );

	    Platform.setImplicitExit(false);
	}

    @Test
    void shouldCreateFoodOrbitAppSuccessfully() {

        FoodOrbitApp app = new FoodOrbitApp();

        assertNotNull(app);
    }

    @Test
    void shouldShowLoginPageBeforeAuthentication() throws Exception {

        runOnJavaFX(stage -> {
            BorderPane root = getRoot(stage);
            assertInstanceOf(LoginPage.class, root.getCenter());
            assertNotNull(root.lookup("#loginUsernameField"));
            assertNotNull(root.lookup("#loginPasswordField"));
            assertNotNull(root.lookup("#loginButton"));
        });
    }

    @Test
    void shouldDisplayFoodOrbitDashboardAfterLogin() throws Exception {

        runOnJavaFX(stage -> {
            login(stage);
            BorderPane root = getRoot(stage);

            assertEquals(
                    "FoodOrbit • Delivery Management Studio",
                    stage.getTitle()
            );

            assertNotNull(stage.getScene());
            assertTrue(stage.isShowing());

            assertEquals(
                    "Dashboard",
                    getPageTitle(root).getText()
            );

            assertTrue(
                    getNavigationButton(root, "Dashboard")
                            .getStyleClass()
                            .contains("sidebar-button-active")
            );

            assertNotNull(getPageContent(root));
        });
    }

    @Test
    void shouldNavigateToOrdersPage() throws Exception {

        testNavigation("Orders");
    }

    @Test
    void shouldNavigateToDriversPage() throws Exception {

        testNavigation("Drivers");
    }

    @Test
    void shouldNavigateToPaymentsPage() throws Exception {

        testNavigation("Payments");
    }

    @Test
    void shouldNavigateToNotificationsPage() throws Exception {

        testNavigation("Notifications");
    }

    @Test
    void shouldReturnToDashboardAfterNavigatingToOrders()
            throws Exception {

        runOnJavaFX(stage -> {

            login(stage);
            BorderPane root = getRoot(stage);

            Button dashboardButton =
                    getNavigationButton(root, "Dashboard");

            Button ordersButton =
                    getNavigationButton(root, "Orders");

            ordersButton.fire();

            assertEquals(
                    "Orders",
                    getPageTitle(root).getText()
            );

            assertTrue(
                    ordersButton.getStyleClass()
                            .contains("sidebar-button-active")
            );

            dashboardButton.fire();

            assertEquals(
                    "Dashboard",
                    getPageTitle(root).getText()
            );

            assertTrue(
                    dashboardButton.getStyleClass()
                            .contains("sidebar-button-active")
            );

            assertFalse(
                    ordersButton.getStyleClass()
                            .contains("sidebar-button-active")
            );

            assertNotNull(getPageContent(root));
        });
    }

    @Test
    void shouldRejectInvalidLoginCredentials() throws Exception {

        runOnJavaFX(stage -> {
            BorderPane root = getRoot(stage);
            TextField username =
                    (TextField) root.lookup("#loginUsernameField");
            PasswordField password =
                    (PasswordField) root.lookup("#loginPasswordField");
            Button loginButton =
                    (Button) root.lookup("#loginButton");

            username.setText("manager");
            password.setText("wrong-password");
            loginButton.fire();

            assertInstanceOf(LoginPage.class, root.getCenter());
            Label feedback = (Label) root.lookup("#loginFeedback");
            assertEquals("Invalid username or password.", feedback.getText());
        });
    }

    @Test
    void shouldShowClearMessageWhenLoginFieldsAreEmpty() throws Exception {

        runOnJavaFX(stage -> {
            BorderPane root = getRoot(stage);
            Button loginButton =
                    (Button) root.lookup("#loginButton");

            loginButton.fire();

            Label feedback = (Label) root.lookup("#loginFeedback");
            assertEquals(
                    "Enter your username and password.",
                    feedback.getText()
            );
            assertInstanceOf(LoginPage.class, root.getCenter());
        });
    }

    @Test
    void shouldRejectCustomerRegistrationWhenPasswordsDoNotMatch()
            throws Exception {

        runOnJavaFX(stage -> {
            BorderPane root = getRoot(stage);
            ((Button) root.lookup("#showRegistrationButton")).fire();

            ((TextField) root.lookup("#registerUsernameField"))
                    .setText("customer2");
            ((PasswordField) root.lookup("#registerPasswordField"))
                    .setText("password123");
            ((PasswordField) root.lookup("#confirmPasswordField"))
                    .setText("different123");
            ((Button) root.lookup("#registerButton")).fire();

            Label feedback = (Label) root.lookup("#loginFeedback");
            assertEquals("The passwords do not match.", feedback.getText());
            assertInstanceOf(LoginPage.class, root.getCenter());
        });
    }

    @Test
    void shouldRegisterCustomerAndOpenWorkspace() throws Exception {

        runOnJavaFX(stage -> {
            BorderPane root = getRoot(stage);
            Button showRegistration =
                    (Button) root.lookup("#showRegistrationButton");
            showRegistration.fire();

            TextField username =
                    (TextField) root.lookup("#registerUsernameField");
            PasswordField password =
                    (PasswordField) root.lookup("#registerPasswordField");
            PasswordField confirmation =
                    (PasswordField) root.lookup("#confirmPasswordField");
            Button register =
                    (Button) root.lookup("#registerButton");

            username.setText("newcustomer");
            password.setText("customer123");
            confirmation.setText("customer123");
            register.fire();

            assertEquals("Dashboard", getPageTitle(root).getText());
            assertNotNull(root.lookup("#logoutButton"));
        });
    }

    @Test
    void shouldLogoutAndReturnToLoginPage() throws Exception {

        runOnJavaFX(stage -> {
            login(stage);
            BorderPane root = getRoot(stage);
            Button logout = (Button) root.lookup("#logoutButton");
            assertNotNull(logout);
            logout.fire();
            assertInstanceOf(LoginPage.class, root.getCenter());
        });
    }

    private void login(Stage stage) {
        BorderPane root = getRoot(stage);
        assertInstanceOf(LoginPage.class, root.getCenter());

        TextField username =
                (TextField) root.lookup("#loginUsernameField");
        PasswordField password =
                (PasswordField) root.lookup("#loginPasswordField");
        Button loginButton =
                (Button) root.lookup("#loginButton");

        username.setText("manager");
        password.setText("FoodOrbit@2026");
        loginButton.fire();
    }

    private void testNavigation(String expectedPage)
            throws Exception {

        runOnJavaFX(stage -> {

            login(stage);
            BorderPane root = getRoot(stage);

            Button button =
                    getNavigationButton(root, expectedPage);

            assertNotNull(button);

            button.fire();

            assertEquals(
                    expectedPage,
                    getPageTitle(root).getText()
            );

            assertTrue(
                    button.getStyleClass()
                            .contains("sidebar-button-active")
            );

            assertNotNull(getPageContent(root));

            assertEquals(
                    1,
                    getPageHost(root).getChildren().size()
            );
        });
    }

    private BorderPane getRoot(Stage stage) {

        assertNotNull(stage.getScene());

        assertInstanceOf(
                BorderPane.class,
                stage.getScene().getRoot()
        );

        return (BorderPane) stage.getScene().getRoot();
    }

    private Button getNavigationButton(
            BorderPane root,
            String pageName) {

        assertInstanceOf(VBox.class, root.getLeft());

        VBox sidebar = (VBox) root.getLeft();

        for (Node node : sidebar.getChildren()) {

            if (node instanceof Button button) {

                String text = button.getText().trim();

                if (text.equals(pageName)
                        || text.endsWith(" " + pageName)) {

                    return button;
                }
            }
        }

        fail("Navigation button not found: " + pageName);

        return null;
    }

    private Label getPageTitle(BorderPane root) {

        assertInstanceOf(VBox.class, root.getCenter());

        VBox workspace = (VBox) root.getCenter();

        assertFalse(workspace.getChildren().isEmpty());

        assertInstanceOf(
                HBox.class,
                workspace.getChildren().get(0)
        );

        HBox topbar =
                (HBox) workspace.getChildren().get(0);

        assertFalse(topbar.getChildren().isEmpty());

        assertInstanceOf(
                VBox.class,
                topbar.getChildren().get(0)
        );

        VBox heading =
                (VBox) topbar.getChildren().get(0);

        assertFalse(heading.getChildren().isEmpty());

        assertInstanceOf(
                Label.class,
                heading.getChildren().get(0)
        );

        return (Label) heading.getChildren().get(0);
    }

    private StackPane getPageHost(BorderPane root) {

        assertInstanceOf(VBox.class, root.getCenter());

        VBox workspace = (VBox) root.getCenter();

        assertTrue(workspace.getChildren().size() >= 2);

        assertInstanceOf(
                StackPane.class,
                workspace.getChildren().get(1)
        );

        return (StackPane) workspace.getChildren().get(1);
    }

    private Node getPageContent(BorderPane root) {

        StackPane host = getPageHost(root);

        assertEquals(1, host.getChildren().size());

        Node page = host.getChildren().get(0);

        assertNotNull(page);

        return page;
    }

    private void runOnJavaFX(
            StageAssertion assertion) throws Exception {

        CountDownLatch latch = new CountDownLatch(1);

        AtomicReference<Throwable> error =
                new AtomicReference<>();

        Platform.runLater(() -> {

            Stage stage = null;

            try {

                stage = new Stage();

                FoodOrbitApp app = new FoodOrbitApp();

                app.start(stage);

                assertion.verify(stage);

            } catch (Throwable exception) {

                error.set(exception);

            } finally {

                if (stage != null) {
                    stage.close();
                }

                latch.countDown();
            }
        });

        assertTrue(
                latch.await(15, TimeUnit.SECONDS),
                "JavaFX test timed out"
        );

        if (error.get() != null) {
            fail("JavaFX test failed", error.get());
        }
    }

    @FunctionalInterface
    private interface StageAssertion {

        void verify(Stage stage) throws Exception;
    }
}
