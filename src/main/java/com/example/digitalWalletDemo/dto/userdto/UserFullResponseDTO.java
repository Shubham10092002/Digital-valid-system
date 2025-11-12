package com.example.digitalWalletDemo.dto.userdto;

import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import java.util.List;
import java.util.stream.Collectors;

public class UserFullResponseDTO {

    private Long id;
    private String username;
    private List<WalletInfo> wallets;

    public UserFullResponseDTO() {}

    public UserFullResponseDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.wallets = user.getWallets()
                .stream()
                .map(WalletInfo::new)
                .collect(Collectors.toList());
    }

    // Nested DTO for wallet info
    public static class WalletInfo {
        private Long id;
        private String walletName;
        private java.math.BigDecimal balance;
        private int version;

        public WalletInfo(Wallet wallet) {
            this.id = wallet.getId();
            this.walletName = wallet.getWalletName();
            this.balance = wallet.getBalance();
            //this.version = wallet.getVersion();
        }

        // Getters
        public Long getId() { return id; }
        public String getWalletName() { return walletName; }
        public java.math.BigDecimal getBalance() { return balance; }
        public int getVersion() { return version; }
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public List<WalletInfo> getWallets() { return wallets; }
}
