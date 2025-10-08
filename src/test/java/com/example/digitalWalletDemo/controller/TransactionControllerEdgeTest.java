package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.TransactionDTO;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class TransactionControllerEdgeTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionController transactionController;

    private Wallet wallet1;
    private Transaction tx1;

    @BeforeEach
    void setup() {
        wallet1 = new Wallet();
        wallet1.setWalletName("Wallet1");
        wallet1.setBalance(BigDecimal.valueOf(1000));

        tx1 = new Transaction();
        tx1.setId(1L);
        tx1.setWallet(wallet1);
        tx1.setAmount(BigDecimal.valueOf(100));
        tx1.setType(Transaction.Type.CREDIT);
        tx1.setDescription("Deposit");
        tx1.setTimestamp(LocalDateTime.now());
    }

    // ---------------------- EDGE CASES ----------------------

    @Test
    void getAllTransactions_emptyList() {
        // repository returns empty list
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();
        List<TransactionDTO> dtos = response.getBody();

        assertEquals(0, dtos.size(), "Expected empty list when no transactions exist");
    }

    @Test
    void getTransactionsByWallet_emptyList() {
        // repository returns empty list for a wallet
        when(transactionRepository.findByWalletId(999L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(999L);
        List<TransactionDTO> dtos = response.getBody();

        assertEquals(0, dtos.size(), "Expected empty list when wallet has no transactions");
    }

    @Test
    void getTransactionsByWallet_invalidWalletId() {
        // repository might return empty list for invalid wallet ID
        when(transactionRepository.findByWalletId(-1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(-1L);
        List<TransactionDTO> dtos = response.getBody();

        assertTrue(dtos.isEmpty(), "Expected empty list for negative wallet ID");
    }

    @Test
    void getAllTransactions_nullTransactionFields() {
        Transaction txNull = new Transaction();
        txNull.setWallet(null);
        txNull.setAmount(null);
        txNull.setType(null);
        txNull.setDescription(null);
        txNull.setTimestamp(null);

        when(transactionRepository.findAll()).thenReturn(List.of(txNull));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();
        List<TransactionDTO> dtos = response.getBody();

        assertEquals(1, dtos.size());
        assertEquals(0, dtos.get(0).getAmount().intValue(), "Amount should default to 0 if null");
        assertEquals("UNKNOWN", dtos.get(0).getType(), "Type should default to UNKNOWN if null");
    }

    @Test
    void getTransactionsByWallet_nullTransactionFields() {
        Transaction txNull = new Transaction();
        txNull.setWallet(null);
        txNull.setAmount(null);
        txNull.setType(null);
        txNull.setDescription(null);
        txNull.setTimestamp(null);

        when(transactionRepository.findByWalletId(1L)).thenReturn(List.of(txNull));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertEquals(1, dtos.size());
        assertEquals(0, dtos.get(0).getAmount().intValue());
        assertEquals("UNKNOWN", dtos.get(0).getType());
    }

    @Test
    void getAllTransactions_repositoryThrowsException() {
        when(transactionRepository.findAll()).thenThrow(new RuntimeException("DB connection failed"));

        try {
            transactionController.getAllTransactions();
        } catch (RuntimeException e) {
            assertEquals("DB connection failed", e.getMessage());
        }
    }

    @Test
    void getTransactionsByWallet_repositoryThrowsException() {
        when(transactionRepository.findByWalletId(1L)).thenThrow(new RuntimeException("DB connection failed"));

        try {
            transactionController.getTransactionsByWallet(1L);
        } catch (RuntimeException e) {
            assertEquals("DB connection failed", e.getMessage());
        }
    }

    @Test
    void getTransactionsByWallet_walletIdZero() {
        // Edge case: walletId = 0
        when(transactionRepository.findByWalletId(0L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(0L);
        List<TransactionDTO> dtos = response.getBody();

        assertTrue(dtos.isEmpty(), "Expected empty list for walletId 0");
    }
}
