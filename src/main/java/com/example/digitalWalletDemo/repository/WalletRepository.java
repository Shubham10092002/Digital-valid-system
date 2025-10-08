package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId);
}
