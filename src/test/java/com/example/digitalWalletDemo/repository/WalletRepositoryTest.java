package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.dto.WalletBalanceDTO;
import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        walletRepository.deleteAll();
        userRepository.deleteAll();

        user = new User("alice", "pass123");
        user = userRepository.save(user);

        walletRepository.save(new Wallet("Wallet1", BigDecimal.valueOf(100), user));
        walletRepository.save(new Wallet("Wallet2", BigDecimal.valueOf(200), user));
    }

    @Test
    void testFindByUserId() {
        List<Wallet> wallets = walletRepository.findByUserId(user.getId());
        assertThat(wallets).hasSize(2);
    }

    @Test
    void testFindByUserId_NoWallets() {
        List<Wallet> wallets = walletRepository.findByUserId(999L); // Non-existent user
        assertThat(wallets).isEmpty();
    }

    @Test
    void testGetTotalBalanceByUserId() {
        BigDecimal total = walletRepository.getTotalBalanceByUserId(user.getId());
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    void testGetTotalBalanceByUserId_NoWallets() {
        BigDecimal total = walletRepository.getTotalBalanceByUserId(999L); // Non-existent user
        assertThat(total).isNull(); // Because query returns null if no wallets
    }

    @Test
    void testGetWalletBalancesByUserId() {
        List<WalletBalanceDTO> balances = walletRepository.getWalletBalancesByUserId(user.getId());
        assertThat(balances).hasSize(2);
        assertThat(balances.get(0).balance()).isNotNull();
    }

    @Test
    void testGetWalletBalancesByUserId_NoWallets() {
        List<WalletBalanceDTO> balances = walletRepository.getWalletBalancesByUserId(999L);
        assertThat(balances).isEmpty();
    }

    @Test
    void testFindUsersWithWalletBalanceGreaterThan() {
        // Should find alice since total balance > 50
        List<User> users = userRepository.findUsersWithWalletBalanceGreaterThan(BigDecimal.valueOf(50));
        assertThat(users).contains(user);

        // Should return empty for high threshold
        users = userRepository.findUsersWithWalletBalanceGreaterThan(BigDecimal.valueOf(1000));
        assertThat(users).isEmpty();
    }
}
