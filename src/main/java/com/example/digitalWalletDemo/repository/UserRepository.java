package com.example.digitalWalletDemo.repository;

import com.example.digitalWalletDemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.wallets w " +
            "WHERE w.balance > :threshold")
    List<User> findUsersWithWalletBalanceGreaterThan(@Param("threshold") BigDecimal threshold);
}
