// src/main/java/com/example/digitalWalletDemo/data/WalletOperationResult.java

package com.example.digitalWalletDemo.data;

/**
 * A Sealed Interface/Class to represent the result of a wallet operation.
 * It permits only the defined Success and Failure records.
 */
public sealed interface WalletOperationResult permits WalletOperationResult.Success, WalletOperationResult.Failure {

    // Record for a successful operation
    record Success(Long transactionId, String message) implements WalletOperationResult {}

    // Record for a failed operation
    record Failure(String errorCode, String reason) implements WalletOperationResult {}
}