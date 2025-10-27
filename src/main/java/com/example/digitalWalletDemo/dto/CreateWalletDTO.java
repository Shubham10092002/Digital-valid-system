package com.example.digitalWalletDemo.dto;

import java.math.BigDecimal;

public class CreateWalletDTO {
    private String walletName;
    private BigDecimal initialBalance;

    // Getters and setters
    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
}
