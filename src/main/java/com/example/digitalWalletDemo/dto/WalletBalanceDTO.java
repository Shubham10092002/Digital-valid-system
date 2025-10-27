package com.example.digitalWalletDemo.dto;

import java.math.BigDecimal;

public record WalletBalanceDTO(
        Long walletId,
        String walletName,
        BigDecimal balance
) {}
