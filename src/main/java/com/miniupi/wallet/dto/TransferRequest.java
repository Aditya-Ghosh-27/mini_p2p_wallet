package com.miniupi.wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Sender wallet ID is required")
        Long senderWalletId,

        @NotNull(message = "Receiver wallet ID is required")
        Long receiverWalletId,

        @NotNull(message = "Transfer amount is required")
        @Positive(message = "Transfer amount must be greater than zero")
        BigDecimal amount
) {}
