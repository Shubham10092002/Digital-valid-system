package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class WalletRepositoryIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        walletRepository.deleteAll();
        userRepository.deleteAll();

        user = new User("jane", "pass123");
        user = userRepository.save(user);
    }

    @Test
    void testSaveAndFindWalletByUser() {
        Wallet w1 = new Wallet("Wallet1", BigDecimal.valueOf(150), user);
        Wallet w2 = new Wallet("Wallet2", BigDecimal.valueOf(250), user);
        walletRepository.saveAll(List.of(w1, w2));

        List<Wallet> wallets = walletRepository.findByUserId(user.getId());
        assertThat(wallets).hasSize(2);
    }

    @Test
    void testUpdateWalletBalance() {
        Wallet wallet = new Wallet("Main", BigDecimal.valueOf(100), user);
        wallet = walletRepository.save(wallet);

        wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(50)));
        walletRepository.save(wallet);

        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void testDeleteWallet_DoesNotDeleteUser() {
        Wallet wallet = walletRepository.save(new Wallet("Temp", BigDecimal.valueOf(100), user));
        walletRepository.delete(wallet);

        assertThat(userRepository.findById(user.getId())).isPresent();
    }
}
