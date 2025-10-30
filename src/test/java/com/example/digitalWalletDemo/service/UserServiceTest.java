package com.example.digitalWalletDemo.service;

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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private userResponseMapper userResponseMapper;
    @Mock private BindingResult bindingResult;

    @InjectMocks
    private UserService userService;

    private UserDTO userDTO;

    @BeforeEach
    void setup() {
        userDTO = new UserDTO();
        userDTO.setUsername("alice");
        userDTO.setPassword("password123");
        userDTO.setEmail("alice@example.com");
        userDTO.setDateOfBirth(LocalDate.of(2000, 1, 1));
    }

    @Test
    void testCreateUser_Success() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        User user = new User("alice", "password123");
        Wallet wallet = new Wallet("Default Wallet", BigDecimal.ZERO, user);
        user.getWallets().add(wallet);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO responseDTO =
                new UserResponseDTO(1L, "alice", 1L, "Default Wallet", BigDecimal.ZERO);
        when(userResponseMapper.toUserResponseDTO(any(), any())).thenReturn(responseDTO);

        Object result = userService.createUser(userDTO, bindingResult);
        assertTrue(result instanceof UserResponseDTO);
        assertEquals("alice", ((UserResponseDTO) result).getUsername());
    }

    @Test
    void testCreateUser_HasValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new FieldError("userDTO", "username", "Username is required")));

        Object result = userService.createUser(userDTO, bindingResult);
        assertTrue(result instanceof Map);
        assertEquals("Username is required", ((Map<?, ?>) result).get("username"));
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User()));

        Object result = userService.createUser(userDTO, bindingResult);
        assertEquals("Username already exists", result);
    }

    @Test
    void testGetUserById_Found() {
        User user = new User("bob", "pw123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Object result = userService.getUserById(1L);
        assertTrue(result instanceof User);
        assertEquals("bob", ((User) result).getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Object result = userService.getUserById(99L);
        assertEquals("User not found", result);
    }
}
