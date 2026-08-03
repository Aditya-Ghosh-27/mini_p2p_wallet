package com.miniupi.wallet.entity;

/*
    This entity acts as an immutable ledger. It tracks the movement of money & stores the
   `idempotency_keys` to prevent duplicate network retries from charging the user twice.
 */

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id")
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_wallet_id", nullable = false)
    private Wallet receiverWallet;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    // Automatically records the exact time, the row is inserted
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    // Ensures we don't process the same payment request twice
    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;
}
