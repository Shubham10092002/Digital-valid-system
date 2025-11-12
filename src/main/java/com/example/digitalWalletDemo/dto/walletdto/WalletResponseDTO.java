package com.example.digitalWalletDemo.dto.walletdto;

import com.example.digitalWalletDemo.model.walletModel.Wallet;
import java.math.BigDecimal;

public class WalletResponseDTO {

    private Long walletId;
    private String walletName;
    private BigDecimal walletBalance;

    public WalletResponseDTO() {}

    public WalletResponseDTO(Wallet wallet) {
        this.walletId = wallet.getId();
        this.walletName = wallet.getWalletName();
        this.walletBalance = wallet.getBalance();
    }

    // getters and setters

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
