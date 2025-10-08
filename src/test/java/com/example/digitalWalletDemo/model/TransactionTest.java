package com.example.digitalWalletDemo.model;

import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void testTransactionCreation() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        Transaction transaction = new Transaction(wallet, BigDecimal.valueOf(500), Transaction.Type.CREDIT, "Test Credit");

        assertEquals(wallet, transaction.getWallet());
        assertEquals(BigDecimal.valueOf(500), transaction.getAmount());
        assertEquals(Transaction.Type.CREDIT, transaction.getType());
        assertEquals("Test Credit", transaction.getDescription());
    }

    @Test
    void testInvalidAmount() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(1000), null);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(wallet, BigDecimal.valueOf(-500), Transaction.Type.CREDIT, "Invalid")
        );
        assertEquals("Invalid amount: must be greater than 0", exception.getMessage());
    }

    @Test
    void testInvalidType() {
        Wallet wallet = new Wallet("Test Wallet", BigDecimal.valueOf(-500), null);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(wallet, BigDecimal.valueOf(500), null, "Invalid Type")
        );
        assertEquals("Transaction type must be specified", exception.getMessage());
    }
}
