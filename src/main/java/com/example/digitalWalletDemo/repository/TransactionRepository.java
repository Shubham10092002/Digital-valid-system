package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
        SELECT t FROM Transaction t
        JOIN t.wallet w
        WHERE w.user.id = :userId
        AND t.timestamp BETWEEN :startDate AND :endDate
        AND (:type IS NULL OR t.type = :type)
        ORDER BY t.timestamp DESC
        """)
    List<Transaction> findUserTransactionsBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") Transaction.Type type
    );

    List<Transaction> findByWalletId(Long walletId);

//    long findByWalletId(Long walletId);
    @Query("SELECT t.type, SUM(t.amount) FROM Transaction t WHERE t.wallet.id = :walletId GROUP BY t.type")
    List<Object[]> getTransactionSumsByType(@Param("walletId") Long walletId);

    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId ORDER BY t.timestamp DESC")
    List<Transaction> findTopTransactionsByWalletId(@Param("walletId") Long walletId);
}
