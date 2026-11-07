
package com.foodorbit.application.auth;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.foodorbit.domain.model.User;

/**
 * Provides authentication for SufraGo users.
 */
public class AuthenticationService {

    /**
     * Stores all registered users.
     */
    private final List<User> users;

    /**
     * Stores the error message from the most recent login attempt.
     */
    private String errorMessage;

    /**
     * Creates an AuthenticationService with an empty user list
     * and no error message.
     */
    public AuthenticationService() {
        this.users = new ArrayList<>();
        this.errorMessage = null;
    }

    /**
     * Registers a user for authentication.
     *
     * @param user the user to register
     * @throws IllegalArgumentException if the user is null
     *         or the username already exists
     */
    public void registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        boolean usernameExists = users.stream()
                .anyMatch(existing ->
                        existing.getUsername().equalsIgnoreCase(
                                user.getUsername()));

        if (usernameExists) {
            throw new IllegalArgumentException(
                    "Username already exists.");
        }

        users.add(user);
    }

    /**
     * Authenticates a user using a username and password.
     *
     * @param username the username entered by the user
     * @param password the password entered by the user
     * @return the authenticated user, or null if authentication fails
     */
    public User login(String username, String password) {
        errorMessage = null;

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            errorMessage = "Username and password are required.";
            return null;
        }

        User authenticatedUser = users.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(
                        username.trim()))
                .filter(user -> Objects.equals(
                        user.getPassword(), password))
                .findFirst()
                .orElse(null);

        if (authenticatedUser == null) {
            errorMessage = "Invalid username or password.";
        }

        return authenticatedUser;
    }

    /**
     * Returns the error message from the most recent failed login.
     *
     * @return the error message, or null if the last login succeeded
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns an unmodifiable view of registered users.
     *
     * @return the registered users
     */
    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }
}
