package com.miniupi.wallet.repository;

/*
The WalletRepository is the core of your concurrency strategy.
We need a standard fetch for viewing balances, but we need a locked fetch for when
we are actively moving money.
*/

import com.miniupi.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Standard lookup (No lock - fast, used for viewing balance)
    Optional<Wallet> findByUserId(Long userId);

    // ==========================================
    // CONCURRENCY CONTROL: PESSIMISTIC LOCKING
    // ==========================================

    // When a thread calls this, PostgreSQL will execute a "SELECT ... FOR UPDATE".
    // If User A tries to transfer money twice simultaneously, the first thread gets the lock.
    // The second thread MUST WAIT at this exact line until the first thread finishes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);
}
