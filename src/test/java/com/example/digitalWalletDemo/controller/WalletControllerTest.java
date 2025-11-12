package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.controller.walletController.WalletController;
import com.example.digitalWalletDemo.data.WalletOperationResult;
import com.example.digitalWalletDemo.dto.walletdto.CreateWalletDTO;
import com.example.digitalWalletDemo.dto.walletdto.WalletResponseDTO;
import com.example.digitalWalletDemo.exception.UserNotFoundException;
import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import com.example.digitalWalletDemo.repository.userRepository.UserRepository;
import com.example.digitalWalletDemo.repository.walletRepository.WalletRepository;
import com.example.digitalWalletDemo.service.walletService.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private UserRepository userRepository;

    private WalletOperationResult.Success successResult;
    private WalletOperationResult.Failure failureResult;

    @BeforeEach
    void setUp() {
        successResult = new WalletOperationResult.Success("Operation successful");
        failureResult = new WalletOperationResult.Failure("NOT_FOUND", "Wallet not found");
    }

    // ---------------- CREATE WALLET ----------------
//    @Test
//    void createWalletForUser_success() throws Exception {
//        User user = new User();
//        user.setId(10L);
//
//        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
//        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
//
//        String walletJson = """
//            {
//                "walletName": "My Savings",
//                "initialBalance": 5000
//            }
//            """;
//
//        mockMvc.perform(post("/api/wallets/user/10/create-wallet")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(walletJson))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.walletName").value("My Savings"))
//                .andExpect(jsonPath("$.walletBalance").value(5000));
//
//    }


    @Test
    void createWalletForUser_success() throws Exception {
        CreateWalletDTO dto = new CreateWalletDTO();
        dto.setWalletName("My Savings");
        dto.setInitialBalance(new BigDecimal("5000"));

        WalletResponseDTO responseDTO = new WalletResponseDTO();
        responseDTO.setWalletId(1L);
        responseDTO.setWalletName("My Savings");
        responseDTO.setWalletBalance(new BigDecimal("5000"));

        when(walletService.createWalletForUser(eq(10L), any(CreateWalletDTO.class)))
                .thenReturn(responseDTO);

        String walletJson = """
        {
            "walletName": "My Savings",
            "initialBalance": 5000
        }
        """;

        mockMvc.perform(post("/api/wallets/user/10/create-wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(walletJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.walletName").value("My Savings"))
                .andExpect(jsonPath("$.walletBalance").value(5000));
    }




//    @Test
//    void createWalletForUser_userNotFound_shouldReturnError() throws Exception {
//        when(userRepository.findById(99L)).thenReturn(Optional.empty());
//
//        String walletJson = """
//            {
//                "walletName": "Emergency Fund",
//                "initialBalance": 1000
//            }
//            """;
//
//        mockMvc.perform(post("/api/wallets/user/99/create-wallet")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(walletJson))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
//                .andExpect(jsonPath("$.reason").value("User not found with ID: 99"));
//
//    }


    @Test
    void createWalletForUser_userNotFound_shouldReturnError() throws Exception {
        when(walletService.createWalletForUser(eq(99L), any(CreateWalletDTO.class)))
                .thenThrow(new UserNotFoundException(99L));

        String walletJson = """
        {
            "walletName": "Emergency Fund",
            "initialBalance": 1000
        }
        """;

        mockMvc.perform(post("/api/wallets/user/99/create-wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(walletJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("User not found with ID: 99"));
    }



    // ---------------- GET WALLET BALANCE ----------------
    @Test
    void getWalletById_success() throws Exception {
        WalletOperationResult.Balance balanceResult =
                new WalletOperationResult.Balance(1L, "100.00");

        when(walletService.getBalance(1L)).thenReturn(balanceResult);

        mockMvc.perform(get("/api/wallets/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(1))
                .andExpect(jsonPath("$.balance").value("100.00"))
                .andExpect(jsonPath("$.message").value("Balance retrieved successfully"));
    }

    @Test
    void getWalletById_notFound() throws Exception {
        when(walletService.getBalance(99L)).thenReturn(failureResult);

        mockMvc.perform(get("/api/wallets/99/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("Wallet not found"));
    }

    // ---------------- CREDIT ----------------
    @Test
    void creditWallet_success() throws Exception {
        when(walletService.credit(anyLong(), any(BigDecimal.class), anyString()))
                .thenReturn(successResult);

        mockMvc.perform(post("/api/wallets/1/credit")
                        .param("amount", "100.00")
                        .param("description", "Test credit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Operation successful"));
    }

    @Test
    void creditWallet_negativeAmount_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallets/1/credit")
                        .param("amount", "-100.00")
                        .param("description", "Test negative credit"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.reason").value("Amount must be positive"));
    }

    @Test
    void creditWallet_walletNotFound() throws Exception {
        when(walletService.credit(anyLong(), any(BigDecimal.class), anyString()))
                .thenReturn(failureResult);

        mockMvc.perform(post("/api/wallets/99/credit")
                        .param("amount", "100.00")
                        .param("description", "Test credit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("Wallet not found"));
    }

    // ---------------- DEBIT ----------------
    @Test
    void debitWallet_success() throws Exception {
        when(walletService.debit(anyLong(), any(BigDecimal.class), anyString()))
                .thenReturn(successResult);

        mockMvc.perform(post("/api/wallets/1/debit")
                        .param("amount", "50.00")
                        .param("description", "Test debit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Operation successful"));
    }

    @Test
    void debitWallet_negativeAmount_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallets/1/debit")
                        .param("amount", "-50.00")
                        .param("description", "Test negative debit"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.reason").value("Amount must be positive"));
    }

    @Test
    void debitWallet_amountGreaterThanBalance_shouldReturnInsufficientFunds() throws Exception {
        WalletOperationResult.Failure insufficientFunds =
                new WalletOperationResult.Failure("INSUFFICIENT_FUNDS", "Insufficient balance");

        when(walletService.debit(anyLong(), any(BigDecimal.class), anyString()))
                .thenReturn(insufficientFunds);

        mockMvc.perform(post("/api/wallets/1/debit")
                        .param("amount", "500.00")
                        .param("description", "Test debit greater than balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.reason").value("Insufficient balance"));
    }

    // ---------------- TRANSFER ----------------
    @Test
    void transfer_success() throws Exception {
        when(walletService.debit(eq(1L), any(BigDecimal.class), anyString()))
                .thenReturn(successResult);
        when(walletService.credit(eq(2L), any(BigDecimal.class), anyString()))
                .thenReturn(successResult);

        mockMvc.perform(post("/api/wallets/transfer")
                        .param("fromWalletId", "1")
                        .param("toWalletId", "2")
                        .param("amount", "50.00")
                        .param("description", "Test transfer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Amount transferred successfully"));
    }

    @Test
    void transfer_sameWallet_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallets/transfer")
                        .param("fromWalletId", "1")
                        .param("toWalletId", "1")
                        .param("amount", "50.00")
                        .param("description", "Test transfer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TRANSFER"))
                .andExpect(jsonPath("$.reason").value("Cannot transfer to the same wallet"));
    }

    @Test
    void transfer_negativeAmount_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/wallets/transfer")
                        .param("fromWalletId", "1")
                        .param("toWalletId", "2")
                        .param("amount", "-50.00")
                        .param("description", "Test transfer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.reason").value("Amount must be positive"));
    }

    @Test
    void transfer_insufficientFunds_shouldReturnBadRequest() throws Exception {
        WalletOperationResult.Failure insufficientFunds =
                new WalletOperationResult.Failure("INSUFFICIENT_FUNDS", "Insufficient balance");

        when(walletService.debit(eq(1L), any(BigDecimal.class), anyString()))
                .thenReturn(insufficientFunds);

        mockMvc.perform(post("/api/wallets/transfer")
                        .param("fromWalletId", "1")
                        .param("toWalletId", "2")
                        .param("amount", "1000.00")
                        .param("description", "Test transfer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.reason").value("Insufficient balance"));
    }
}
