package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.TransactionDTO;
import com.example.digitalWalletDemo.exception.WalletIdNotFoundException;
import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionController(TransactionRepository transactionRepository,
                                 WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    // ------------------- Get all transactions -------------------
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionRepository.findAll()
                .stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    // ------------------- Get transactions by wallet -------------------
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByWallet(@PathVariable Long walletId) {
        // Throw exception if wallet not found
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletIdNotFoundException("Wallet ID not found: " + walletId));

        List<TransactionDTO> dtos = transactionRepository.findByWalletId(walletId)
                .stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos); // returns empty list if wallet exists but has no transactions
    }

    // ------------------- Get transactions by user filtered by date/type -------------------
    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<?> getTransactions(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String type
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDateTime startDate = LocalDate.parse(start, formatter).atStartOfDay();
            LocalDateTime endDate = LocalDate.parse(end, formatter).atTime(23, 59, 59);

            Transaction.Type transactionType = null;
            if (type != null) {
                transactionType = Transaction.Type.valueOf(type.toUpperCase());
            }

            List<TransactionDTO> dtos = transactionRepository
                    .findUserTransactionsBetweenDates(userId, startDate, endDate, transactionType)
                    .stream()
                    .map(TransactionDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid date format. Please use dd-MM-yyyy.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid transaction type. Use CREDIT, DEBIT, or TRANSFER.");
        }
    }
}
