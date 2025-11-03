package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.TransactionDTO;
import com.example.digitalWalletDemo.exception.WalletIdNotFoundException;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;


    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionDTO sampleTx;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        wallet.setId(1L);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setWallet(wallet);
        transaction.setAmount(BigDecimal.valueOf(500));
        transaction.setType(Transaction.Type.CREDIT);
        transaction.setDescription("Wallet credited");
        transaction.setTimestamp(LocalDateTime.now());

        sampleTx = new TransactionDTO(transaction);
    }

    // ✅ 1. Test get all transactions
    @Test
    void testGetAllTransactions_Success() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(sampleTx));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();
        List<TransactionDTO> result = response.getBody();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(500), result.get(0).getAmount());
    }

    // ✅ 2. Test get transactions by wallet ID (success)
    @Test
    void testGetTransactionsByWallet_Success() {
        when(transactionService.getTransactionsByWallet(1L)).thenReturn(List.of(sampleTx));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> result = response.getBody();

        assertNotNull(result);
        assertEquals(1, result.size());
        //assertEquals("CREDIT", result.get(0).getType());
        assertEquals("CREDIT", result.get(0).getType());

    }

    // ❌ 3. Wallet not found
    @Test
    void testGetTransactionsByWallet_WalletNotFound() {
        when(transactionService.getTransactionsByWallet(999L))
                .thenThrow(new WalletIdNotFoundException("Wallet ID not found: 999"));

        WalletIdNotFoundException exception = assertThrows(
                WalletIdNotFoundException.class,
                () -> transactionController.getTransactionsByWallet(999L)
        );

        assertEquals("Wallet ID not found: 999", exception.getMessage());
    }

    // ✅ 4. Empty transactions
    @Test
    void testGetTransactionsByWallet_EmptyList() {
        when(transactionService.getTransactionsByWallet(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> result = response.getBody();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ✅ 5. Handle null-safe transactions
    @Test
    void testGetTransactionsByWallet_NullFieldsHandled() {
        Wallet wallet = new Wallet();
        wallet.setId(1L);

        Transaction nullTx = new Transaction();
        nullTx.setWallet(wallet);
        nullTx.setAmount(null);
        nullTx.setType(null);
        nullTx.setDescription(null);
        nullTx.setTimestamp(null);

        TransactionDTO txDto = new TransactionDTO(nullTx);
        when(transactionService.getTransactionsByWallet(1L)).thenReturn(List.of(txDto));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> result = response.getBody();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.ZERO, result.get(0).getAmount());
        assertEquals("UNKNOWN", result.get(0).getType());
    }

    // ✅ 6. Simulate runtime exception
    @Test
    void testGetTransactionsByWallet_RuntimeException() {
        when(transactionService.getTransactionsByWallet(1L))
                .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionController.getTransactionsByWallet(1L));

        assertEquals("Database connection failed", exception.getMessage());
    }


}
