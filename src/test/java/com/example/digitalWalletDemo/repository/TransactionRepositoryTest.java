package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.transactionModel.Transaction;
import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import com.example.digitalWalletDemo.repository.transactionRepository.TransactionRepository;
import com.example.digitalWalletDemo.repository.userRepository.UserRepository;
import com.example.digitalWalletDemo.repository.walletRepository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private Wallet wallet;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User("bob", "pass123");
        user = userRepository.save(user);

        wallet = new Wallet("Wallet1", BigDecimal.valueOf(100), user);
        wallet = walletRepository.save(wallet);

        transactionRepository.save(new Transaction(wallet, BigDecimal.valueOf(50), Transaction.Type.CREDIT, "Deposit"));
        transactionRepository.save(new Transaction(wallet, BigDecimal.valueOf(20), Transaction.Type.DEBIT, "Purchase"));
    }

    @Test
    void testFindByWalletId() {
        List<Transaction> transactions = transactionRepository.findByWalletId(wallet.getId());
        assertThat(transactions).hasSize(2);
    }

    @Test
    void testFindByWalletId_NoTransactions() {
        List<Transaction> transactions = transactionRepository.findByWalletId(999L);
        assertThat(transactions).isEmpty();
    }

    @Test
    void testFindUserTransactionsBetweenDates() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<Transaction> transactions = transactionRepository.findUserTransactionsBetweenDates(
                wallet.getUser().getId(), start, end, null
        );
        assertThat(transactions).hasSize(2);
    }

    @Test
    void testFindUserTransactionsBetweenDates_TypeFilter() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<Transaction> creditTx = transactionRepository.findUserTransactionsBetweenDates(
                wallet.getUser().getId(), start, end, Transaction.Type.CREDIT
        );
        assertThat(creditTx).hasSize(1);
        assertThat(creditTx.get(0).getType()).isEqualTo(Transaction.Type.CREDIT);
    }

    @Test
    void testFindUserTransactionsBetweenDates_NoTransactions() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().minusDays(4);
        List<Transaction> transactions = transactionRepository.findUserTransactionsBetweenDates(
                wallet.getUser().getId(), start, end, null
        );
        assertThat(transactions).isEmpty();
    }

    @Test
    void testGetTransactionSumsByType() {
        List<Object[]> sums = transactionRepository.getTransactionSumsByType(wallet.getId());
        assertThat(sums).hasSize(2); // one CREDIT and one DEBIT

        // Validate actual sum values
        for (Object[] row : sums) {
            Transaction.Type type = (Transaction.Type) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (type == Transaction.Type.CREDIT) {
                assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(50));
            } else if (type == Transaction.Type.DEBIT) {
                assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(20));
            }
        }
    }

    @Test
    void testGetTransactionSumsByType_NoTransactions() {
        Wallet newWallet = walletRepository.save(new Wallet("EmptyWallet", BigDecimal.ZERO, wallet.getUser()));
        List<Object[]> sums = transactionRepository.getTransactionSumsByType(newWallet.getId());
        assertThat(sums).isEmpty();
    }
}
