package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.UserRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UserController(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    // ================== CREATE USER with Wallet ==================
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        logger.info("Received request to create user: {}", user.getUsername());

        // Validate username and password
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            logger.warn("Username is missing in request");
            return ResponseEntity.badRequest().body("Username is required.");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            logger.warn("Password is missing for username: {}", user.getUsername());
            return ResponseEntity.badRequest().body("Password is required.");
        }

        // Check for duplicate username
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            logger.warn("Username '{}' already exists", user.getUsername());
            return ResponseEntity.badRequest().body("Username '" + user.getUsername() + "' already exists.");
        }

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User '{}' created with ID {}", savedUser.getUsername(), savedUser.getId());

        // Create wallet for new user
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setWalletName(savedUser.getUsername() + "'s Wallet");
        wallet.setBalance(BigDecimal.ZERO); // initial balance
        Wallet savedWallet = walletRepository.save(wallet);
        logger.info("Wallet '{}' created for user '{}' with initial balance {}",
                savedWallet.getWalletName(), savedUser.getUsername(), savedWallet.getBalance());

        // Return user info along with wallet ID
        return ResponseEntity.ok(new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedWallet.getId(),
                savedWallet.getBalance()
        ));
    }

    // ================== GET ALL USERS ==================
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.info("Found {} users", users.size());
        return ResponseEntity.ok(users);
    }

    // ================== GET USER BY ID ==================
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID {}", id);
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            logger.info("User found: {}", userOpt.get().getUsername());
            return ResponseEntity.ok(userOpt.get());
        } else {
            logger.warn("User with ID {} not found", id);
            return ResponseEntity.status(404).body("User not found");
        }
    }

    // ================== Response DTO ==================
    public static record UserResponse(
            Long userId,
            String username,
            Long walletId,
            BigDecimal walletBalance
    ) {}
}
