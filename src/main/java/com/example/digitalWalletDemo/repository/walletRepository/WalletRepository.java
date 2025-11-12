package com.example.digitalWalletDemo.repository.walletRepository;

import com.example.digitalWalletDemo.dto.walletdto.WalletBalanceDTO;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface  WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserId(Long userId);

    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = :userId")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
    // Optional<Wallet> findByWalletNumber(String walletNumber);


    @Query("SELECT new com.example.digitalWalletDemo.dto.walletdto.WalletBalanceDTO(w.id, w.walletName, w.balance) " +
            "FROM Wallet w WHERE w.user.id = :userId")
    List<WalletBalanceDTO> getWalletBalancesByUserId(@Param("userId") Long userId);
}
