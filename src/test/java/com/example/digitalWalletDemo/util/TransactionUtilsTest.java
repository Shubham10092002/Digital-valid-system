package com.example.digitalWalletDemo.util;

import com.example.digitalWalletDemo.data.WalletTransaction;
import com.example.digitalWalletDemo.data.WalletTransaction.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionUtilsTest {

    private List<WalletTransaction> transactions;

    @BeforeEach
    void setUp() {
        // Sample data for testing stream operations
        transactions = Arrays.asList(
                // CREDIT transactions
                new WalletTransaction(1L, 1L, new BigDecimal("100.00"), Type.CREDIT, "D1", null),
                new WalletTransaction(3L, 1L, new BigDecimal("200.00"), Type.CREDIT, "D2", null),
                // DEBIT transactions
                new WalletTransaction(2L, 1L, new BigDecimal("50.50"), Type.DEBIT, "W1", null),
                new WalletTransaction(4L, 2L, new BigDecimal("25.00"), Type.DEBIT, "W2", null),
                // TRANSFER transaction (ignored by Credit/Debit filters)
                new WalletTransaction(5L, 3L, new BigDecimal("15.00"), Type.TRANSFER, "T1", null)
        );
    }

    @Test
    void calculateTotal_shouldReturnCorrectCreditTotal() {
        BigDecimal expected = new BigDecimal("300.00"); // 100.00 + 200.00
        BigDecimal actual = TransactionUtils.calculateTotal(transactions, Type.CREDIT);
        assertEquals(expected, actual.stripTrailingZeros()); // stripZeros for safe BigDecimal comparison
    }

    @Test
    void calculateTotal_shouldReturnCorrectDebitTotal() {
        BigDecimal expected = new BigDecimal("75.50"); // 50.50 + 25.00
        BigDecimal actual = TransactionUtils.calculateTotal(transactions, Type.DEBIT);
        assertEquals(expected, actual.stripTrailingZeros());
    }

    @Test
    void calculateTotal_shouldReturnZeroForEmptyList() {
        BigDecimal actual = TransactionUtils.calculateTotal(Collections.emptyList(), Type.CREDIT);
        assertEquals(BigDecimal.ZERO, actual);
    }

    @Test
    void filterByMinAmount_shouldReturnFilteredList() {
        // Filter for transactions >= 90.00
        List<WalletTransaction> filtered = TransactionUtils.filterByMinAmount(transactions, new BigDecimal("90.00"));
        assertEquals(2, filtered.size()); // Should be 100.00 and 200.00
        // Assert that all filtered transactions meet the criteria
        assertTrue(filtered.stream().allMatch(t -> t.amount().compareTo(new BigDecimal("90.00")) >= 0));
    }
}