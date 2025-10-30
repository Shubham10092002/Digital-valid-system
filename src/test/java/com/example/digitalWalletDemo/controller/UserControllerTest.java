package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.UserDTO;
import com.example.digitalWalletDemo.dto.UserResponseDTO;
import com.example.digitalWalletDemo.service.UserService;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
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
    void testCreateUser_Success() {
        UserResponseDTO mockResponse =
                new UserResponseDTO(1L, "alice", 10L, "Default Wallet", BigDecimal.ZERO);

        when(userService.createUser(validUserDTO, bindingResult)).thenReturn(mockResponse);

        ResponseEntity<?> response = userController.createUser(validUserDTO, bindingResult);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof UserResponseDTO);
        assertEquals("alice", ((UserResponseDTO) response.getBody()).getUsername());
    }

    @Test
    void testCreateUser_ValidationError() {
        Map<String, String> mockErrors = Map.of("username", "Username is required");

        when(userService.createUser(validUserDTO, bindingResult)).thenReturn(mockErrors);

        ResponseEntity<?> response = userController.createUser(validUserDTO, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Map);
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        when(userService.createUser(validUserDTO, bindingResult)).thenReturn("Username already exists");

        ResponseEntity<?> response = userController.createUser(validUserDTO, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username already exists", response.getBody());
    }

    @Test
    void testGetUserById_Found() {
        UserResponseDTO responseDTO =
                new UserResponseDTO(1L, "alice", 1L, "Default Wallet", BigDecimal.ZERO);

        when(userService.getUserById(1L)).thenReturn(responseDTO);

        ResponseEntity<?> response = userController.getUserById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userService.getUserById(99L)).thenReturn("User not found");

        ResponseEntity<?> response = userController.getUserById(99L);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }
}
