package com.example.digitalWalletDemo.dto.userdto;

import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;

import java.math.BigDecimal;

public class UserResponseDTO {

    private Long userId;
    private String username;
    private Long walletId;
    private String walletName;
    private BigDecimal walletBalance;

    // 🔹 Constructors
    public UserResponseDTO() {}

    public UserResponseDTO(User user, Wallet wallet) {
        this.userId = user.getId();
        this.username = user.getUsername();
        if (wallet != null) {
            this.walletId = wallet.getId();
            this.walletName = wallet.getWalletName();
            this.walletBalance = wallet.getBalance();
        }
    }

    public UserResponseDTO(Long userId, String username,
                           Long walletId, String walletName,
                           BigDecimal walletBalance) {
        this.userId = userId;
        this.username = username;
        this.walletId = walletId;
        this.walletName = walletName;
        this.walletBalance = walletBalance;
    }

    // 🔹 Getters and Setters
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public Long getWalletId() {
        return walletId;
    }
    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getWalletName() {
        return walletName;
    }
    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }
    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }
}
