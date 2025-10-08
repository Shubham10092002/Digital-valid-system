package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.UserRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository; // Needed for createUser

    @InjectMocks
    private UserController userController;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setup() {
        user1 = new User("shubham", "pass123");
        user2 = new User("alex", "pass456");
        user3 = new User("shubham", "pass12");
        user1.setId(1L);
        user2.setId(2L);
        user3.setId(3L);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        ResponseEntity<List<User>> response = userController.getAllUsers();
        List<User> users = response.getBody();

        assertEquals(3, users.size());
        assertEquals("shubham", users.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testCreateUser() {
        User user = new User("alice", "password123");

        // Mock saving user and wallet
        User savedUser = new User("alice", "password123");
        savedUser.setId(1L);
        Wallet savedWallet = new Wallet();
        savedWallet.setId(10L);
        savedWallet.setBalance(BigDecimal.ZERO);
        savedWallet.setUser(savedUser);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        // Call controller
        ResponseEntity<?> response = userController.createUser(user);

        // Cast the body to UserResponse
        assertTrue(response.getBody() instanceof UserController.UserResponse);
        UserController.UserResponse userResponse = (UserController.UserResponse) response.getBody();

        assertNotNull(userResponse);
        assertEquals("alice", userResponse.username());
        assertEquals(1L, userResponse.userId());
        assertEquals(10L, userResponse.walletId());
        assertEquals(BigDecimal.ZERO, userResponse.walletBalance());

        verify(userRepository, times(1)).save(any(User.class));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void testCreateUserWithMissingUsername() {
        User user = new User(null, "password123"); // Missing username

        ResponseEntity<?> response = userController.createUser(user);

        // Assert that the status is 400 Bad Request
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username is required.", response.getBody());

        // Ensure repository save is never called
        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testCreateUserWithMissingPassword() {
        User user = new User("alice", null); // Missing password

        ResponseEntity<?> response = userController.createUser(user);

        // Assert that the status is 400 Bad Request
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Password is required.", response.getBody());

        // Ensure repository save is never called
        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }


    @Test
    void testCreateUserWithDuplicateUsername() {
        // Simulate an existing user in the repository
        User existingUser = new User("alice", "pass123");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        // Attempt to create a new user with the same username
        User newUser = new User("alice", "newPassword");
        ResponseEntity<?> response = userController.createUser(newUser);

        // Assert that the response status is 400 Bad Request
        assertEquals(400, response.getStatusCodeValue());

        // Assert that the response body contains the duplicate username message
        assertEquals("Username 'alice' already exists.", response.getBody());

        // Verify that neither userRepository.save() nor walletRepository.save() is called
        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testGetUserByIdFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        ResponseEntity<?> response = userController.getUserById(1L);
        assertTrue(response.getBody() instanceof User);
        User resultUser = (User) response.getBody();

        assertEquals("shubham", resultUser.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }


    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserById(99L);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
        verify(userRepository, times(1)).findById(99L);
    }

}
