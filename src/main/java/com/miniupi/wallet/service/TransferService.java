package com.miniupi.wallet.service;

import com.miniupi.wallet.entity.Transaction;
import com.miniupi.wallet.entity.TransactionStatus;
import com.miniupi.wallet.entity.Wallet;
import com.miniupi.wallet.exception.DuplicateTransactionException;
import com.miniupi.wallet.exception.InsufficientBalanceException;
import com.miniupi.wallet.exception.WalletNotFoundException;
import com.miniupi.wallet.repository.TransactionRepository;
import com.miniupi.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction transferFunds(Long senderWalletId, Long receiverWalletId, BigDecimal amount, String idempotencyKey) {

        // 1. Idempotency Check
        if(transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateTransactionException("Duplicate request. Transaction already processed for key: " + idempotencyKey);
        }

        // 2. Prevent sending money to oneself
        if(senderWalletId.equals(receiverWalletId)) {
            throw new IllegalArgumentException("Sender and receiver wallets cannot be the same");
        }

        // 3. Deadlock Prevention: Always acquire the locks in consistent order
        Long firstLockId = Math.min(senderWalletId, receiverWalletId);
        Long secondLockId = Math.max(senderWalletId, receiverWalletId);

        // 4. Acquire Pessimistic Locks (Throw 404 if not found)
        walletRepository.findByIdForUpdate(firstLockId).orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + firstLockId));
        walletRepository.findByIdForUpdate(secondLockId).orElseThrow(() -> new WalletNotFoundException(" Wallet not found with ID: " + secondLockId));

        // Fetch the actual sender and receiver entities now that locks are held
        Wallet sender = walletRepository.findById(senderWalletId).get();
        Wallet receiver = walletRepository.findById(receiverWalletId).get();

        // 5. Business Validation (Throws 400 if insufficient funds)
        if(sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Transfer failed: Insufficient balance in wallet ID " + senderWalletId);
        }

        // 6. Execute the Transfer
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        // 7. Record the Immutable Ledger
        Transaction transaction = Transaction.builder()
                .senderWallet(sender)
                .receiverWallet(receiver)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .build();

        return transactionRepository.save(transaction);
    }
}
