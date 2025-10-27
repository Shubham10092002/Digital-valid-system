package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.dto.WalletBalanceDTO;
import com.example.digitalWalletDemo.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserId(Long userId);

    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = :userId")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
   // Optional<Wallet> findByWalletNumber(String walletNumber);



    @Query("SELECT new com.example.digitalWalletDemo.dto.WalletBalanceDTO(w.id, w.walletName, w.balance) " +
            "FROM Wallet w WHERE w.user.id = :userId")
    List<WalletBalanceDTO> getWalletBalancesByUserId(@Param("userId") Long userId);
}
