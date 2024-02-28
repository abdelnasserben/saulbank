package com.dabel.service.transaction;

import com.dabel.exception.IllegalOperationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class TransactionContext {

    private final Map<String, Transaction> transactionMap = new HashMap<>();

    public TransactionContext(Set<Transaction> transactions) {
        transactions.forEach(type -> transactionMap.put(type.getType().name(), type));
    }

    public Transaction setContext(String transactionType) {
        return Optional.ofNullable(transactionMap.get(transactionType)).orElseThrow(() -> new IllegalOperationException("Unknown transaction type"));
    }
}
