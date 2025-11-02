package com.example.digitalWalletDemo.service;

import com.example.digitalWalletDemo.config.WalletConfig;
import com.example.digitalWalletDemo.data.WalletOperationResult;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Enterprise-grade unit test for WalletService (with TransactionService integration mock).
 * Covers success, edge, negative, and exception scenarios including daily/monthly limits.
 */
class WalletServiceTest {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceTest.class);

    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private WalletConfig walletConfig;
    private TransactionService transactionService; // ✅ new mock
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        log.info("===== WalletServiceTest Setup Started =====");

        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        walletConfig = mock(WalletConfig.class);
        transactionService = mock(TransactionService.class);

        when(walletConfig.getMaxCreditLimit()).thenReturn(new BigDecimal("100000.00"));
        when(walletConfig.getMaxDebitLimit()).thenReturn(new BigDecimal("50000.00"));

        walletService = new WalletService(walletRepository, transactionRepository, walletConfig, transactionService);

        log.info("===== WalletServiceTest Setup Completed =====");
    }

    // ======================================================
    // ================ CREDIT TEST CASES ====================
    // ======================================================

    @Test
    void testCreditSuccess() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.credit(1L, BigDecimal.valueOf(500), "Deposit");

        assertInstanceOf(WalletOperationResult.Success.class, result);
        assertEquals("1500.00", ((WalletOperationResult.Success) result).message().split(": ")[1]);
        verify(transactionService, times(1))
                .validateTransactionLimits(eq(1L), eq(BigDecimal.valueOf(500)), eq(Transaction.Type.CREDIT));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void testCreditWithNegativeAmount() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.credit(1L, BigDecimal.valueOf(-200), "Invalid Deposit");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("INVALID_AMOUNT", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testCreditExceedingLimit() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.credit(1L, new BigDecimal("200000"), "Huge Deposit");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("LIMIT_EXCEEDED", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testCreditToNonExistentWallet() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        WalletOperationResult result = walletService.credit(99L, BigDecimal.valueOf(500), "Deposit");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("WALLET_NOT_FOUND", ((WalletOperationResult.Failure) result).errorCode());
    }

    @Test
    void testCreditLimitValidationFailure() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(2000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        doThrow(new IllegalArgumentException("Daily limit exceeded"))
                .when(transactionService)
                .validateTransactionLimits(eq(1L), eq(BigDecimal.valueOf(1000)), eq(Transaction.Type.CREDIT));

        WalletOperationResult result = walletService.credit(1L, BigDecimal.valueOf(1000), "Deposit");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("LIMIT_EXCEEDED", ((WalletOperationResult.Failure) result).errorCode());
        assertTrue(((WalletOperationResult.Failure) result).reason().contains("Daily limit exceeded"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ======================================================
    // ================ DEBIT TEST CASES =====================
    // ======================================================

    @Test
    void testDebitSuccess() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.debit(1L, BigDecimal.valueOf(300), "Purchase");

        assertInstanceOf(WalletOperationResult.Success.class, result);
        assertEquals("700.00", ((WalletOperationResult.Success) result).message().split(": ")[1]);
        verify(transactionService, times(1))
                .validateTransactionLimits(eq(1L), eq(BigDecimal.valueOf(300)), eq(Transaction.Type.DEBIT));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testDebitInsufficientFunds() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(100), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.debit(1L, BigDecimal.valueOf(200), "Withdraw");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("INSUFFICIENT_FUNDS", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testDebitWithNegativeAmount() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(500), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.debit(1L, BigDecimal.valueOf(-100), "Invalid Withdrawal");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("INVALID_AMOUNT", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testDebitFromNonExistentWallet() {
        when(walletRepository.findById(77L)).thenReturn(Optional.empty());

        WalletOperationResult result = walletService.debit(77L, BigDecimal.valueOf(300), "Purchase");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("WALLET_NOT_FOUND", ((WalletOperationResult.Failure) result).errorCode());
    }

    @Test
    void testDebitLimitValidationFailure() {
        Wallet wallet = new Wallet("Wallet A", BigDecimal.valueOf(2000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        doThrow(new IllegalArgumentException("Monthly limit exceeded"))
                .when(transactionService)
                .validateTransactionLimits(eq(1L), eq(BigDecimal.valueOf(500)), eq(Transaction.Type.DEBIT));

        WalletOperationResult result = walletService.debit(1L, BigDecimal.valueOf(500), "Purchase");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("LIMIT_EXCEEDED", ((WalletOperationResult.Failure) result).errorCode());
        assertTrue(((WalletOperationResult.Failure) result).reason().contains("Monthly limit exceeded"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ======================================================
    // ================ BALANCE TEST CASES ==================
    // ======================================================

    @Test
    void testGetBalanceSuccess() {
        Wallet wallet = new Wallet("My Wallet", BigDecimal.valueOf(1200), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.getBalance(1L);

        assertInstanceOf(WalletOperationResult.Balance.class, result);
        WalletOperationResult.Balance balanceResult = (WalletOperationResult.Balance) result;
        assertEquals("1200.00", balanceResult.balance());
        assertEquals(1L, balanceResult.walletId());
    }

    @Test
    void testGetBalanceWalletNotFound() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        WalletOperationResult result = walletService.getBalance(99L);

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("WALLET_NOT_FOUND", ((WalletOperationResult.Failure) result).errorCode());
    }

    // ======================================================
    // =============== EXCEPTION TEST CASES =================
    // ======================================================

    @Test
    void testNullAmount() {
        Wallet wallet = new Wallet("Wallet A", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        WalletOperationResult result = walletService.credit(1L, null, "Null Deposit");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("INVALID_AMOUNT", ((WalletOperationResult.Failure) result).errorCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testExceptionHandling() {
        when(walletRepository.findById(any())).thenThrow(new RuntimeException("DB connection failed"));

        WalletOperationResult result = walletService.credit(1L, BigDecimal.valueOf(100), "Deposit");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("UNKNOWN_ERROR", ((WalletOperationResult.Failure) result).errorCode());
        assertTrue(((WalletOperationResult.Failure) result).reason().contains("DB connection failed"));
    }

    @Test
    void testConcurrentUpdate_ThrowsOptimisticLockException() {
        Wallet wallet = new Wallet("Concurrent Wallet", BigDecimal.valueOf(1000), null);
        wallet.setId(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenThrow(new OptimisticLockException("Concurrent update detected"));

        WalletOperationResult result = walletService.credit(1L, BigDecimal.valueOf(200), "Concurrent deposit");

        assertInstanceOf(WalletOperationResult.Failure.class, result);
        assertEquals("CONFLICT", ((WalletOperationResult.Failure) result).errorCode());
    }
}
