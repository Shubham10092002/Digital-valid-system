package com.example.digitalWalletDemo.service;

import com.example.digitalWalletDemo.config.WalletConfig;
import com.example.digitalWalletDemo.dto.transactiondto.TransactionDTO;
import com.example.digitalWalletDemo.exception.WalletIdNotFoundException;
import com.example.digitalWalletDemo.model.transactionModel.Transaction;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import com.example.digitalWalletDemo.repository.transactionRepository.TransactionRepository;
import com.example.digitalWalletDemo.repository.walletRepository.WalletRepository;
import com.example.digitalWalletDemo.service.transactionService.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@MockitoSettings(strictness = Strictness.LENIENT)

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletConfig walletConfig;

    @InjectMocks
    private TransactionService transactionService;

    private Wallet wallet;
    private Transaction transaction;

    @BeforeEach
    void setup() {
        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setBalance(BigDecimal.valueOf(1000));

        transaction = new Transaction();
        transaction.setId(10L);
        transaction.setWallet(wallet);
        transaction.setAmount(BigDecimal.valueOf(500));
        transaction.setType(Transaction.Type.CREDIT);
        transaction.setDescription("Test credit");
        transaction.setTimestamp(LocalDateTime.now());

        // Default config limits
        when(walletConfig.getDailyDebitLimit()).thenReturn(new BigDecimal("10000"));
        when(walletConfig.getMonthlyDebitLimit()).thenReturn(new BigDecimal("50000"));
        when(walletConfig.getDailyCreditLimit()).thenReturn(new BigDecimal("20000"));
        when(walletConfig.getMonthlyCreditLimit()).thenReturn(new BigDecimal("100000"));
    }

    // ✅ 1. Get all transactions
    @Test
    void testGetAllTransactions_Success() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        List<TransactionDTO> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(500), result.get(0).getAmount());
        verify(transactionRepository, times(1)).findAll();
    }

    // ✅ 2. Get transactions by walletId (valid)
    @Test
    void testGetTransactionsByWallet_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenReturn(List.of(transaction));

        List<TransactionDTO> result = transactionService.getTransactionsByWallet(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CREDIT", result.get(0).getType());
        verify(walletRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).findByWalletId(1L);
    }

    // ❌ 3. Get transactions by walletId (wallet not found)
    @Test
    void testGetTransactionsByWallet_WalletNotFound() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WalletIdNotFoundException.class,
                () -> transactionService.getTransactionsByWallet(99L));

        verify(transactionRepository, never()).findByWalletId(anyLong());
    }

    // ✅ 4. Get transactions by walletId (empty list)
    @Test
    void testGetTransactionsByWallet_EmptyList() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId(1L)).thenReturn(Collections.emptyList());

        List<TransactionDTO> result = transactionService.getTransactionsByWallet(1L);

        assertTrue(result.isEmpty());
    }

    // ✅ 5. Get user transactions (valid date & type)
    @Test
    void testGetUserTransactions_Success() {
        Long userId = 1L;
        String start = "01-01-2024";
        String end = "10-01-2024";
        String type = "CREDIT";

        when(transactionRepository.findUserTransactionsBetweenDates(
                anyLong(), any(), any(), any())).thenReturn(List.of(transaction));

        Object response = transactionService.getUserTransactions(userId, start, end, type);

        assertTrue(response instanceof List);
        List<?> result = (List<?>) response;
        assertEquals(1, result.size());
        verify(transactionRepository, times(1))
                .findUserTransactionsBetweenDates(eq(userId), any(), any(), any());
    }

    // ❌ 6. Invalid date format
    @Test
    void testGetUserTransactions_InvalidDateFormat() {
        Object response = transactionService.getUserTransactions(1L, "2024-01-01", "10-01-2024", null);

        assertTrue(response instanceof String);
        assertEquals("Invalid date format. Please use dd-MM-yyyy.", response);
    }

    // ❌ 7. Invalid transaction type
    @Test
    void testGetUserTransactions_InvalidType() {
        Object response = transactionService.getUserTransactions(1L, "01-01-2024", "10-01-2024", "INVALID");

        assertTrue(response instanceof String);
        assertEquals("Invalid transaction type. Use CREDIT, DEBIT, or TRANSFER.", response);
    }

    // ✅ 8. Get user transactions with null type
    @Test
    void testGetUserTransactions_NullType() {
        when(transactionRepository.findUserTransactionsBetweenDates(
                anyLong(), any(), any(), isNull()))
                .thenReturn(List.of(transaction));

        Object response = transactionService.getUserTransactions(1L, "01-01-2024", "10-01-2024", null);

        assertTrue(response instanceof List);
        List<?> result = (List<?>) response;
        assertEquals(1, result.size());
    }

    // ✅ 9. Validate transaction limits - within credit limits
    @Test
    void testValidateTransactionLimits_WithinCreditLimits() {
        when(transactionRepository.getTotalAmountByWalletAndTypeBetweenDates(anyLong(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        assertDoesNotThrow(() ->
                transactionService.validateTransactionLimits(1L, new BigDecimal("1000"), Transaction.Type.CREDIT)
        );
    }

    // ❌ 10. Daily credit limit exceeded
    @Test
    void testValidateTransactionLimits_DailyCreditLimitExceeded() {
        when(transactionRepository.getTotalAmountByWalletAndTypeBetweenDates(
                anyLong(), eq(Transaction.Type.CREDIT), any(), any()))
                .thenReturn(new BigDecimal("19500")) // 1st call (daily)
                .thenReturn(BigDecimal.ZERO);         // 2nd call (monthly)


        when(walletConfig.getDailyCreditLimit()).thenReturn(new BigDecimal("20000"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.validateTransactionLimits(1L, new BigDecimal("1000"), Transaction.Type.CREDIT)
        );

        assertTrue(ex.getMessage().contains("CREDIT daily limit exceeded"));

    }

    // ❌ 11. Monthly debit limit exceeded
    @Test
    void testValidateTransactionLimits_MonthlyDebitLimitExceeded() {
        when(transactionRepository.getTotalAmountByWalletAndTypeBetweenDates(
                eq(1L), eq(Transaction.Type.DEBIT), any(), any()))
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(new BigDecimal("49000"));

        when(walletConfig.getMonthlyDebitLimit()).thenReturn(new BigDecimal("50000"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.validateTransactionLimits(1L, new BigDecimal("2000"), Transaction.Type.DEBIT)
        );

        assertTrue(ex.getMessage().contains("DEBIT monthly limit exceeded"));

    }


    // ✅ 12. Create transaction successfully
    @Test
    void testCreateTransaction_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.getTotalAmountByWalletAndTypeBetweenDates(anyLong(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionDTO result = transactionService.createTransaction(1L, BigDecimal.valueOf(1000), Transaction.Type.CREDIT);

        assertNotNull(result);
        assertEquals("CREDIT", result.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // ❌ 13. Create transaction - wallet not found
    @Test
    void testCreateTransaction_WalletNotFound() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WalletIdNotFoundException.class,
                () -> transactionService.createTransaction(99L, BigDecimal.valueOf(1000), Transaction.Type.CREDIT));
    }

    // ❌ 14. Create transaction - limit exceeded
    @Test
    void testCreateTransaction_LimitExceeded() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(transactionRepository.getTotalAmountByWalletAndTypeBetweenDates(anyLong(), eq(Transaction.Type.DEBIT), any(), any()))
                .thenReturn(new BigDecimal("9900"));
        when(walletConfig.getDailyDebitLimit()).thenReturn(new BigDecimal("10000"));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(1L, new BigDecimal("2000"), Transaction.Type.DEBIT));
    }
}
