package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryEdgeCasesTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setup() {
        // Create user and wallet
        user = userRepository.save(new User("tester", "Test User"));
        wallet = walletRepository.save(new Wallet("TestWallet", BigDecimal.valueOf(1000), user));

        // Insert transactions with explicit timestamps
        Transaction tx1 = new Transaction(wallet, BigDecimal.valueOf(100), Transaction.Type.CREDIT, "Initial credit");
        tx1.setTimestamp(LocalDateTime.of(2025, 10, 10, 0, 0));
        transactionRepository.save(tx1);

        Transaction tx2 = new Transaction(wallet, BigDecimal.valueOf(200), Transaction.Type.DEBIT, "Mid-month debit");
        tx2.setTimestamp(LocalDateTime.of(2025, 10, 15, 12, 0));
        transactionRepository.save(tx2);

        Transaction tx3 = new Transaction(wallet, BigDecimal.valueOf(300), Transaction.Type.CREDIT, "End-month credit");
        tx3.setTimestamp(LocalDateTime.of(2025, 10, 20, 23, 59));
        transactionRepository.save(tx3);
    }

//    @Test
//    void testTransactionsWithinDateRange_inclusiveBoundaries() {
//        LocalDateTime start = LocalDateTime.of(2025, 10, 10, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 10, 20, 23, 59);
//
//        List<Transaction> transactions = transactionRepository.findUserTransactionsBetweenDates(user.getId(), start, end, null);
//        assertThat(transactions).hasSize(3); // all transactions included
//    }



//
//    @Test
//    void testTransactionsWithinDateRange_withTypeFilter() {
//        LocalDateTime start = LocalDateTime.of(2025, 10, 10, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 10, 20, 23, 59);
//
//        List<Transaction> creditTransactions = transactionRepository.findUserTransactionsBetweenDates(user.getId(), start, end, Transaction.Type.CREDIT);
//        assertThat(creditTransactions).hasSize(2);
//        assertThat(creditTransactions).allMatch(t -> t.getType() == Transaction.Type.CREDIT);
//    }

    @Test
    void testTransactionsOutsideDateRange() {
        LocalDateTime start = LocalDateTime.of(2025, 9, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 9, 30, 23, 59);

        List<Transaction> transactions = transactionRepository.findUserTransactionsBetweenDates(user.getId(), start, end, null);
        assertThat(transactions).isEmpty(); // no transactions in this range
    }

//    @Test
//    void testTransactionsWithNullType() {
//        LocalDateTime start = LocalDateTime.of(2025, 10, 10, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 10, 20, 23, 59);
//
//        List<Transaction> transactions = transactionRepository.findUserTransactionsBetweenDates(user.getId(), start, end, null);
//        assertThat(transactions).hasSize(3); // null type fetches all
//    }

    @Test
    void testTransactionSumsByType() {
        List<Object[]> sums = transactionRepository.getTransactionSumsByType(wallet.getId());

        sums.forEach(obj -> {
            Transaction.Type type = (Transaction.Type) obj[0];
            BigDecimal total = (BigDecimal) obj[1];

            if (type == Transaction.Type.CREDIT) assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(400));
            if (type == Transaction.Type.DEBIT) assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(200));
        });
    }

    @Test
    void testTopTransactionsOrdering() {
        List<Transaction> topTransactions = transactionRepository.findTopTransactionsByWalletId(wallet.getId());

        assertThat(topTransactions).hasSize(3);
        assertThat(topTransactions.get(0).getTimestamp()).isAfterOrEqualTo(topTransactions.get(1).getTimestamp());
        assertThat(topTransactions.get(1).getTimestamp()).isAfterOrEqualTo(topTransactions.get(2).getTimestamp());
    }
}
