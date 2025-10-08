package com.example.digitalWalletDemo.service;

import com.example.digitalWalletDemo.data.WalletOperationResult;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceTest.class);

    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        walletService = new WalletService(walletRepository, transactionRepository);
        log.info("===== WalletServiceTest Setup Completed =====");
    }

    @Test
    void testCreditSuccess() {
        log.info("Running testCreditSuccess...");
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.credit(1L, BigDecimal.valueOf(500), "Deposit");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Success.class, result);
        assertEquals("1500.00", ((WalletOperationResult.Success) result).message().split(": ")[1]);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void testCreditWithNegativeAmount() {
        log.info("Running testCreditWithNegativeAmount...");
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.credit(1L, BigDecimal.valueOf(-200), "Invalid Deposit");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("INVALID_AMOUNT", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testCreditExceedingLimit() {
        log.info("Running testCreditExceedingLimit...");
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.credit(1L, new BigDecimal("200000"), "Huge Deposit");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("LIMIT_EXCEEDED", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testCreditToNonExistentWallet() {
        log.info("Running testCreditToNonExistentWallet...");
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        WalletOperationResult result = walletService.credit(99L, BigDecimal.valueOf(500), "Deposit");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("WALLET_NOT_FOUND", ((WalletOperationResult.Failure) result).errorCode());
    }

    @Test
    void testDebitSuccess() {
        log.info("Running testDebitSuccess...");
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.debit(1L, BigDecimal.valueOf(300), "Purchase");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Success.class, result);
        assertEquals("700.00", ((WalletOperationResult.Success) result).message().split(": ")[1]);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testDebitInsufficientFunds() {
        log.info("Running testDebitInsufficientFunds...");
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(100), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.debit(1L, BigDecimal.valueOf(200), "Withdraw");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("INSUFFICIENT_FUNDS", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testDebitWithNegativeAmount() {
        log.info("Running testDebitWithNegativeAmount...");
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(500), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.debit(1L, BigDecimal.valueOf(-100), "Invalid Withdrawal");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("INVALID_AMOUNT", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testDebitFromNonExistentWallet() {
        log.info("Running testDebitFromNonExistentWallet...");
        when(walletRepository.findById(77L)).thenReturn(Optional.empty());

        WalletOperationResult result = walletService.debit(77L, BigDecimal.valueOf(300), "Purchase");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("WALLET_NOT_FOUND", ((WalletOperationResult.Failure) result).errorCode());
    }

    @Test
    void testGetBalanceSuccess() {
        log.info("Running testGetBalanceSuccess...");
        Wallet wallet = new Wallet("My Wallet", BigDecimal.valueOf(1200), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.getBalance(1L);
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Balance.class, result);
        WalletOperationResult.Balance balanceResult = (WalletOperationResult.Balance) result;
        assertEquals("1200.00", balanceResult.balance());
        assertEquals(1L, balanceResult.walletId());
    }

    @Test
    void testGetBalanceWalletNotFound() {
        log.info("Running testGetBalanceWalletNotFound...");
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        WalletOperationResult result = walletService.getBalance(99L);
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("WALLET_NOT_FOUND", ((WalletOperationResult.Failure) result).errorCode());
    }

    @Test
    void testNullAmount() {
        log.info("Running testNullAmount...");
        Wallet wallet = new Wallet("Wallet A", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.credit(1L, null, "Null Deposit");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("INVALID_AMOUNT", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testExceptionHandling() {
        log.info("Running testExceptionHandling...");
        when(walletRepository.findById(any())).thenThrow(new RuntimeException("DB connection failed"));

        WalletOperationResult result = walletService.credit(1L, BigDecimal.valueOf(100), "Deposit");
        log.info("Result: {}", result);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("UNKNOWN_ERROR", ((WalletOperationResult.Failure) result).errorCode());
        assertTrue(((WalletOperationResult.Failure) result).reason().contains("DB connection failed"));
    }
}
