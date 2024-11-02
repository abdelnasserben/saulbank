package com.dabel.service.transaction;

import com.dabel.app.CurrencyExchanger;
import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;
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

    protected void setInitiator(TransactionDto transactionDto) {
        transactionDto.setInitiatedBy(currentUsername());
    }

    protected void validateActiveAccount(TransactionDto transactionDto) {
        if (!Helper.isActiveStatedObject(transactionDto.getInitiatorAccount())) {
            throw new IllegalOperationException("Account must be active");
        }
    }

    protected boolean isNotPendingStatus(TransactionDto transactionDto) {
        return !transactionDto.getStatus().equals(Status.PENDING.code());
    }

    protected void updateTransactionStatus(TransactionDto transactionDto, String status, String reason, boolean isUpdated) {
        transactionDto.setStatus(status);
        transactionDto.setFailureReason(reason);
        transactionDto.setUpdatedBy(isUpdated ? currentUsername() : null);
        transactionService.save(transactionDto);
    }

    protected void adjustInitiatorAndBeneficiaryAccounts(TransactionDto transactionDto) {
        double creditAmount = CurrencyExchanger.exchange(transactionDto.getInitiatorAccount().getCurrency(), transactionDto.getReceiverAccount().getCurrency(), transactionDto.getAmount());

        accountService.debitAccount(transactionDto.getInitiatorAccount(), transactionDto.getAmount());
        accountService.creditAccount(transactionDto.getReceiverAccount(), creditAmount);
    }

    private String currentUsername() {
        return Helper.getAuthenticated().getName();
    }
}
