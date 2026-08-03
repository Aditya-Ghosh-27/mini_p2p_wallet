package com.miniupi.wallet.repository;

// This repository handles the passbook/ledger functionality and idempotency checks.

import com.miniupi.wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 1. Passbook feature: Fetch all transactions for a specific wallet.
    // Notice we use Pageable here. If a user has 10,000 transactions,
    // returning them all at once will crash your server. Pagination is a must.
    Page<Transaction> findBySenderWalletIdOrReceiverWalletIdOrderByTimestampDesc(
            Long senderId, Long receiverId, Pageable pageable);


    // 2. Idempotency Check: Before processing a transfer, we check if this key exists.
    // If it does, we know the frontend accidentally sent a duplicate request.
    boolean existsByIdempotencyKey(String idempotencyKey);


    // Alternative approach to Idempotency: Return the transaction if it exists
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
