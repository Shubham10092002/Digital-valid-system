package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.dto.TransactionDTO;
import com.example.digitalWalletDemo.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Get all transactions
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    // Get transactions by wallet ID
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByWallet(@PathVariable Long walletId) {
        return ResponseEntity.ok(transactionService.getTransactionsByWallet(walletId));
    }

    // Get transactions for a user between dates (optionally filtered by type)
    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<?> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String type
    ) {
        Object response = transactionService.getUserTransactions(userId, start, end, type);

        if (response instanceof String) {
            // return error message if invalid date/type
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
