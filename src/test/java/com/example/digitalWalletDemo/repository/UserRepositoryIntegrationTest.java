package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import com.example.digitalWalletDemo.repository.userRepository.UserRepository;
import com.example.digitalWalletDemo.repository.walletRepository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setup() {
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testSaveUserWithWallets_CascadePersist() {
        User user = new User("john_doe", "secure123");
        Wallet w1 = new Wallet("Wallet1", BigDecimal.valueOf(1000), user);
        Wallet w2 = new Wallet("Wallet2", BigDecimal.valueOf(500), user);
        user.setWallets(List.of(w1, w2));

        userRepository.save(user);

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getWallets()).hasSize(2);
    }

    @Test
    void testDeleteUser_CascadeDeleteWallets() {
        User user = new User("alice", "pass123");
        Wallet wallet = new Wallet("Primary", BigDecimal.valueOf(200), user);
        user.setWallets(List.of(wallet));

        user = userRepository.save(user);
        Long userId = user.getId();

        userRepository.deleteById(userId);

        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(walletRepository.findAll()).isEmpty(); // cascade delete check
    }

    @Test
    void testPreventDuplicateUsername() {
        userRepository.save(new User("bob", "p1"));
        User duplicate = new User("bob", "p2");

        try {
            userRepository.saveAndFlush(duplicate);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    // ✅ NEW TEST 1: Lazy loading behavior
    @Test
    void testLazyLoading_UserInWallet() {
        User user = new User("lazyUser", "pw");
        Wallet wallet = new Wallet("LazyWallet", BigDecimal.valueOf(100), user);
        user.setWallets(List.of(wallet));

        userRepository.saveAndFlush(user);

        Wallet fetchedWallet = walletRepository.findById(wallet.getId()).orElseThrow();

        // The user is lazily loaded — access triggers fetch
        assertThat(fetchedWallet.getUser().getUsername()).isEqualTo("lazyUser");
    }

    // ✅ NEW TEST 2: Orphan removal test
    @Test
    void testOrphanRemoval_RemovingWalletDeletesIt() {
        User user = new User("orphanUser", "pw123");
        Wallet w1 = new Wallet("WalletA", BigDecimal.valueOf(200), user);
        Wallet w2 = new Wallet("WalletB", BigDecimal.valueOf(400), user);

        user.setWallets(new ArrayList<>(List.of(w1, w2))); // ✅ Mutable list

        user = userRepository.save(user);

        // Remove one wallet from list
        user.getWallets().remove(w1);
        userRepository.saveAndFlush(user);

        List<Wallet> wallets = walletRepository.findAll();
        assertThat(wallets).hasSize(1);
        assertThat(wallets.get(0).getWalletName()).isEqualTo("WalletB");
    }

}
