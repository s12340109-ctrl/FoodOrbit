
package com.foodorbit.application.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.foodorbit.domain.model.User;

class AuthenticationServiceTest {

    @Test
    void shouldRegisterUserSuccessfully() {

        AuthenticationService service = new AuthenticationService();

        User user = new User(
            "U001",
            "safaa",
            "123456",
            "CUSTOMER"
        );

        service.registerUser(user);

        assertEquals(1, service.getUsers().size());
        assertSame(user, service.getUsers().get(0));
    }

@Test
void shouldRejectDuplicateUsername() {

    AuthenticationService service = new AuthenticationService();

    User firstUser = new User(
        "U001",
        "safaa",
        "123456",
        "CUSTOMER"
    );

    User secondUser = new User(
        "U002",
        "SAFAA",
        "654321",
        "MANAGER"
    );

    service.registerUser(firstUser);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.registerUser(secondUser)
    );

    assertEquals(
        "Username already exists.",
        exception.getMessage()
    );

    assertEquals(1, service.getUsers().size());
}

@Test
void shouldLoginSuccessfully() {

    AuthenticationService service = new AuthenticationService();

    User user = new User(
        "U001",
        "safaa",
        "123456",
        "CUSTOMER"
    );

    service.registerUser(user);

    User loggedInUser = service.login(
        "safaa",
        "123456"
    );

    assertNotNull(loggedInUser);
    assertSame(user, loggedInUser);
    assertNull(service.getErrorMessage());
}

@Test
void shouldRejectIncorrectPassword() {

    AuthenticationService service = new AuthenticationService();

    User user = new User(
        "U001",
        "safaa",
        "123456",
        "CUSTOMER"
    );

    service.registerUser(user);

    User loggedInUser = service.login(
        "safaa",
        "wrongPassword"
    );

    assertNull(loggedInUser);

    assertEquals(
        "Invalid username or password.",
        service.getErrorMessage()
    );
}

@Test
void shouldRejectMissingLoginCredentials() {

    AuthenticationService service = new AuthenticationService();

    assertNull(service.login(null, "123456"));
    assertEquals(
        "Username and password are required.",
        service.getErrorMessage()
    );

    assertNull(service.login("", "123456"));
    assertEquals(
        "Username and password are required.",
        service.getErrorMessage()
    );

    assertNull(service.login("   ", "123456"));

    assertNull(service.login("safaa", null));

    assertNull(service.login("safaa", ""));

    assertNull(service.login("safaa", "   "));

    assertEquals(
        "Username and password are required.",
        service.getErrorMessage()
    );
}

@Test
void shouldRejectNullUser() {

    AuthenticationService service = new AuthenticationService();

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.registerUser(null)
    );

    assertEquals(
        "User cannot be null.",
        exception.getMessage()
    );

    assertTrue(service.getUsers().isEmpty());

}

@Test
void shouldPreventModifyingUsersList() {

    AuthenticationService service = new AuthenticationService();

    User user = new User(
        "U001",
        "safaa",
        "123456",
        "CUSTOMER"
    );

    service.registerUser(user);

    assertThrows(
        UnsupportedOperationException.class,
        () -> service.getUsers().clear()
    );

    assertEquals(1, service.getUsers().size());
}

@Test
void shouldClearErrorMessageAfterSuccessfulLogin() {

    AuthenticationService service = new AuthenticationService();

    User user = new User(
        "U001",
        "safaa",
        "123456",
        "CUSTOMER"
    );

    service.registerUser(user);

    User failedLogin = service.login("safaa", "wrongPassword");

    assertNull(failedLogin);
    assertEquals(
        "Invalid username or password.",
        service.getErrorMessage()
    );

    User successfulLogin = service.login("safaa", "123456");

    assertSame(user, successfulLogin);
    assertNull(service.getErrorMessage());
}

}
