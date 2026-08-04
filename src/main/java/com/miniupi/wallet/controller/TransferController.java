package com.miniupi.wallet.controller;

import com.miniupi.wallet.dto.TransferRequest;
import com.miniupi.wallet.dto.TransferResponse;
import com.miniupi.wallet.entity.Transaction;
import com.miniupi.wallet.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transferFunds(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request
            ) {

        // 1. Call the service layer
        Transaction transaction = transferService.transferFunds(
                request.senderWalletId(),
                request.receiverWalletId(),
                request.amount(),
                idempotencyKey
        );


        // 2. Map the Entity to the Response DTO
        TransferResponse response = new TransferResponse(
                transaction.getId(),
                transaction.getSenderWallet().getId(),
                transaction.getReceiverWallet().getId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getTimestamp()
        );


        // 3. Return a 200 OK with the payload
        return ResponseEntity.ok(response);
    }
}
