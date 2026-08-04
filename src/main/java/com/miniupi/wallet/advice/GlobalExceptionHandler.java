package com.miniupi.wallet.advice;

/*
    We need to handle our custom business exceptions, but we also need to handle
    MethodArgumentNotValidException—which is what Spring throws when a user violates
    the @Positive or @NotNull constraints we added to the TransferRequest.
*/

import com.miniupi.wallet.dto.ErrorResponse;
import com.miniupi.wallet.exception.DuplicateTransactionException;
import com.miniupi.wallet.exception.InsufficientBalanceException;
import com.miniupi.wallet.exception.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle Business Exceptions
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTransaction(DuplicateTransactionException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONTINUE.value(),
                "Conflict",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 2. Handle DTO Validation Errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String>  errors = new HashMap<>();

        // Extracts the specific field that failed and its custom error message
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 3. Fallback for all other unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred.", // Don't expose ex.getMessage() here to avoid leaking sensitive data
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


/*

    Without this class, if a user tried to send -50 rupees, Spring would throw a massive HTML
    stack trace at the frontend.

    With this @RestControllerAdvice in place, if we send -50, the
    handleValidationExceptions method intercepts it and returns this clean JSON:

    `
        {
            "amount": "Transfer amount must be greater than zero"
        }
    `


    If we hit a network snag and our idempotency check catches a duplicate request,
    we get this exact, parseable response:

    `
        {
            "status": 409,
            "error": "Conflict",
            "message": "Duplicate request. Transaction already processed for key: req_12345",
            "timestamp": "2026-08-04T10:15:30.12345"
        }
    `

*/