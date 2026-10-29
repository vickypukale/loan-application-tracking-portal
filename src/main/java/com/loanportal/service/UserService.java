package com.loanportal.service;

import com.loanportal.model.User;
import com.loanportal.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Seeds initial default Admin and End User accounts if they do not already exist.
     */
    @PostConstruct
    public void seedDefaultUsers() {
        if (!userRepository.existsByUsernameIgnoreCase("admin")) {
            User admin = new User(
                    "admin",
                    "admin123",
                    "Bank Admin Officer",
                    "admin@loanportal.com",
                    "9876543210",
                    "ADMIN"
            );
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsernameIgnoreCase("user")) {
            User defaultUser = new User(
                    "user",
                    "user123",
                    "Rahul Sharma",
                    "rahul.sharma@example.com",
                    "9876543210",
                    "USER"
            );
            userRepository.save(defaultUser);
        }
    }

    /**
     * Authenticates a user given their username/email and password.
     */
    public Optional<User> authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return Optional.empty();
        }
        String trimmed = usernameOrEmail.trim();

        // Search by username first, then email
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(trimmed);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(trimmed);
        }

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt;
        }
        return Optional.empty();
    }

    /**
     * Registers a new applicant/end-user with role USER.
     */
    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered.");
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("USER");
        }
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
