package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.exception.GlobalExceptionHandler;
import com.example.digitalWalletDemo.mapping.userResponseMapper;
import com.example.digitalWalletDemo.repository.UserRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import com.example.digitalWalletDemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(controllers = UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTypeMismatchTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock dependencies that UserController requires
    @MockBean
    private UserService userService; // ✅ Required for UserController constructor

    @MockBean
    private UserRepository userRepository; // Not required by controller but OK to keep if used indirectly

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private userResponseMapper userResponseMapper;

    @Test
    void testGetUserById_WhenIdIsString_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/s"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid ID format: s")));
    }

    @Test
    void testGetUserById_WhenIdIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid ID format: null")));
    }
}
