
package com.foodorbit.domain.model;

/**
 * Represents a customer who can browse restaurants and place orders.
 */
public class Customer extends User {

    /**
     * Creates a customer with the specified information.
     *
     * @param id the unique customer identifier
     * @param username the customer's username
     * @param password the customer's password
     */
    public Customer(String id, String username, String password) {
        super(id, username, password, "CUSTOMER");
    }
}
