package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.UserDTO;
import com.example.digitalWalletDemo.dto.UserResponseDTO;
import com.example.digitalWalletDemo.mapping.userResponseMapper;
import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.UserRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private userResponseMapper userResponseMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    public UserController(UserRepository userRepository,
                          WalletRepository walletRepository,
                          userResponseMapper userResponseMapper) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.userResponseMapper = userResponseMapper;
    }

    //  Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }


//    //  Create a new user with a default wallet
//    @PostMapping
//    public ResponseEntity<?> createUser(@RequestBody User user) {
//        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("Username is required.");
//        }
//
//
//        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("Password is required.");
//        }
//
//
//
//        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
//            return ResponseEntity.badRequest().body("Username '" + user.getUsername() + "' already exists.");
//        }
//
////        // Save the user first
////        User savedUser = userRepository.save(user);
////
////        // Create default wallet
////        Wallet defaultWallet = new Wallet();
////        defaultWallet.setWalletName("Default Wallet");
////        defaultWallet.setBalance(BigDecimal.ZERO);
////        defaultWallet.setUser(savedUser);
////
////        // Save wallet
////        Wallet savedWallet = walletRepository.save(defaultWallet);
////
////        //  Update the user’s wallet list correctly
////        savedUser.getWallets().add(savedWallet);
////      //  userRepository.save(savedUser); // persist the relationship
////
////        // Prepare response
//////        Map<String, Object> response = new HashMap<>();
//////        response.put("userId", savedUser.getId());
//////        response.put("username", savedUser.getUsername());
//////        response.put("walletId", savedWallet.getId());
//////        response.put("walletName", savedWallet.getWalletName());
//////        response.put("walletBalance", savedWallet.getBalance());
////
////        UserResponseDTO response = new UserResponseDTO(
////                savedUser.getId(),
////                savedUser.getUsername(),
////                savedWallet.getId(),
////                savedWallet.getWalletName(),
////                savedWallet.getBalance()
////        );
////
////        return ResponseEntity.ok(response);
//
//        //  Save the user
//        User savedUser = userRepository.save(user);
//
//        //  Create and save default wallet
//        Wallet defaultWallet = new Wallet();
//        defaultWallet.setWalletName("Default Wallet");
//        defaultWallet.setBalance(BigDecimal.ZERO);
//        defaultWallet.setUser(savedUser);
//
//        Wallet savedWallet = walletRepository.save(defaultWallet);
//
//        //  Prevent NPE
//        if (savedUser.getWallets() == null) {
//            savedUser.setWallets(new ArrayList<>());
//        }
//
//        //  Add wallet to user's list
//        savedUser.getWallets().add(savedWallet);
//
//        //  Map User + Wallet → DTO using mapper
//        UserResponseDTO responseDTO = userResponseMapper.toUserResponseDTO(savedUser, savedWallet);
//        return ResponseEntity.ok(responseDTO);
//    }
//
//

//@PostMapping
//public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
//    logger.info("Received request to create user: {}", userDTO.getUsername());
//    if (result.hasErrors()) {
//
//        logger.warn("Validation failed for user '{}': {}", userDTO.getUsername(), result.getAllErrors());
//        Map<String, String> errors = new HashMap<>();
//        result.getFieldErrors().forEach(error ->
//                errors.put(error.getField(), error.getDefaultMessage())
//        );
//        return ResponseEntity.badRequest().body(errors);
//    }
//
//    // check if the useName has length between 3 to 20
//
//    if(userDTO.getUsername().length() <3 && userDTO.getEmail().length() > 20){
//        logger.warn("Invalid UserName username Should be between 3 and 20 characters");
//        throw new IllegalArgumentException("Invalid UserName username");
//    }
//
//    //  Check if username exists
//    if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
//        logger.warn("Username '{}' already exists", userDTO.getUsername());
//        return ResponseEntity.badRequest().body("Username already exists");
//    }
//
//    //  Map UserDTO → User entity
//    User user = new User();
//    user.setUsername(userDTO.getUsername());
//    user.setPassword(userDTO.getPassword());
//
//    //  Save the user
//    User savedUser = userRepository.save(user);
//    logger.debug("User '{}' saved with ID {}", savedUser.getUsername(), savedUser.getId());
//
//    //  Create default wallet
//    Wallet wallet = new Wallet();
//    wallet.setWalletName("Default Wallet");
//
//    wallet.setBalance(BigDecimal.ZERO);
//    wallet.setUser(savedUser);
//    Wallet savedWallet = walletRepository.save(wallet);
//   // Wallet defaultWallet = new Wallet();
//
//    if (savedUser.getWallets() == null) {
//           savedUser.setWallets(new ArrayList<>());
//      }
//
//    // Add Wallet to the user's List
//    savedUser.getWallets().add(savedWallet);
//
////        defaultWallet.setWalletName("Default Wallet");
////        defaultWallet.setBalance(BigDecimal.ZERO);
////        defaultWallet.setUser(savedUser);
////
////        Wallet savedWallet = walletRepository.save(defaultWallet);
////
////        //  Prevent NPE
////        if (savedUser.getWallets() == null) {
////            savedUser.setWallets(new ArrayList<>());
////        }
////
////        //  Add wallet to user's list
////        savedUser.getWallets().add(savedWallet);
////
////        //  Map User + Wallet → DTO using mapper
////        UserResponseDTO responseDTO = userResponseMapper.toUserResponseDTO(savedUser, savedWallet);
////        return ResponseEntity.ok(responseDTO);
//    logger.debug("Default wallet created for user ID {} with wallet ID {}", savedUser.getId(), savedWallet.getId());
//
//    //  Response mapping
//    UserResponseDTO response = userResponseMapper.toUserResponseDTO(savedUser, savedWallet);
//
//    logger.info("User '{}' successfully created", savedUser.getUsername());
//    return ResponseEntity.ok(response);
//}

//
//@PostMapping
//public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
//    logger.info("Received request to create user: {}", userDTO.getUsername());
//
//    if (result.hasErrors()) {
//        logger.warn("Validation failed for user '{}': {}", userDTO.getUsername(), result.getAllErrors());
//        Map<String, String> errors = new HashMap<>();
//        result.getFieldErrors().forEach(error ->
//                errors.put(error.getField(), error.getDefaultMessage())
//        );
//        return ResponseEntity.badRequest().body(errors);
//    }
//
//    // Validate username length
//    if (userDTO.getUsername().length() < 3 || userDTO.getUsername().length() > 20) {
//        logger.warn("Invalid username '{}' — must be 3–20 characters", userDTO.getUsername());
//        throw new IllegalArgumentException("Invalid username length");
//    }
//
//    // Check for duplicate username
//    if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
//        logger.warn("Username '{}' already exists", userDTO.getUsername());
//        return ResponseEntity.badRequest().body("Username already exists");
//    }
//
//    // Create user and wallet
//    User user = new User();
//    user.setUsername(userDTO.getUsername());
//    user.setPassword(userDTO.getPassword());
//    User savedUser = userRepository.save(user);
//
//    Wallet defaultWallet = new Wallet();
//    defaultWallet.setWalletName("Default Wallet");
//    defaultWallet.setBalance(BigDecimal.ZERO);
//    defaultWallet.setUser(savedUser);
//
//    Wallet savedWallet = walletRepository.save(defaultWallet);
//            //  Prevent NPE
//        if (savedUser.getWallets() == null) {
//            savedUser.setWallets(new ArrayList<>());
//       }
//
//    savedUser.getWallets().add(savedWallet);
//    //user.getWallets().add(defaultWallet);
//
//    // ✅ CascadeType.ALL ensures wallet is saved automatically
//    //User savedUser = userRepository.save(user);
//   // Wallet savedWallet = savedUser.getWallets().get(0);
//
//    logger.debug("User '{}' saved with wallet ID {}", savedUser.getUsername(), savedWallet.getId());
//
//    UserResponseDTO response = userResponseMapper.toUserResponseDTO(savedUser, savedWallet);
//    logger.info("User '{}' successfully created", savedUser.getUsername());
//
//    return ResponseEntity.ok(response);
//}
//



@PostMapping
public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
    logger.info("Received request to create user: {}", userDTO.getUsername());

    // 1️⃣ Handle validation errors
    if (result.hasErrors()) {
        logger.warn("Validation failed for user '{}': {}", userDTO.getUsername(), result.getAllErrors());
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    // 2️⃣ Validate username length (redundant but fine)
    if (userDTO.getUsername().length() < 3 || userDTO.getUsername().length() > 20) {
        logger.warn("Invalid username '{}' — must be 3–20 characters", userDTO.getUsername());
        return ResponseEntity.badRequest().body("Invalid username length");
    }

    // 3️⃣ Check duplicate username
    if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
        logger.warn("Username '{}' already exists", userDTO.getUsername());
        return ResponseEntity.badRequest().body("Username already exists");
    }

    // 4️⃣ Map DTO → Entity
    User user = new User();
    user.setUsername(userDTO.getUsername());
    user.setPassword(userDTO.getPassword());

    // 5️⃣ Create default wallet (attach to user before saving)
    Wallet defaultWallet = new Wallet();
    defaultWallet.setWalletName("Default Wallet");
    defaultWallet.setBalance(BigDecimal.ZERO);
    defaultWallet.setUser(user);

    // Add to user’s wallet list
    user.getWallets().add(defaultWallet);

    // 6️⃣ Save user (CascadeType.ALL ensures wallet is saved too)
    User savedUser = userRepository.save(user);

    // Retrieve the saved wallet (since ID is generated post-save)
    Wallet savedWallet = savedUser.getWallets().get(0);

    // 7️⃣ Log and map response
    logger.debug("User '{}' saved with wallet ID {}", savedUser.getUsername(), savedWallet.getId());
    UserResponseDTO response = userResponseMapper.toUserResponseDTO(savedUser, savedWallet);

    logger.info("User '{}' successfully created", savedUser.getUsername());
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
