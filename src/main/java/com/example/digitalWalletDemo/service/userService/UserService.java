package com.example.digitalWalletDemo.service.userService;

import com.example.digitalWalletDemo.dto.userdto.UserDTO;
import com.example.digitalWalletDemo.dto.userdto.UserFullResponseDTO;
import com.example.digitalWalletDemo.dto.userdto.UserResponseDTO;
import com.example.digitalWalletDemo.mapping.userResponseMapper;
import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import com.example.digitalWalletDemo.repository.userRepository.UserRepository;
import com.example.digitalWalletDemo.repository.walletRepository.WalletRepository;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final userResponseMapper userResponseMapper;

    public UserService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       userResponseMapper userResponseMapper) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.userResponseMapper = userResponseMapper;
    }

    /**
     * Get all users
     */
    public List<UserFullResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        // Convert each User entity to UserFullResponseDTO
        return users.stream()
                .map(UserFullResponseDTO::new)
                .toList();
    }


    /**
     * Create new user and attach a default wallet
     */
    public Object createUser(@Valid UserDTO userDTO, BindingResult result) {
        logger.info("Received request to create user: {}", userDTO.getUsername());

        // 1️⃣ Handle validation errors
        if (result.hasErrors()) {
            logger.warn("Validation failed for user '{}': {}", userDTO.getUsername(), result.getAllErrors());
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return errors;
        }

        // 2️⃣ Validate username length
        if (userDTO.getUsername().length() < 3 || userDTO.getUsername().length() > 20) {
            logger.warn("Invalid username '{}' — must be 3–20 characters", userDTO.getUsername());
            return "Invalid username length";
        }

        // 3️⃣ Check duplicate username
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            logger.warn("Username '{}' already exists", userDTO.getUsername());
            return "Username already exists";
        }

        // 4️⃣ Map DTO → Entity
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());

        // 5️⃣ Create default wallet (attach before saving)
        Wallet defaultWallet = new Wallet();
        defaultWallet.setWalletName("Default Wallet");
        defaultWallet.setBalance(BigDecimal.ZERO);
        defaultWallet.setUser(user);

        user.getWallets().add(defaultWallet);

        // 6️⃣ Save user (CascadeType.ALL saves wallet too)
        User savedUser = userRepository.save(user);

        // Retrieve the saved wallet (ID generated post-save)
        Wallet savedWallet = savedUser.getWallets().get(0);

        // 7️⃣ Map response
        logger.debug("User '{}' saved with wallet ID {}", savedUser.getUsername(), savedWallet.getId());
        UserResponseDTO response = userResponseMapper.toUserResponseDTO(savedUser, savedWallet);

        logger.info("User '{}' successfully created", savedUser.getUsername());
        return response;
    }

    /**
     * Get a user by ID
     */
    public Object getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            logger.warn("User with ID {} not found", id);
            return "User not found";
        }
        return new UserFullResponseDTO(userOpt.get());
    }
}
