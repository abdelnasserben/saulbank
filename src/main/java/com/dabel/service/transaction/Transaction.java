package com.dabel.service.transaction;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.TransactionDto;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Transaction implements EvaluableOperation<TransactionDto> {

    protected TransactionService transactionService;
    protected AccountService accountService;

    @Autowired
    public Transaction(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @Override
    public void reject(TransactionDto transactionDto, String remarks) {
        if(!transactionDto.getStatus().equals(Status.PENDING.code()))
            return;

        transactionDto.setStatus(Status.REJECTED.code());
        transactionDto.setFailureReason(remarks);
        transactionDto.setUpdatedBy(currentUsername());

        transactionService.save(transactionDto);
    }

    abstract TransactionType getType();
    public String currentUsername() {
        return Helper.getAuthenticated().getName();
    }
}
