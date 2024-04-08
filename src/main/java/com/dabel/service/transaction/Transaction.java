package com.dabel.service.transaction;

import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.TransactionDto;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.account.AccountFacadeService;

public abstract class Transaction implements EvaluableOperation<TransactionDto> {

    protected TransactionService transactionService;
    protected AccountFacadeService accountFacadeService;

    public Transaction(TransactionService transactionService, AccountFacadeService accountFacadeService) {
        this.transactionService = transactionService;
        this.accountFacadeService = accountFacadeService;
    }

    @Override
    public void reject(TransactionDto transactionDto, String remarks) {
        if(!transactionDto.getStatus().equals(Status.PENDING.code()))
            return;

        transactionDto.setStatus(Status.REJECTED.code());
        transactionDto.setFailureReason(remarks);
        //we'll set updatedBy later...

        transactionService.save(transactionDto);
    }

    abstract TransactionType getType();
}
