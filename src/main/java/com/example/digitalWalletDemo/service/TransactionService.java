package com.example.digitalWalletDemo.service;

import com.example.digitalWalletDemo.dto.TransactionDTO;
import com.example.digitalWalletDemo.exception.WalletIdNotFoundException;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    /**
     * Fetch all transactions in the system.
     */
    public List<TransactionDTO> getAllTransactions() {
        logger.info("Fetching all transactions");
        return transactionRepository.findAll()
                .stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get all transactions for a specific wallet.
     */
    public List<TransactionDTO> getTransactionsByWallet(Long walletId) {
        logger.info("Fetching transactions for wallet ID {}", walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletIdNotFoundException("Wallet ID not found: " + walletId));

        return transactionRepository.findByWalletId(walletId)
                .stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get transactions for a specific user between two dates, optionally filtered by type.
     */
    public Object getUserTransactions(Long userId, String start, String end, String type) {
        logger.info("Fetching transactions for user {} from {} to {} with type {}", userId, start, end, type);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDateTime startDate = LocalDate.parse(start, formatter).atStartOfDay();
            LocalDateTime endDate = LocalDate.parse(end, formatter).atTime(23, 59, 59);

            Transaction.Type transactionType = null;
            if (type != null) {
                transactionType = Transaction.Type.valueOf(type.toUpperCase());
            }

            return transactionRepository.findUserTransactionsBetweenDates(userId, startDate, endDate, transactionType)
                    .stream()
                    .map(TransactionDTO::new)
                    .collect(Collectors.toList());

        } catch (DateTimeParseException e) {
            logger.error("Invalid date format provided: start={}, end={}", start, end, e);
            return "Invalid date format. Please use dd-MM-yyyy.";
        } catch (IllegalArgumentException e) {
            logger.error("Invalid transaction type provided: {}", type, e);
            return "Invalid transaction type. Use CREDIT, DEBIT, or TRANSFER.";
        }
    }
}
