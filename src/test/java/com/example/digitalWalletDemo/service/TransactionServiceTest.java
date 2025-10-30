package com.example.digitalWalletDemo.service;

import com.example.digitalWalletDemo.dto.TransactionDTO;
import com.example.digitalWalletDemo.exception.WalletIdNotFoundException;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Wallet wallet;
    private Transaction transaction;

    @BeforeEach
    void setup() {
        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.valueOf(1000));

        transaction = new Transaction();
        transaction.setId(10L);
        transaction.setWallet(wallet);
        transaction.setAmount(BigDecimal.valueOf(500));
        transaction.setType(Transaction.Type.CREDIT);
        transaction.setDescription("Test credit");
        transaction.setTimestamp(LocalDateTime.now());
    }

    // ✅ 1. Get all transactions
    @Test
    void testGetAllTransactions_Success() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        List<TransactionDTO> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(500), result.get(0).getAmount());
        verify(transactionRepository, times(1)).findAll();
    }

    // ✅ 2. Get transactions by walletId (valid)
    @Test
    void testGetTransactionsByWallet_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenReturn(List.of(transaction));

        List<TransactionDTO> result = transactionService.getTransactionsByWallet(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CREDIT", result.get(0).getType());
        verify(walletRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).findByWalletId(1L);
    }

    // ❌ 3. Get transactions by walletId (wallet not found)
    @Test
    void testGetTransactionsByWallet_WalletNotFound() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WalletIdNotFoundException.class,
                () -> transactionService.getTransactionsByWallet(99L));

        verify(transactionRepository, never()).findByWalletId(anyLong());
    }

    // ✅ 4. Get transactions by walletId (empty list)
    @Test
    void testGetTransactionsByWallet_EmptyList() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenReturn(Collections.emptyList());

        List<TransactionDTO> result = transactionService.getTransactionsByWallet(1L);

        assertTrue(result.isEmpty());
    }

    // ✅ 5. Get user transactions (valid date & type)
    @Test
    void testGetUserTransactions_Success() {
        Long userId = 1L;
        String start = "01-01-2024";
        String end = "10-01-2024";
        String type = "CREDIT";

        when(transactionRepository.findUserTransactionsBetweenDates(
                anyLong(), any(), any(), any())).thenReturn(List.of(transaction));

        Object response = transactionService.getUserTransactions(userId, start, end, type);

        assertTrue(response instanceof List);
        List<?> result = (List<?>) response;
        assertEquals(1, result.size());
        verify(transactionRepository, times(1))
                .findUserTransactionsBetweenDates(eq(userId), any(), any(), any());
    }

    // ❌ 6. Invalid date format
    @Test
    void testGetUserTransactions_InvalidDateFormat() {
        Object response = transactionService.getUserTransactions(1L, "2024-01-01", "10-01-2024", null);

        assertTrue(response instanceof String);
        assertEquals("Invalid date format. Please use dd-MM-yyyy.", response);
    }

    // ❌ 7. Invalid transaction type
    @Test
    void testGetUserTransactions_InvalidType() {
        Object response = transactionService.getUserTransactions(1L, "01-01-2024", "10-01-2024", "INVALID");

        assertTrue(response instanceof String);
        assertEquals("Invalid transaction type. Use CREDIT, DEBIT, or TRANSFER.", response);
    }

    // ✅ 8. Get user transactions with null type
    @Test
    void testGetUserTransactions_NullType() {
        when(transactionRepository.findUserTransactionsBetweenDates(
                anyLong(), any(), any(), isNull()))
                .thenReturn(List.of(transaction));

        Object response = transactionService.getUserTransactions(1L, "01-01-2024", "10-01-2024", null);

        assertTrue(response instanceof List);
        List<?> result = (List<?>) response;
        assertEquals(1, result.size());
    }
}
