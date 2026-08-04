package com.miniupi.wallet.dto;

import com.miniupi.wallet.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse (
    Long transactionId,
    Long senderWalletId,
    Long receiverWalletId,
    BigDecimal amount,
    TransactionStatus status,
    LocalDateTime timestamp
) {}
