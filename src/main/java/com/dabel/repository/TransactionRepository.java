package com.dabel.repository;

import com.dabel.model.Account;
import com.dabel.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByInitiatorAccountOrReceiverAccount(Account initiatorAccount, Account receiverAccount);
}
