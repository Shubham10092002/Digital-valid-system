package com.example.digitalWalletDemo.data;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class WalletTransactionTest {

    @Test
    void shouldCreateTransactionSuccessfully() {
        // Test a valid transaction creation
        assertDoesNotThrow(() -> new WalletTransaction(
                1L, 100L, new BigDecimal("50.00"), WalletTransaction.Type.CREDIT, "Deposit", null
        ));
    }

    @Test
    void shouldThrowExceptionForInvalidWalletId() {
        // walletId is null
        assertThrows(IllegalArgumentException.class, () -> new WalletTransaction(
                1L, null, new BigDecimal("50.00"), WalletTransaction.Type.CREDIT, "Deposit", null
        ));
        // walletId is zero or negative
        assertThrows(IllegalArgumentException.class, () -> new WalletTransaction(
                1L, 0L, new BigDecimal("50.00"), WalletTransaction.Type.CREDIT, "Deposit", null
        ));
        assertThrows(IllegalArgumentException.class, () -> new WalletTransaction(
                1L, -5L, new BigDecimal("50.00"), WalletTransaction.Type.CREDIT, "Deposit", null
        ));
    }

    @Test
    void shouldThrowExceptionForNonPositiveAmount() {
        // amount is zero
        assertThrows(IllegalArgumentException.class, () -> new WalletTransaction(
                1L, 100L, BigDecimal.ZERO, WalletTransaction.Type.CREDIT, "Deposit", null
        ));
        // amount is negative
        assertThrows(IllegalArgumentException.class, () -> new WalletTransaction(
                1L, 100L, new BigDecimal("-10.00"), WalletTransaction.Type.DEBIT, "Withdrawal", null
        ));
    }

    @Test
    void shouldThrowExceptionForNullType() {
        assertThrows(IllegalArgumentException.class, () -> new WalletTransaction(
                1L, 100L, new BigDecimal("50.00"), null, "Deposit", null
        ));
    }
}