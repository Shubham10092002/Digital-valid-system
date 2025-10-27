package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.UserRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UserController(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    //  Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    //  Create a new user with a default wallet
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required.");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required.");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username '" + user.getUsername() + "' already exists.");
        }

        // Save the user first
        User savedUser = userRepository.save(user);

        // Create default wallet
        Wallet defaultWallet = new Wallet();
        defaultWallet.setWalletName("Default Wallet");
        defaultWallet.setBalance(BigDecimal.ZERO);
        defaultWallet.setUser(savedUser);

        // Save wallet
        Wallet savedWallet = walletRepository.save(defaultWallet);

        //  Update the user’s wallet list correctly
        savedUser.getWallets().add(savedWallet);
      //  userRepository.save(savedUser); // persist the relationship

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("walletId", savedWallet.getId());
        response.put("walletName", savedWallet.getWalletName());
        response.put("walletBalance", savedWallet.getBalance());

        return ResponseEntity.ok(response);
    }

    //  Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }
}
