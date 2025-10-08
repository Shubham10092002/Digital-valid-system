package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.data.WalletOperationResult;
import com.example.digitalWalletDemo.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

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

    private WalletOperationResult.Success successResult;
    private WalletOperationResult.Failure failureResult;

    @BeforeEach
    void setUp() {
        successResult = new WalletOperationResult.Success(1L, "Operation successful");
        failureResult = new WalletOperationResult.Failure("NOT_FOUND", "Wallet not found");
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

    // ---------------- BALANCE ----------------
    @Test
    void getBalance_success() throws Exception {
        WalletOperationResult.Balance balanceResult =
                new WalletOperationResult.Balance(1L, "100.00", "Balance retrieved successfully");

        when(walletService.getBalance(anyLong())).thenReturn(balanceResult);

        mockMvc.perform(get("/api/wallets/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("100.00"))
                .andExpect(jsonPath("$.message").value("Balance retrieved successfully"));
    }

    @Test
    void getBalance_walletNotFound() throws Exception {
        when(walletService.getBalance(anyLong())).thenReturn(failureResult);

        mockMvc.perform(get("/api/wallets/99/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("Wallet not found"));
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

    // ---------------- INVALID URL ----------------
    @Test
    void invalidUrl_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/wallets/invalid-url"))
                .andExpect(status().isNotFound());
    }

    @Test
    void wrongHttpMethod_shouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/wallets/1/credit")) // credit expects POST
                .andExpect(status().isMethodNotAllowed());
    }
}
