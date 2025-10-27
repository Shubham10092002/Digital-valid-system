package com.example.digitalWalletDemo.controller;

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
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerEdgeTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionController transactionController;

    private Wallet wallet;
    private Transaction tx;

    @BeforeEach
    void setup() {
        wallet = new Wallet();
        wallet.setWalletName("Wallet1");
        wallet.setBalance(BigDecimal.valueOf(1000));

        tx = new Transaction();
        tx.setId(1L);
        tx.setWallet(wallet);
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setType(Transaction.Type.CREDIT);
        tx.setDescription("Deposit");
        tx.setTimestamp(LocalDateTime.now());
    }

    // ---------------------- EDGE CASES ----------------------

    @Test
    void getAllTransactions_emptyList() {
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty(), "Expected empty list when no transactions exist");
    }

    @Test
    void getTransactionsByWallet_walletIdZero() {
        when(walletRepository.findById(0L)).thenReturn(Optional.empty());

        WalletIdNotFoundException ex = assertThrows(
                WalletIdNotFoundException.class,
                () -> transactionController.getTransactionsByWallet(0L)
        );

        assertEquals("Wallet ID not found: 0", ex.getMessage());
    }

    @Test
    void getTransactionsByWallet_invalidWalletId() {
        when(walletRepository.findById(-1L)).thenReturn(Optional.empty());

        WalletIdNotFoundException ex = assertThrows(
                WalletIdNotFoundException.class,
                () -> transactionController.getTransactionsByWallet(-1L)
        );

        assertEquals("Wallet ID not found: -1", ex.getMessage());
    }

    @Test
    void getTransactionsByWallet_emptyTransactionList() {
        when(walletRepository.findById(999L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(999L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(999L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty(), "Expected empty list when wallet has no transactions");
    }

    @Test
    void getTransactionsByWallet_nullTransactionFields() {
        Transaction txNull = new Transaction();
        txNull.setWallet(wallet); // wallet must exist
        txNull.setAmount(null);
        txNull.setType(null);
        txNull.setDescription(null);
        txNull.setTimestamp(null);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenReturn(List.of(txNull));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(0, dtos.get(0).getAmount().intValue(), "Amount should default to 0 if null");
        assertEquals("UNKNOWN", dtos.get(0).getType(), "Type should default to UNKNOWN if null");
    }

    @Test
    void getTransactionsByWallet_repositoryThrowsException() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenThrow(new RuntimeException("DB connection failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> transactionController.getTransactionsByWallet(1L));

        assertEquals("DB connection failed", ex.getMessage());
    }
}
