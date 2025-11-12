package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.controller.transactionController.TransactionController;
import com.example.digitalWalletDemo.dto.transactiondto.TransactionDTO;
import com.example.digitalWalletDemo.exception.WalletIdNotFoundException;
import com.example.digitalWalletDemo.model.transactionModel.Transaction;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import com.example.digitalWalletDemo.service.transactionService.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerEdgeTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private Wallet wallet;
    private Transaction tx;
    private TransactionDTO dto;

    @BeforeEach
    void setup() {
        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setWalletName("Wallet1");
        wallet.setBalance(BigDecimal.valueOf(1000));

        tx = new Transaction();
        tx.setId(1L);
        tx.setWallet(wallet);
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setType(Transaction.Type.CREDIT);
        tx.setDescription("Deposit");
        tx.setTimestamp(LocalDateTime.now());

        dto = new TransactionDTO(tx);
    }

    // ✅ 1. Empty transactions list
    @Test
    void getAllTransactions_emptyList() {
        when(transactionService.getAllTransactions()).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty(), "Expected empty list when no transactions exist");
    }

    // ✅ 2. Wallet ID = 0
    @Test
    void getTransactionsByWallet_walletIdZero() {
        when(transactionService.getTransactionsByWallet(0L))
                .thenThrow(new WalletIdNotFoundException("Wallet ID not found: 0"));

        WalletIdNotFoundException ex = assertThrows(
                WalletIdNotFoundException.class,
                () -> transactionController.getTransactionsByWallet(0L)
        );

        assertEquals("Wallet ID not found: 0", ex.getMessage());
    }

    // ✅ 3. Invalid wallet ID (negative)
    @Test
    void getTransactionsByWallet_invalidWalletId() {
        when(transactionService.getTransactionsByWallet(-1L))
                .thenThrow(new WalletIdNotFoundException("Wallet ID not found: -1"));

        WalletIdNotFoundException ex = assertThrows(
                WalletIdNotFoundException.class,
                () -> transactionController.getTransactionsByWallet(-1L)
        );

        assertEquals("Wallet ID not found: -1", ex.getMessage());
    }

    // ✅ 4. Empty transaction list for valid wallet
    @Test
    void getTransactionsByWallet_emptyTransactionList() {
        when(transactionService.getTransactionsByWallet(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty(), "Expected empty list when wallet has no transactions");
    }

    // ✅ 5. Null fields handled gracefully
    @Test
    void getTransactionsByWallet_nullTransactionFields() {
        Transaction txNull = new Transaction();
        txNull.setWallet(wallet);
        txNull.setAmount(null);
        txNull.setType(null);
        txNull.setDescription(null);
        txNull.setTimestamp(null);

        TransactionDTO dtoNull = new TransactionDTO(txNull);
        when(transactionService.getTransactionsByWallet(1L)).thenReturn(List.of(dtoNull));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(BigDecimal.ZERO, dtos.get(0).getAmount(), "Amount should default to 0 if null");
        assertEquals("UNKNOWN", dtos.get(0).getType(), "Type should default to UNKNOWN if null");
    }

    // ✅ 6. Repository/service throws runtime exception
    @Test
    void getTransactionsByWallet_repositoryThrowsException() {
        when(transactionService.getTransactionsByWallet(1L))
                .thenThrow(new RuntimeException("DB connection failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionController.getTransactionsByWallet(1L));

        assertEquals("DB connection failed", ex.getMessage());
    }
}
