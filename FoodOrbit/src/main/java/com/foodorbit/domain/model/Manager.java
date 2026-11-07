
package com.foodorbit.domain.model;

/**
 * Represents a manager who can access management functions.
 */
public class Manager extends User {

    /**
     * Creates a manager with the specified information.
     *
     * @param id the unique manager identifier
     * @param username the manager's username
     * @param password the manager's password
     */
    public Manager(String id, String username, String password) {
        super(id, username, password, "MANAGER");
    }
}
