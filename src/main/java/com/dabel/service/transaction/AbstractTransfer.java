package com.dabel.service.transaction;

import com.dabel.constant.Status;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;

public abstract class AbstractTransfer extends OwnerBasedTransaction {

    public AbstractTransfer(TransactionService transactionService, AccountService accountService, CustomerService customerService) {
        super(transactionService, accountService, customerService);
    }

    @Override
    public void init(TransactionDto transactionDto) {

        validateActiveAccount(transactionDto);
        validateCurrencyMatch(transactionDto);
        validateIsNotSelfTransfer(transactionDto);
        validateTransactionOwnership(transactionDto);

        setInitiator(transactionDto);

        if(isInsufficientBalance(transactionDto)) {
            updateTransactionStatus(transactionDto, Status.FAILED.code(), "Insufficient balance", false);
            throw new BalanceInsufficientException();
        }

        transactionDto.setStatus(Status.PENDING.code());
        transactionService.save(transactionDto);
    }

    private boolean isInsufficientBalance(TransactionDto transactionDto) {
        return transactionDto.getInitiatorAccount().getBalance() < transactionDto.getAmount();
    }

    @Override
    public void approve(TransactionDto transactionDto) {

        if(isNotPendingStatus(transactionDto)) return;

        adjustInitiatorAndBeneficiaryAccounts(transactionDto);

        updateTransactionStatus(transactionDto, Status.APPROVED.code(), "Approved", true);
    }

    private void validateIsNotSelfTransfer(TransactionDto transactionDto) {
        if(transactionDto.getInitiatorAccount().getAccountNumber().equals(transactionDto.getReceiverAccount().getAccountNumber())) {
            throw new IllegalOperationException("Can't make self transfer");
        }
    }
}
