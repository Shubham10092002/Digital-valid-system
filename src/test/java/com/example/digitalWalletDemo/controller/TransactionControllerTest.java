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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionController transactionController;

    private Wallet wallet1;
    private Transaction tx1;

    @BeforeEach
    void setup() {
        wallet1 = new Wallet();
        wallet1.setId(1L);
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

    // ✅ Test for all transactions
    @Test
    void testGetAllTransactions() {
        when(transactionRepository.findAll()).thenReturn(List.of(tx1));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(100, dtos.get(0).getAmount().intValue());
        assertEquals("CREDIT", dtos.get(0).getType());
    }

    // ✅ Test when wallet exists
    @Test
    void testGetTransactionsByWallet_success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet1));
        when(transactionRepository.findByWalletId(1L)).thenReturn(List.of(tx1));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByWallet(1L);
        List<TransactionDTO> dtos = response.getBody();

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(100, dtos.get(0).getAmount().intValue());
        assertEquals("CREDIT", dtos.get(0).getType());
    }

    @Test
    void testGetTransactionsByWallet_walletNotFound() {
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        WalletIdNotFoundException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                WalletIdNotFoundException.class,
                () -> transactionController.getTransactionsByWallet(999L)
        );

        assertEquals("Wallet ID not found: 999", thrown.getMessage());
    }

}
