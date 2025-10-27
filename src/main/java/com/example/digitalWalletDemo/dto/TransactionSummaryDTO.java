package com.example.digitalWalletDemo.dto;

import java.math.BigDecimal;
import com.example.digitalWalletDemo.model.Transaction;

public record TransactionSummaryDTO(
        Transaction.Type type,
        BigDecimal totalAmount
) {}
