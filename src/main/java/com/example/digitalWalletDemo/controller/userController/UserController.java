package com.example.digitalWalletDemo.controller.userController;

import com.example.digitalWalletDemo.dto.userdto.UserDTO;
import com.example.digitalWalletDemo.dto.userdto.UserFullResponseDTO;
import com.example.digitalWalletDemo.dto.userdto.UserResponseDTO;
import com.example.digitalWalletDemo.service.userService.UserService;
import com.example.digitalWalletDemo.model.userModel.User;

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
//    @GetMapping
//    public ResponseEntity<List<User>> getAllUsers() {
//        logger.info("Fetching all users");
//        return ResponseEntity.ok(userService.getAllUsers());
//    }


    @GetMapping
    public ResponseEntity<List<UserFullResponseDTO>> getAllUsers() {
        logger.info("Fetching all users");
        List<UserFullResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }



//    public List<UserFullResponseDTO> getAllUsers() {
//        List<User> users = userRepository.findAll();
//
//        // Convert each User entity to UserFullResponseDTO
//        return users.stream()
//                .map(UserFullResponseDTO::new)
//                .toList();
//    }

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
