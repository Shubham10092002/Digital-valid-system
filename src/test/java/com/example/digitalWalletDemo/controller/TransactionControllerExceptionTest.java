package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.TransactionDTO;
import com.example.digitalWalletDemo.exception.WalletIdNotFoundException;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerExceptionTest {

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

    @Test
    void getTransactionsByWallet_walletExists_returnsTransactions() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenReturn(List.of(tx));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(100, dtos.get(0).getAmount().intValue());
        assertEquals("CREDIT", dtos.get(0).getType());
    }

    @Test
    void getTransactionsByWallet_walletDoesNotExist_throwsException() {
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        WalletIdNotFoundException ex = assertThrows(
                WalletIdNotFoundException.class,
                () -> transactionController.getTransactionsByWallet(999L)
        );

        assertEquals("Wallet ID not found: 999", ex.getMessage());
    }

    @Test
    void getTransactionsByWallet_emptyTransactionList_returnsEmpty() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty(), "Expected empty list when wallet has no transactions");
    }
}
