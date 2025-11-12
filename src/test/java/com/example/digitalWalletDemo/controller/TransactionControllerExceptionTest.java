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
class TransactionControllerExceptionTest {

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

    @Test
    void getTransactionsByWallet_walletExists_returnsTransactions() {
        when(transactionService.getTransactionsByWallet(1L)).thenReturn(List.of(dto));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(BigDecimal.valueOf(100), dtos.get(0).getAmount());
        assertEquals("CREDIT", dtos.get(0).getType());
    }

    @Test
    void getTransactionsByWallet_walletDoesNotExist_throwsException() {
        when(transactionService.getTransactionsByWallet(999L))
                .thenThrow(new WalletIdNotFoundException("Wallet ID not found: 999"));

        WalletIdNotFoundException ex = assertThrows(
                WalletIdNotFoundException.class,
                () -> transactionController.getTransactionsByWallet(999L)
        );

        assertEquals("Wallet ID not found: 999", ex.getMessage());
    }

    @Test
    void getTransactionsByWallet_emptyTransactionList_returnsEmpty() {
        when(transactionService.getTransactionsByWallet(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty(), "Expected empty list when wallet has no transactions");
    }
}
