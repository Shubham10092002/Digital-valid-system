package com.example.digitalWalletDemo.util;

import com.example.digitalWalletDemo.data.WalletTransaction;
import com.example.digitalWalletDemo.data.WalletTransaction.Type;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionUtilsTest {

    @Test
    void testCalculateTotal() {
        List<WalletTransaction> transactions = List.of(
                new WalletTransaction(1L, 1L, BigDecimal.valueOf(100), Type.CREDIT, "Credit 1", LocalDateTime.now()),
                new WalletTransaction(2L, 1L, BigDecimal.valueOf(50), Type.DEBIT, "Debit 1", LocalDateTime.now()),
                new WalletTransaction(3L, 1L, BigDecimal.valueOf(200), Type.CREDIT, "Credit 2", LocalDateTime.now())
        );

        BigDecimal totalCredit = TransactionUtils.calculateTotal(transactions, Type.CREDIT);
        BigDecimal totalDebit = TransactionUtils.calculateTotal(transactions, Type.DEBIT);

        assertEquals(BigDecimal.valueOf(300), totalCredit);
        assertEquals(BigDecimal.valueOf(50), totalDebit);
    }

    @Test
    void testFilterByMinAmount() {
        List<WalletTransaction> transactions = List.of(
                new WalletTransaction(1L, 1L, BigDecimal.valueOf(100), Type.CREDIT, "Credit 1", LocalDateTime.now()),
                new WalletTransaction(2L, 1L, BigDecimal.valueOf(50), Type.DEBIT, "Debit 1", LocalDateTime.now()),
                new WalletTransaction(3L, 1L, BigDecimal.valueOf(200), Type.CREDIT, "Credit 2", LocalDateTime.now())
        );

        List<WalletTransaction> filtered = TransactionUtils.filterByMinAmount(transactions, BigDecimal.valueOf(100));

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(t -> t.amount().compareTo(BigDecimal.valueOf(100)) >= 0));
    }
}
