package com.loanportal.controller;

import com.loanportal.model.User;
import com.loanportal.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "registered", required = false) String registered,
                                @RequestParam(value = "message", required = false) String message,
                                HttpSession session,
                                Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            return currentUser.isAdmin() ? "redirect:/dashboard" : "redirect:/my-applications";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username/email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been successfully logged out.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! You can now log in.");
        }
        if (message != null) {
            model.addAttribute("infoMessage", message);
        }

        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam(value = "roleHint", required = false) String roleHint,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.authenticate(username, password);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid credentials. Please verify your username and password.");
            return "redirect:/login";
        }

        User user = userOpt.get();
        session.setAttribute("currentUser", user);

        if (user.isAdmin()) {
            redirectAttributes.addFlashAttribute("successMessage", "Welcome, " + user.getFullName() + " (Administrator / Loan Officer).");
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + user.getFullName() + "! Manage your loan applications below.");
            return "redirect:/my-applications";
        }
    }

    @GetMapping("/quick-login/{role}")
    public String quickLogin(@PathVariable("role") String role,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if ("admin".equalsIgnoreCase(role)) {
            Optional<User> admin = userService.findByUsername("admin");
            if (admin.isPresent()) {
                session.setAttribute("currentUser", admin.get());
                redirectAttributes.addFlashAttribute("successMessage", "Logged in as Administrator (Demo Mode).");
                return "redirect:/dashboard";
            }
        } else {
            Optional<User> user = userService.findByUsername("user");
            if (user.isPresent()) {
                session.setAttribute("currentUser", user.get());
                redirectAttributes.addFlashAttribute("successMessage", "Logged in as Applicant (Rahul Sharma - Demo Mode).");
                return "redirect:/my-applications";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            return currentUser.isAdmin() ? "redirect:/dashboard" : "redirect:/my-applications";
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam("fullName") String fullName,
                                      @RequestParam("username") String username,
                                      @RequestParam("email") String email,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match. Please re-enter.");
            return "redirect:/register";
        }

        try {
            User newUser = new User(username, password, fullName, email, phone, "USER");
            User created = userService.registerUser(newUser);
            session.setAttribute("currentUser", created);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Welcome, " + created.getFullName() + "! Your account was created successfully. You can now apply for loans.");
            return "redirect:/my-applications";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/login";
    }
}
