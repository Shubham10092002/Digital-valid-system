package com.example.digitalWalletDemo.controller;

import com.example.digitalWalletDemo.data.WalletOperationResult;
import com.example.digitalWalletDemo.dto.CreateWalletDTO;
import com.example.digitalWalletDemo.dto.TransactionSummaryDTO;
import com.example.digitalWalletDemo.dto.WalletBalanceDTO;
import com.example.digitalWalletDemo.exception.UserNotFoundException;
import com.example.digitalWalletDemo.model.User;
import com.example.digitalWalletDemo.model.Wallet;
import com.example.digitalWalletDemo.repository.UserRepository;
import com.example.digitalWalletDemo.repository.WalletRepository;
import com.example.digitalWalletDemo.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private WalletRepository walletRepository;
    private UserRepository userRepository;

    public WalletController(WalletService walletService, WalletRepository walletRepository, UserRepository userRepository) {
        this.walletService = walletService;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }



    // ---------- GET ALL WALLETS ----------
    @GetMapping
    public ResponseEntity<?> getAllWallets() {
        try {
            var wallets = walletService.getAllWallets();
            return ResponseEntity.ok(wallets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("errorCode", "UNKNOWN_ERROR",
                            "reason", "Failed to fetch wallets: " + e.getMessage())
            );
        }
    }

    //---------CREATE NEW WALLET------------
    @PostMapping("/user/{userId}/create-wallet")
    public ResponseEntity<?> createWalletForUser(
            @PathVariable Long userId,
            @RequestBody CreateWalletDTO walletDTO
    ) {
        // 1️⃣ Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2️⃣ Validate input
        if (walletDTO.getWalletName() == null || walletDTO.getWalletName().isEmpty()) {
            return ResponseEntity.badRequest().body("walletName cannot be empty");
        }

        if (walletDTO.getInitialBalance() == null) {
            walletDTO.setInitialBalance(BigDecimal.ZERO);
        }

        // 3️⃣ Create Wallet
        Wallet wallet = new Wallet();
        wallet.setWalletName(walletDTO.getWalletName());
        wallet.setBalance(walletDTO.getInitialBalance());
        wallet.setUser(user);

        walletRepository.save(wallet);

        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }



    // ---------- GET WALLETS BY USER ----------
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getWalletBalancesByUser(@PathVariable Long userId) {
        try {
            List<WalletBalanceDTO> balances = walletService.getWalletBalancesByUser(userId);
            if (balances.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "errorCode", "NOT_FOUND",
                        "reason", "No wallets found for user ID " + userId
                ));
            }
            return ResponseEntity.ok(balances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "errorCode", "UNKNOWN_ERROR",
                    "reason", "Failed to fetch balances: " + e.getMessage()
            ));
        }
    }

    // ---------- GET TOTAL BALANCE BY USER ----------
    @GetMapping("/user/{userId}/totalBalance")
    public ResponseEntity<?> getTotalBalanceByUser(@PathVariable Long userId) {
        BigDecimal totalBalance = walletService.getTotalBalanceByUser(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "totalBalance", totalBalance.setScale(2)
        ));
    }

    // ---------- GET TRANSACTION SUMMARY BY WALLET ----------
    @GetMapping("/{walletId}/transactions/summary")
    public ResponseEntity<List<TransactionSummaryDTO>> getTransactionSummary(@PathVariable Long walletId) {
        List<TransactionSummaryDTO> summary = walletService.getTransactionSummary(walletId);
        return ResponseEntity.ok(summary);
    }

    // ---------- CREDIT ----------
    @PostMapping("/{walletId}/credit")
    public ResponseEntity<?> creditWallet(
            @PathVariable Long walletId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", "INVALID_AMOUNT",
                    "reason", "Amount must be positive"
            ));
        }

        WalletOperationResult result = walletService.credit(walletId, amount, description);
        return buildResponse(result);
    }

    // ---------- DEBIT ----------
    @PostMapping("/{walletId}/debit")
    public ResponseEntity<?> debitWallet(
            @PathVariable Long walletId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", "INVALID_AMOUNT",
                    "reason", "Amount must be positive"
            ));
        }

        WalletOperationResult result = walletService.debit(walletId, amount, description);
        return buildResponse(result);
    }

    // ---------- GET BALANCE ----------
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Long walletId) {
        WalletOperationResult result = walletService.getBalance(walletId);
        return buildResponse(result);
    }

    // ---------- TRANSFER ----------
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestParam Long fromWalletId,
            @RequestParam Long toWalletId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {

        if (fromWalletId.equals(toWalletId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", "INVALID_TRANSFER",
                    "reason", "Cannot transfer to the same wallet"
            ));
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", "INVALID_AMOUNT",
                    "reason", "Amount must be positive"
            ));
        }

        WalletOperationResult debitResult = walletService.debit(fromWalletId, amount, description);
        if (debitResult instanceof WalletOperationResult.Failure failure) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", failure.errorCode(),
                    "reason", failure.reason()
            ));
        }

        walletService.credit(toWalletId, amount, description);

        return ResponseEntity.ok(Map.of("message", "Amount transferred successfully"));
    }

    // ---------- HELPER METHOD ----------
    private ResponseEntity<?> buildResponse(WalletOperationResult result) {
        if (result instanceof WalletOperationResult.Success success) {
            return ResponseEntity.ok(Map.of("message", success.message()));
        } else if (result instanceof WalletOperationResult.Balance balance) {
            return ResponseEntity.ok(Map.of(
                    "walletId", balance.walletId(),
                    "balance", balance.balance(),
                    "message", "Balance retrieved successfully"
            ));
        } else if (result instanceof WalletOperationResult.Failure failure) {
            return ResponseEntity.ok(Map.of(
                    "errorCode", failure.errorCode(),
                    "reason", failure.reason()
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                    "errorCode", "UNKNOWN_ERROR",
                    "reason", "Unexpected result type"
            ));
        }
    }
}
