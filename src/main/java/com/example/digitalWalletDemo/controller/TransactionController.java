package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.model.Transaction;
import com.example.digitalWalletDemo.repository.TransactionRepository;
import com.example.digitalWalletDemo.dto.TransactionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByWallet(@PathVariable Long walletId) {
        logger.info("Fetching transactions for walletId={}", walletId);
        List<TransactionDTO> dtos = transactionRepository.findByWalletId(walletId)
                .stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
        logger.info("Found {} transactions for walletId={}", dtos.size(), walletId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        logger.info("Fetching all transactions");
        List<TransactionDTO> dtos = transactionRepository.findAll()
                .stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
        logger.info("Found {} total transactions", dtos.size());
        return ResponseEntity.ok(dtos);
    }
}
