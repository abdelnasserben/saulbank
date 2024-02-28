package com.dabel.exception;

public class BalanceInsufficientException extends RuntimeException {
    public BalanceInsufficientException() {
        super("Account balance is insufficient");
    }

    public BalanceInsufficientException(String message) {
        super(message);
    }
}
