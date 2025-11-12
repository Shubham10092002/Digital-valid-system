package com.example.digitalWalletDemo.dto.transactiondto;

import java.math.BigDecimal;
import com.example.digitalWalletDemo.model.transactionModel.Transaction;

public record TransactionSummaryDTO(
        Transaction.Type type,
        BigDecimal totalAmount
) {}
