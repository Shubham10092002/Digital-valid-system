package com.example.digitalWalletDemo.exception;

import com.example.digitalWalletDemo.exception.WalletIdNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTests {

    @Test
    void testWalletIdNotFoundExceptionMessage() {
        WalletIdNotFoundException ex = new WalletIdNotFoundException("Wallet not found");
        assertEquals("Wallet not found", ex.getMessage());
    }
}