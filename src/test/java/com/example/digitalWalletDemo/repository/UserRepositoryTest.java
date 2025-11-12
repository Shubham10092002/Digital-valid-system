package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import com.example.digitalWalletDemo.repository.userRepository.UserRepository;
import com.example.digitalWalletDemo.repository.walletRepository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private User user1, user2;

    @BeforeEach
    void setup() {
        walletRepository.deleteAll();
        userRepository.deleteAll();

        user1 = new User("john_doe", "password123");
        user1 = userRepository.save(user1);
        Wallet wallet1 = new Wallet("Wallet1", BigDecimal.valueOf(500), user1);
        walletRepository.save(wallet1);

        user2 = new User("jane_smith", "password456");
        user2 = userRepository.save(user2);
        Wallet wallet2 = new Wallet("Wallet2", BigDecimal.valueOf(50), user2);
        walletRepository.save(wallet2);
    }

    @Test
    void testFindByUsername() {
        Optional<User> found = userRepository.findByUsername("john_doe");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    void testFindUsersWithWalletBalanceGreaterThan() {
        List<User> richUsers = userRepository.findUsersWithWalletBalanceGreaterThan(BigDecimal.valueOf(100));
        assertThat(richUsers).hasSize(1);
        assertThat(richUsers.get(0).getUsername()).isEqualTo("john_doe");
    }
}
