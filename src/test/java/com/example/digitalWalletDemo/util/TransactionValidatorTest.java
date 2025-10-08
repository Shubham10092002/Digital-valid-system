package com.example.digitalWalletDemo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorTest {

    @Test
    void testValidStringDescription() {
        assertTrue(TransactionValidator.isValidTransactionData("Payment"));
        assertFalse(TransactionValidator.isValidTransactionData(""));
        assertFalse(TransactionValidator.isValidTransactionData("  "));
    }

    @Test
    void testValidLongId() {
        assertTrue(TransactionValidator.isValidTransactionData(123L));
        assertFalse(TransactionValidator.isValidTransactionData(0L));
        assertFalse(TransactionValidator.isValidTransactionData(-10L));
    }

    @Test
    void testValidNumber() {
        assertTrue(TransactionValidator.isValidTransactionData(50));
        assertTrue(TransactionValidator.isValidTransactionData(12.5));
        assertFalse(TransactionValidator.isValidTransactionData(-5));
        assertFalse(TransactionValidator.isValidTransactionData(0));
    }
}
