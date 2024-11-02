package com.dabel.service.transaction;

import com.dabel.dto.CustomerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;

public abstract class OwnerBasedTransaction extends Transaction {

    private final CustomerService customerService;

    public OwnerBasedTransaction(TransactionService transactionService, AccountService accountService, CustomerService customerService) {
        super(transactionService, accountService);
        this.customerService = customerService;
    }

    protected void validateCurrencyMatch(TransactionDto transactionDto) {
        if (!transactionDto.getInitiatorAccount().getCurrency().equals(transactionDto.getCurrency())) {
            throw new IllegalOperationException(String.format("Currency mismatch. Expected: %s", transactionDto.getInitiatorAccount().getCurrency()));
        }
    }

    protected void validateTransactionOwnership(TransactionDto transactionDto) {
        CustomerDto customerDto = customerService.findByIdentity(transactionDto.getCustomerIdentity());
        accountService.findTrunkByCustomerAndAccountNumber(customerDto, transactionDto.getInitiatorAccount().getAccountNumber());
    }
}
