package com.foodorbit.presentation;

import java.net.URL;
import java.util.UUID;
import java.util.function.Consumer;

import com.foodorbit.application.auth.AuthenticationService;
import com.foodorbit.domain.model.Customer;
import com.foodorbit.domain.model.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Displays the FoodOrbit sign-in and customer registration screens.
 */
public class LoginPage extends StackPane {

    private final AuthenticationService authenticationService;
    private final Consumer<User> onLogin;

    private final VBox formContent = new VBox(14);
    private final Label feedback = new Label();

    private TextField usernameField;
    private PasswordField passwordField;
    private TextField registrationUsernameField;
    private PasswordField registrationPasswordField;
    private PasswordField confirmPasswordField;

    /**
     * Creates the login page.
     *
     * @param authenticationService service used to authenticate and register users
     * @param onLogin action performed after a successful login
     */
    public LoginPage(
            AuthenticationService authenticationService,
            Consumer<User> onLogin) {

        if (authenticationService == null || onLogin == null) {
            throw new IllegalArgumentException(
                    "Authentication service and login action are required.");
        }

        this.authenticationService = authenticationService;
        this.onLogin = onLogin;

        getStyleClass().add("login-root");
        setPadding(new Insets(30));
        setAlignment(Pos.CENTER);

        // Keep the login design independent from the workspace stylesheet.
        URL loginStylesheet = getClass().getResource("/css/login.css");
        if (loginStylesheet != null) {
            getStylesheets().add(loginStylesheet.toExternalForm());
        }

        HBox layout = new HBox();
        layout.getStyleClass().add("login-layout");
        layout.setPrefSize(1000, 580);
        layout.setMaxWidth(1080);
        layout.setMaxHeight(640);

        VBox brandPanel = createBrandPanel();
        VBox formPanel = createFormPanel();
        HBox.setHgrow(formPanel, Priority.ALWAYS);

        layout.getChildren().addAll(brandPanel, formPanel);
        getChildren().add(layout);

        showLoginForm();
    }

    private VBox createBrandPanel() {

        VBox panel = new VBox(20);
        panel.getStyleClass().add("login-brand-panel");
        panel.setPrefWidth(410);
        panel.setMinWidth(320);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(42));

        Label logo = new Label("◉  FoodOrbit");
        logo.getStyleClass().add("login-brand-title");

        Label eyebrow = new Label("SMART FOOD DELIVERY MANAGEMENT");
        eyebrow.getStyleClass().add("login-eyebrow");

        Label headline = new Label("Everything in one orbit.");
        headline.getStyleClass().add("login-brand-headline");
        headline.setWrapText(true);

        Label description = new Label(
                "Manage restaurants, orders, drivers and payments "
                + "from one simple workspace.");
        description.getStyleClass().add("login-brand-description");
        description.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label footer = new Label("FOODORBIT  •  DELIVERY STUDIO");
        footer.getStyleClass().add("login-brand-footer");

        panel.getChildren().addAll(
                logo, eyebrow, headline, description, spacer, footer);

        return panel;
    }

    private VBox createFormPanel() {

        VBox panel = new VBox();
        panel.getStyleClass().add("login-form-panel");
        panel.setPadding(new Insets(48, 58, 48, 58));
        panel.setPrefWidth(590);
        panel.setMinWidth(430);
        panel.setAlignment(Pos.CENTER_LEFT);

        formContent.setMaxWidth(450);
        formContent.setFillWidth(true);
        panel.getChildren().add(formContent);

        return panel;
    }

    private void showLoginForm() {

        Label title = new Label("Welcome back");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label(
                "Sign in to continue to your FoodOrbit workspace.");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setWrapText(true);

        Label usernameLabel = fieldLabel("Username");
        usernameField = new TextField();
        usernameField.setId("loginUsernameField");
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("login-input");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        Label passwordLabel = fieldLabel("Password");
        passwordField = new PasswordField();
        passwordField.setId("loginPasswordField");
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("login-input");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        usernameField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> attemptLogin());

        Button loginButton = new Button("Sign in");
        loginButton.setId("loginButton");
        loginButton.getStyleClass().add("login-primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> attemptLogin());
        loginButton.setDefaultButton(true);

        Button registerButton = new Button("Create a customer account");
        registerButton.setId("showRegistrationButton");
        registerButton.getStyleClass().add("login-link-button");
        registerButton.setOnAction(event -> showRegistrationForm());

        Label registerText = new Label("New to FoodOrbit?");
        registerText.getStyleClass().add("login-small-text");
        HBox registerRow = new HBox(5, registerText, registerButton);
        registerRow.setAlignment(Pos.CENTER);

        Label demo = new Label(
                "Demo manager account\nUsername: manager\nPassword: FoodOrbit@2026");
        demo.getStyleClass().add("login-demo-note");
        demo.setWrapText(true);
        demo.setMaxWidth(Double.MAX_VALUE);

        prepareFeedback();

        formContent.getChildren().setAll(
                title,
                subtitle,
                fieldSpacing(),
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                feedback,
                loginButton,
                registerRow,
                demo
        );
    }

    private void showRegistrationForm() {

        Label title = new Label("Create your account");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label(
                "Register as a customer to start ordering.");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setWrapText(true);

        Label usernameLabel = fieldLabel("Username");
        registrationUsernameField = new TextField();
        registrationUsernameField.setId("registerUsernameField");
        registrationUsernameField.setPromptText("Choose a username");
        registrationUsernameField.getStyleClass().add("login-input");
        registrationUsernameField.setMaxWidth(Double.MAX_VALUE);

        Label passwordLabel = fieldLabel("Password");
        registrationPasswordField = new PasswordField();
        registrationPasswordField.setId("registerPasswordField");
        registrationPasswordField.setPromptText("Choose a password");
        registrationPasswordField.getStyleClass().add("login-input");
        registrationPasswordField.setMaxWidth(Double.MAX_VALUE);
        registrationPasswordField.setOnAction(
                event -> confirmPasswordField.requestFocus());

        Label confirmLabel = fieldLabel("Confirm password");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setId("confirmPasswordField");
        confirmPasswordField.setPromptText("Enter your password again");
        confirmPasswordField.getStyleClass().add("login-input");
        confirmPasswordField.setMaxWidth(Double.MAX_VALUE);
        confirmPasswordField.setOnAction(event -> registerCustomer());

        Button createButton = new Button("Create account");
        createButton.setId("registerButton");
        createButton.getStyleClass().add("login-primary-button");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setOnAction(event -> registerCustomer());

        Button backButton = new Button("Back to sign in");
        backButton.setId("backToLoginButton");
        backButton.getStyleClass().add("login-link-button");
        backButton.setOnAction(event -> showLoginForm());

        prepareFeedback();

        formContent.getChildren().setAll(
                title,
                subtitle,
                fieldSpacing(),
                usernameLabel,
                registrationUsernameField,
                passwordLabel,
                registrationPasswordField,
                confirmLabel,
                confirmPasswordField,
                feedback,
                createButton,
                backButton
        );
    }

    private Label fieldLabel(String value) {
        Label label = new Label(value);
        label.getStyleClass().add("login-field-label");
        return label;
    }

    private Region fieldSpacing() {
        Region spacer = new Region();
        spacer.setMinHeight(3);
        return spacer;
    }

    private void prepareFeedback() {
        feedback.setId("loginFeedback");
        feedback.getStyleClass().setAll("login-feedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(18);
    }

    private void attemptLogin() {

        if (usernameField.getText() == null
                || usernameField.getText().isBlank()
                || passwordField.getText() == null
                || passwordField.getText().isBlank()) {
            feedback.setText("Enter your username and password.");
            return;
        }

        User user = authenticationService.login(
                usernameField.getText(),
                passwordField.getText());

        if (user == null) {
            feedback.setText(authenticationService.getErrorMessage());
            return;
        }

        onLogin.accept(user);
    }

    private void registerCustomer() {

        String username = registrationUsernameField.getText();
        String password = registrationPasswordField.getText();
        String confirmation = confirmPasswordField.getText();

        if (username == null || username.isBlank()) {
            feedback.setText("Choose a username.");
            registrationUsernameField.requestFocus();
            return;
        }

        if (password == null || password.isBlank()) {
            feedback.setText("Choose a password.");
            registrationPasswordField.requestFocus();
            return;
        }

        if (confirmation == null || confirmation.isBlank()) {
            feedback.setText("Confirm your password.");
            confirmPasswordField.requestFocus();
            return;
        }

        if (!password.equals(confirmation)) {
            feedback.setText("The passwords do not match.");
            return;
        }

        try {
            Customer customer = new Customer(
                    "C-" + UUID.randomUUID(),
                    username == null ? null : username.trim(),
                    password);

            authenticationService.registerUser(customer);
            onLogin.accept(customer);

        } catch (IllegalArgumentException exception) {
            feedback.setText(exception.getMessage());
        }
    }
}
