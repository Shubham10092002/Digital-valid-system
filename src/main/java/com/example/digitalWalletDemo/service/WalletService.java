package com.example.digitalWalletDemo.service;

import com.example.digitalWalletDemo.config.WalletConfig;
import com.example.digitalWalletDemo.data.WalletOperationResult;
import com.example.digitalWalletDemo.dto.TransactionSummaryDTO;
import com.example.digitalWalletDemo.dto.WalletBalanceDTO;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.OptimisticLockException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletConfig walletConfig;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository, WalletConfig walletConfig) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.walletConfig = walletConfig;
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }


    public Optional<Wallet> getWalletById(Long id) {
        return walletRepository.findById(id);
    }

    public List<WalletBalanceDTO> getWalletBalancesByUser(Long userId) {
        return walletRepository.getWalletBalancesByUserId(userId);
    }

    public BigDecimal getTotalBalanceByUser(Long userId) {
        BigDecimal totalBalance = walletRepository.getTotalBalanceByUserId(userId);
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    public List<TransactionSummaryDTO> getTransactionSummary(Long walletId) {
        List<Object[]> results = transactionRepository.getTransactionSumsByType(walletId);
        return results.stream()
                .map(r -> new TransactionSummaryDTO((Transaction.Type) r[0], (BigDecimal) r[1]))
                .collect(Collectors.toList());
    }
//
//@Transactional
//    public WalletOperationResult credit(Long walletId, BigDecimal amount, String description) {
//        try {
//            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
//                return new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be greater than zero.");
//            }
//
//            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
//            if (walletOpt.isEmpty()) {
//                return new WalletOperationResult.Failure("WALLET_NOT_FOUND", "Wallet ID " + walletId + " not found.");
//            }
//
//            Wallet wallet = walletOpt.get();
//            if (amount.compareTo(walletConfig.getMaxCreditLimit()) > 0) {
//                return new WalletOperationResult.Failure("LIMIT_EXCEEDED", "Amount exceeds credit limit.");
//            }
//
//            wallet.setBalance(wallet.getBalance().add(amount));
//            walletRepository.save(wallet);
//
//            Transaction transaction = new Transaction(wallet, amount, Transaction.Type.CREDIT, description);
//            transactionRepository.save(transaction);
//
//            return new WalletOperationResult.Success("New Balance: " + wallet.getBalance().setScale(2));
//        } catch (Exception e) {
//            return new WalletOperationResult.Failure("UNKNOWN_ERROR", "An unexpected error occurred: " + e.getMessage());
//        }
//    }

    @Transactional
    public WalletOperationResult credit(Long walletId, BigDecimal amount, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be greater than zero.");
            }

            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
            if (walletOpt.isEmpty()) {
                return new WalletOperationResult.Failure("WALLET_NOT_FOUND", "Wallet ID " + walletId + " not found.");
            }

            Wallet wallet = walletOpt.get();

            if (amount.compareTo(walletConfig.getMaxCreditLimit()) > 0) {
                return new WalletOperationResult.Failure("LIMIT_EXCEEDED", "Amount exceeds credit limit.");
            }

            wallet.setBalance(wallet.getBalance().add(amount));
            walletRepository.save(wallet);

            transactionRepository.save(new Transaction(wallet, amount, Transaction.Type.CREDIT, description));

            return new WalletOperationResult.Success("New Balance: " + wallet.getBalance().setScale(2));
        } catch (OptimisticLockException e) {
            return new WalletOperationResult.Failure(
                    "CONFLICT",
                    "Wallet was updated by another transaction. Please retry."
            );
        } catch (Exception e) {
            return new WalletOperationResult.Failure(
                    "UNKNOWN_ERROR",
                    "An unexpected error occurred: " + e.getMessage()
            );
        }
    }


//    public WalletOperationResult debit(Long walletId, BigDecimal amount, String description) {
//        try {
//            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
//                return new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be greater than zero.");
//            }
//
//            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
//            if (walletOpt.isEmpty()) {
//                return new WalletOperationResult.Failure("WALLET_NOT_FOUND", "Wallet ID " + walletId + " not found.");
//            }
//
//            Wallet wallet = walletOpt.get();
//            if (amount.compareTo(walletConfig.getMaxDebitLimit()) > 0) {
//                return new WalletOperationResult.Failure("LIMIT_EXCEEDED", "Amount exceeds debit limit.");
//            }
//
//            if (wallet.getBalance().compareTo(amount) < 0) {
//                return new WalletOperationResult.Failure("INSUFFICIENT_FUNDS", "Not enough balance.");
//            }
//
//            wallet.setBalance(wallet.getBalance().subtract(amount));
//            walletRepository.save(wallet);
//
//            Transaction transaction = new Transaction(wallet, amount, Transaction.Type.DEBIT, description);
//            transactionRepository.save(transaction);
//
//            return new WalletOperationResult.Success("New Balance: " + wallet.getBalance().setScale(2));
//        } catch (Exception e) {
//            return new WalletOperationResult.Failure("UNKNOWN_ERROR", "An unexpected error occurred: " + e.getMessage());
//        }
//    }


    @Transactional
    public WalletOperationResult debit(Long walletId, BigDecimal amount, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be greater than zero.");
            }

            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
            if (walletOpt.isEmpty()) {
                return new WalletOperationResult.Failure("WALLET_NOT_FOUND", "Wallet ID " + walletId + " not found.");
            }

            Wallet wallet = walletOpt.get();

            if (amount.compareTo(walletConfig.getMaxDebitLimit()) > 0) {
                return new WalletOperationResult.Failure("LIMIT_EXCEEDED", "Amount exceeds debit limit.");
            }

            if (wallet.getBalance().compareTo(amount) < 0) {
                return new WalletOperationResult.Failure("INSUFFICIENT_FUNDS", "Not enough balance.");
            }

            wallet.setBalance(wallet.getBalance().subtract(amount));
            walletRepository.save(wallet);

            transactionRepository.save(new Transaction(wallet, amount, Transaction.Type.DEBIT, description));

            return new WalletOperationResult.Success("New Balance: " + wallet.getBalance().setScale(2));
        } catch (OptimisticLockException e) {
            return new WalletOperationResult.Failure(
                    "CONFLICT",
                    "Wallet was updated by another transaction. Please retry."
            );
        } catch (Exception e) {
            return new WalletOperationResult.Failure(
                    "UNKNOWN_ERROR",
                    "An unexpected error occurred: " + e.getMessage()
            );
        }
    }



    public WalletOperationResult getBalance(Long walletId) {
        try {
            Optional<Wallet> walletOpt = walletRepository.findById(walletId);
            if (walletOpt.isEmpty()) {
                return new WalletOperationResult.Failure("WALLET_NOT_FOUND", "Wallet ID " + walletId + " not found.");
            }

            Wallet wallet = walletOpt.get();
            return new WalletOperationResult.Balance(walletId, wallet.getBalance().setScale(2).toPlainString());
        } catch (Exception e) {
            return new WalletOperationResult.Failure("UNKNOWN_ERROR", "An unexpected error occurred: " + e.getMessage());
        }
    }
}
