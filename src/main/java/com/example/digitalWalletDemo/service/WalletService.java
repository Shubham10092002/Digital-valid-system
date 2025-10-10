package com.example.digitalWalletDemo.service;


import com.example.digitalWalletDemo.data.WalletOperationResult;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import com.example.digitalWalletDemo.config.WalletConfig;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletConfig walletConfig;

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository,
                         TransactionRepository transactionRepository,
                         WalletConfig walletConfig) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.walletConfig = walletConfig;
    }


    @Transactional
    public WalletOperationResult credit(Long walletId, BigDecimal amount, String description) {
        log.info("Credit request: walletId={}, amount={}, description={}", walletId, amount, description);

        try {
            if (amount == null) {
                log.warn("Credit failed: amount is null for walletId={}", walletId);
                return new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount cannot be null");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Credit failed: negative or zero amount={} for walletId={}", amount, walletId);
                return new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be positive");
            }

            BigDecimal limit = walletConfig.getMaxCreditLimit();
            if (amount.compareTo(limit) > 0) {
                log.warn("Credit failed: amount={} exceeds limit for walletId={}", amount, walletId);
                return new WalletOperationResult.Failure("LIMIT_EXCEEDED",
                        "Credit amount cannot exceed $" + limit);
            }

            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
            if (walletOpt.isEmpty()) {
                log.warn("Credit failed: wallet not found for walletId={}", walletId);
                return new WalletOperationResult.Failure("WALLET_NOT_FOUND", "Wallet not found");
            }

            Wallet wallet = walletOpt.get();
            wallet.setBalance(wallet.getBalance().add(amount));

            Transaction transaction = new Transaction(wallet, amount, Transaction.Type.CREDIT, description);
            transactionRepository.save(transaction);
            walletRepository.save(wallet);

            log.info("Credit success: walletId={}, transactionId={}, newBalance={}",
                    walletId, transaction.getId(), wallet.getBalance());
            return new WalletOperationResult.Success(transaction.getId(),
                    "Amount credited successfully. New balance: " + formatBalance(wallet.getBalance()));
        } catch (Exception e) {
            log.error("Credit exception for walletId={}", walletId, e);
            return new WalletOperationResult.Failure("UNKNOWN_ERROR", e.getMessage());
        }
    }

    @Transactional
    public WalletOperationResult debit(Long walletId, BigDecimal amount, String description) {
        log.info("Debit request: walletId={}, amount={}, description={}", walletId, amount, description);
        try {
            if (amount == null) {
                log.warn("Debit failed: amount is null for walletId={}", walletId);
                return new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount cannot be null");
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Debit failed: negative or zero amount={} for walletId={}", amount, walletId);
                return new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be positive");
            }

            BigDecimal limit = walletConfig.getMaxDebitLimit();
            if (amount.compareTo(limit) > 0) {
                log.warn("Debit failed: amount={} exceeds limit for walletId={}", amount, walletId);
                return new WalletOperationResult.Failure("LIMIT_EXCEEDED",
                        "Debit amount cannot exceed $" + limit);
            }

            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
            if (walletOpt.isEmpty()) {
                log.warn("Debit failed: wallet not found for walletId={}", walletId);
                return new WalletOperationResult.Failure("WALLET_NOT_FOUND", "Wallet not found");
            }

            Wallet wallet = walletOpt.get();

            if (wallet.getBalance().compareTo(amount) < 0) {
                log.warn("Debit failed: insufficient funds. walletId={}, currentBalance={}, debitAmount={}",
                        walletId, wallet.getBalance(), amount);
                return new WalletOperationResult.Failure("INSUFFICIENT_FUNDS", "Insufficient balance");
            }

            wallet.setBalance(wallet.getBalance().subtract(amount));

            Transaction transaction = new Transaction(wallet, amount, Transaction.Type.DEBIT, description);
            transactionRepository.save(transaction);
            walletRepository.save(wallet);

            log.info("Debit success: walletId={}, transactionId={}, newBalance={}",
                    walletId, transaction.getId(), wallet.getBalance());
            return new WalletOperationResult.Success(transaction.getId(),
                    "Amount debited successfully. New balance: " + formatBalance(wallet.getBalance()));
        } catch (Exception e) {
            log.error("Debit exception for walletId={}", walletId, e);
            return new WalletOperationResult.Failure("UNKNOWN_ERROR", e.getMessage());
        }
    }

    public WalletOperationResult getBalance(Long walletId) {
        log.info("Get balance request: walletId={}", walletId);
        try {
            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
            if (walletOpt.isEmpty()) {
                log.warn("Get balance failed: wallet not found for walletId={}", walletId);
                return new WalletOperationResult.Failure("WALLET_NOT_FOUND", "Wallet not found");
            }

            Wallet wallet = walletOpt.get();
            log.info("Balance retrieved: walletId={}, balance={}", walletId, wallet.getBalance());
            return new WalletOperationResult.Balance(
                    wallet.getId(),
                    formatBalance(wallet.getBalance()),
                    "Balance retrieved successfully"
            );
        } catch (Exception e) {
            log.error("Get balance exception for walletId={}", walletId, e);
            return new WalletOperationResult.Failure("UNKNOWN_ERROR", e.getMessage());
        }
    }

    private String formatBalance(BigDecimal balance) {
        return balance.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
