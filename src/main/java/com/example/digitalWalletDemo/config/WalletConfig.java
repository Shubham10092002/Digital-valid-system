package com.example.digitalWalletDemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WalletConfig {

    @Value("${wallet.max-credit-limit}")
    private BigDecimal maxCreditLimit;

    @Value("${wallet.max-debit-limit}")
    private BigDecimal maxDebitLimit;

    public BigDecimal getMaxCreditLimit() {
        return maxCreditLimit;
    }

    public BigDecimal getMaxDebitLimit() {
        return maxDebitLimit;
    }
}
