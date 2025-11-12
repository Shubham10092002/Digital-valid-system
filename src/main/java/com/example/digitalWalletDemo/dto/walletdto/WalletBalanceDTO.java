package com.example.digitalWalletDemo.dto.walletdto;

import java.math.BigDecimal;

public record WalletBalanceDTO(
        Long walletId,
        String walletName,
        BigDecimal balance
) {}
