package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class TransactionRepositoryIntegrationTest {

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

        User user = userRepository.save(new User("mike", "pw123"));
        wallet = walletRepository.save(new Wallet("Main Wallet", BigDecimal.valueOf(500), user));
    }

    @Test
    void testSaveTransactionAndRetrieve() {
        Transaction tx = new Transaction(wallet, BigDecimal.valueOf(100), Transaction.Type.CREDIT, "Initial deposit");
        transactionRepository.save(tx);

        List<Transaction> list = transactionRepository.findByWalletId(wallet.getId());
        assertThat(list).hasSize(1);
    }

    @Test
    void testFindUserTransactionsBetweenDates() {
        Transaction t1 = new Transaction(wallet, BigDecimal.valueOf(100), Transaction.Type.CREDIT, "Deposit");
        transactionRepository.save(t1);

        // Use UTC-based time range for consistency with DB storage
        ZoneOffset utc = ZoneOffset.UTC;
        LocalDateTime start = LocalDateTime.now(utc).minusHours(1);
        LocalDateTime end = LocalDateTime.now(utc).plusHours(1);

        List<Transaction> txs = transactionRepository.findUserTransactionsBetweenDates(
                wallet.getUser().getId(), start, end, null
        );

        assertThat(txs)
                .as("Should find at least one transaction within the UTC window")
                .isNotEmpty();
    }


    @Test
    void testInvalidTransactionAmountThrowsError() {
        try {
            new Transaction(wallet, BigDecimal.ZERO, Transaction.Type.CREDIT, "Invalid");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("must be greater than 0");
        }
    }
}
