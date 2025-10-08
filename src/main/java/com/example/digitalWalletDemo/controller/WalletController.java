package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.data.WalletOperationResult;
import com.example.digitalWalletDemo.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // ================== CREDIT money ==================
    @PostMapping("/{walletId}/credit")
    public ResponseEntity<WalletOperationResult> creditWallet(
            @PathVariable Long walletId,
            @RequestParam BigDecimal amount,
            @RequestParam String description
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be positive"));
        }
        WalletOperationResult result = walletService.credit(walletId, amount, description);
        return ResponseEntity.ok(result);
    }

    // ================== DEBIT money ==================
    @PostMapping("/{walletId}/debit")
    public ResponseEntity<WalletOperationResult> debitWallet(
            @PathVariable Long walletId,
            @RequestParam BigDecimal amount,
            @RequestParam String description
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be positive"));
        }
        WalletOperationResult result = walletService.debit(walletId, amount, description);
        return ResponseEntity.ok(result);
    }

    // ================== GET wallet balance ==================
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<WalletOperationResult> getBalance(@PathVariable Long walletId) {
        WalletOperationResult result = walletService.getBalance(walletId);
        return ResponseEntity.ok(result);
    }

    // ================== TRANSFER money ==================
    @PostMapping("/transfer")
    public ResponseEntity<WalletOperationResult> transfer(
            @RequestParam Long fromWalletId,
            @RequestParam Long toWalletId,
            @RequestParam BigDecimal amount,
            @RequestParam String description
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(new WalletOperationResult.Failure("INVALID_AMOUNT", "Amount must be positive"));
        }

        if (fromWalletId.equals(toWalletId)) {
            return ResponseEntity.badRequest()
                    .body(new WalletOperationResult.Failure("INVALID_TRANSFER", "Cannot transfer to the same wallet"));
        }

        // Debit from source wallet
        WalletOperationResult debitResult = walletService.debit(fromWalletId, amount, description);
        if (debitResult instanceof WalletOperationResult.Failure) {
            return ResponseEntity.badRequest().body(debitResult);
        }

        // Credit to destination wallet
        WalletOperationResult creditResult = walletService.credit(toWalletId, amount, description);
        if (creditResult instanceof WalletOperationResult.Failure) {
            // Rollback debit if credit fails
            walletService.credit(fromWalletId, amount, "Rollback of failed transfer");
            return ResponseEntity.badRequest().body(creditResult);
        }

        return ResponseEntity.ok(new WalletOperationResult.Success(
                ((WalletOperationResult.Success) creditResult).transactionId(),
                "Amount transferred successfully"
        ));
    }
}
