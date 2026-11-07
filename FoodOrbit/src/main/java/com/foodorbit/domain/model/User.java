
package com.foodorbit.domain.model;

/**
 * Represents a user who can access the SufraGo system.
 */
public class User {

    /** The unique identifier of the user. */
    private final String id;

    /** The username used to access the system. */
    private final String username;

    /** The user's password. */
    private final String password;

    /** The role assigned to the user. */
    private final String role;

    /**
     * Creates a user with the specified information.
     *
     * @param id the unique user identifier
     * @param username the user's username
     * @param password the user's password
     * @param role the user's role
     * @throws IllegalArgumentException if any argument is null or blank
     */
    public User(String id, String username, String password, String role) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("User ID cannot be empty.");
        }
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (isBlank(password)) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (isBlank(role)) {
            throw new IllegalArgumentException("Role cannot be empty.");
        }

        this.id = id.trim();
        this.username = username.trim();
        this.password = password;
        this.role = role.trim();
    }

    /**
     * Checks whether a string is null or contains only whitespace.
     *
     * @param value the string to check
     * @return true if the string is null or blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Returns the user identifier.
     *
     * @return the user ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the user's password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user's role.
     *
     * @return the user role
     */
    public String getRole() {
        return role;
    }

}
