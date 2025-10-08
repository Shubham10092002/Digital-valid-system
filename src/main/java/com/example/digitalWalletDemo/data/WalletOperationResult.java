package com.example.digitalWalletDemo.data;
public sealed interface WalletOperationResult
        permits WalletOperationResult.Success, WalletOperationResult.Failure, WalletOperationResult.Balance {

    record Success(Long transactionId, String message) implements WalletOperationResult {}
    record Failure(String errorCode, String reason) implements WalletOperationResult {}
    record Balance(Long walletId, String balance, String message) implements WalletOperationResult {}
}

