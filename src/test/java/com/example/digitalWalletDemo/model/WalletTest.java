package com.example.digitalWalletDemo.model;

import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void testWalletCreation() {
        User user = new User("shubham", "pass123");
        Wallet wallet = new Wallet("Shubham Wallet", BigDecimal.valueOf(5000), user);

        assertEquals("Shubham Wallet", wallet.getWalletName());
        assertEquals(BigDecimal.valueOf(5000), wallet.getBalance());
        assertEquals(user, wallet.getUser());
    }

    @Test
    void testWalletCredit() {
        Wallet wallet = new Wallet("Wallet 1", BigDecimal.valueOf(1000), null);
        wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(500)));

        assertEquals(BigDecimal.valueOf(1500), wallet.getBalance());
    }

    @Test
    void testWalletDebit() {
        Wallet wallet = new Wallet("Wallet 2", BigDecimal.valueOf(2000), null);
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(1000)));

        assertEquals(BigDecimal.valueOf(1000), wallet.getBalance());
    }
}
