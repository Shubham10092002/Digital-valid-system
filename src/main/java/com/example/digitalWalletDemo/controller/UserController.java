package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.UserDTO;
import com.example.digitalWalletDemo.service.UserService;
import com.example.digitalWalletDemo.model.User;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Create user
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        Object response = userService.createUser(userDTO, result);

        if (response instanceof String) {
            // Validation or duplicate username error
            return ResponseEntity.badRequest().body(response);
        } else if (response instanceof java.util.Map<?, ?>) {
            // Field validation errors
            return ResponseEntity.badRequest().body(response);
        }

        // Success
        return ResponseEntity.ok(response);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Object response = userService.getUserById(id);

        if (response instanceof String && ((String) response).equals("User not found")) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
