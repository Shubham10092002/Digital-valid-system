package com.example.digitalWalletDemo.service;

import com.example.digitalWalletDemo.config.WalletConfig;
import com.example.digitalWalletDemo.dto.TransactionDTO;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionServicePaginationTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletConfig walletConfig;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTransactionHistory_WithValidFilters() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(BigDecimal.valueOf(200));
        transaction.setType(Transaction.Type.CREDIT);
        transaction.setTimestamp(LocalDateTime.now());

        Page<Transaction> mockPage = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findTransactionsWithFilters(
                anyLong(), anyLong(), any(), any(), any(), any(Pageable.class))
        ).thenReturn(mockPage);

        // Act
        Page<TransactionDTO> result = transactionService.getTransactionHistory(
                1L, 2L, "CREDIT", "01-10-2025", "31-10-2025", 0, 5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CREDIT", result.getContent().get(0).getType()); // ✅ fixed

        verify(transactionRepository, times(1)).findTransactionsWithFilters(
                eq(2L), eq(1L), eq(Transaction.Type.CREDIT), any(), any(), any(Pageable.class));
    }


    @Test
    void testGetTransactionHistory_InvalidTransactionType() {
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionHistory(1L, 2L, "INVALID", "01-10-2025", "31-10-2025", 0, 5)
        );

        assertTrue(ex.getMessage().contains("Invalid transaction type"));
        verify(transactionRepository, never()).findTransactionsWithFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGetTransactionHistory_InvalidDateFormat() {
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionHistory(1L, 2L, "CREDIT", "invalid-date", "31-10-2025", 0, 5)
        );

        assertTrue(ex.getMessage().contains("Invalid date format"));
        verify(transactionRepository, never()).findTransactionsWithFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGetTransactionHistory_EmptyResult() {
        // Arrange
        when(transactionRepository.findTransactionsWithFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act
        Page<TransactionDTO> result = transactionService.getTransactionHistory(
                1L, 2L, null, "01-10-2025", "31-10-2025", 0, 5);

        // Assert
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findTransactionsWithFilters(
                eq(2L), eq(1L), isNull(), any(), any(), any(Pageable.class));
    }
}
