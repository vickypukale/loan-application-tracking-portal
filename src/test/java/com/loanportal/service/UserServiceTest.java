package com.loanportal.service;

import com.loanportal.model.User;
import com.loanportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Ensure default users are seeded
        userService.seedDefaultUsers();
    }

    @Test
    @DisplayName("Default accounts: admin and user are pre-seeded")
    void testDefaultAccounts_ArePreSeeded() {
        Optional<User> admin = userService.findByUsername("admin");
        assertTrue(admin.isPresent(), "Admin account should be present");
        assertTrue(admin.get().isAdmin(), "Admin should have ADMIN role");
        assertEquals("Bank Admin Officer", admin.get().getFullName());

        Optional<User> user = userService.findByUsername("user");
        assertTrue(user.isPresent(), "User account should be present");
        assertTrue(user.get().isUser(), "User should have USER role");
        assertEquals("Rahul Sharma", user.get().getFullName());
    }

    @Test
    @DisplayName("Authentication: valid credentials return user")
    void testAuthenticate_Success() {
        Optional<User> authAdmin = userService.authenticate("admin", "admin123");
        assertTrue(authAdmin.isPresent(), "Valid admin credentials should succeed");
        assertEquals("admin", authAdmin.get().getUsername());

        // Authenticate with email
        Optional<User> authByEmail = userService.authenticate("rahul.sharma@example.com", "user123");
        assertTrue(authByEmail.isPresent(), "Authentication by email should succeed");
        assertEquals("user", authByEmail.get().getUsername());
    }

    @Test
    @DisplayName("Authentication: invalid credentials return empty")
    void testAuthenticate_Failure() {
        Optional<User> wrongPass = userService.authenticate("admin", "wrongpass");
        assertTrue(wrongPass.isEmpty(), "Wrong password should fail");

        Optional<User> wrongUser = userService.authenticate("nonexistent", "user123");
        assertTrue(wrongUser.isEmpty(), "Nonexistent user should fail");
    }

    @Test
    @DisplayName("Register: new user registration works")
    void testRegisterUser_Success() {
        String testUser = "priya_" + System.currentTimeMillis();
        User user = new User(testUser, "priya123", "Priya Nair", testUser + "@example.com", "9871112233", "USER");
        User saved = userService.registerUser(user);

        assertNotNull(saved.getId(), "User ID should be generated");
        assertEquals("USER", saved.getRole());
        assertTrue(userService.authenticate(testUser, "priya123").isPresent());
    }

    @Test
    @DisplayName("Register: duplicate username throws exception")
    void testRegisterUser_DuplicateUsernameThrows() {
        User duplicate = new User("admin", "pass", "Duplicate Admin", "other@example.com", "9871112233", "USER");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(duplicate));
    }
}
