package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.UserDTO;
import com.example.digitalWalletDemo.dto.UserResponseDTO;
import com.example.digitalWalletDemo.mapping.userResponseMapper;
import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.UserRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private userResponseMapper userResponseMapper;
    @Mock private BindingResult bindingResult;

    @InjectMocks
    private UserController userController;

    private UserDTO validUserDTO;

    @BeforeEach
    void setup() {
        validUserDTO = new UserDTO();
        validUserDTO.setUsername("alice");
        validUserDTO.setPassword("password123");
        validUserDTO.setEmail("alice@example.com");
        validUserDTO.setDateOfBirth(LocalDate.of(2000, 1, 1));
    }
    @Test
    void testCreateUser_ValidInput() {
        // Given
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(validUserDTO.getUsername());
        savedUser.setPassword(validUserDTO.getPassword());

        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setWalletName("Default Wallet");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(savedUser);
        savedUser.getWallets().add(wallet);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername(validUserDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userResponseMapper.toUserResponseDTO(any(User.class), any(Wallet.class)))
                .thenReturn(new UserResponseDTO(1L, "alice", 1L, "Default Wallet", BigDecimal.ZERO));

        // When
        ResponseEntity<?> response = userController.createUser(validUserDTO, bindingResult);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
        verify(userResponseMapper, times(1)).toUserResponseDTO(any(User.class), any(Wallet.class));
        verify(walletRepository, never()).save(any()); // Wallet saved via cascade

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody() instanceof UserResponseDTO);

        UserResponseDTO responseBody = (UserResponseDTO) response.getBody();
        assertEquals("alice", responseBody.getUsername());
        assertEquals("Default Wallet", responseBody.getWalletName());
    }


    // ✅ 2. Missing username validation error
    @Test
    void testCreateUser_MissingUsername() {
        validUserDTO.setUsername(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("userDTO", "username", "Username is required"))
        );

        ResponseEntity<?> response = userController.createUser(validUserDTO, bindingResult);
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertTrue(errors.containsKey("username"));
        assertEquals("Username is required", errors.get("username"));
    }

    // ✅ 3. Missing password validation error
    @Test
    void testCreateUser_MissingPassword() {
        validUserDTO.setPassword(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("userDTO", "password", "Password is required"))
        );

        ResponseEntity<?> response = userController.createUser(validUserDTO, bindingResult);
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertTrue(errors.containsKey("password"));
        assertEquals("Password is required", errors.get("password"));
    }

    // ✅ 4. Duplicate username
    @Test
    void testCreateUser_DuplicateUsername() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User()));

        ResponseEntity<?> response = userController.createUser(validUserDTO, bindingResult);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username already exists", response.getBody());
        verify(userRepository, never()).save(any());
    }

    // ✅ 5. Invalid age (under 18)
    @Test
    void testCreateUser_InvalidAge() {
        validUserDTO.setDateOfBirth(LocalDate.now().minusYears(10)); // age 10

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("userDTO", "dateOfBirth", "User must be at least 18 years old"))
        );

        ResponseEntity<?> response = userController.createUser(validUserDTO, bindingResult);
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertEquals("User must be at least 18 years old", errors.get("dateOfBirth"));
    }

    // ✅ 6. Get user by ID - Found
    @Test
    void testGetUserByIdFound() {
        User user = new User("shubham", "pass123");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUserById(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof User);
        assertEquals("shubham", ((User) response.getBody()).getUsername());
    }

    // ✅ 7. Get user by ID - Not Found
    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = userController.getUserById(99L);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }
}
