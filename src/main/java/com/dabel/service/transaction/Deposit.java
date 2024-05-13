package com.dabel.service.transaction;

import com.dabel.app.CurrencyExchanger;
import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.AccountDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import org.springframework.stereotype.Service;

@Service
public class Deposit extends Transaction {

    public Deposit(TransactionService transactionService, AccountService accountService) {
        super(transactionService, accountService);
    }

    @Override
    public void init(TransactionDto transactionDto) {

        if(!Helper.isActiveStatedObject(transactionDto.getInitiatorAccount()))
            throw new IllegalOperationException("Account must be active");

        //TODO: for deposit, initiator account is the beneficiary account so we interchange initiator as the vault and beneficiary as receiver
        AccountDto initiatorAccount = accountService.findVault(transactionDto.getBranch(), transactionDto.getCurrency());
        AccountDto receiverAccount = transactionDto.getInitiatorAccount();

        //TODO: we update transaction details
        transactionDto.setInitiatorAccount(initiatorAccount);
        transactionDto.setReceiverAccount(receiverAccount);
        transactionDto.setStatus(Status.PENDING.code());
        transactionDto.setInitiatedBy(currentUsername());
        transactionService.save(transactionDto);
    }

    @Override
    public void approve(TransactionDto transactionDto) {

        if(!transactionDto.getStatus().equals(Status.PENDING.code()))
            return;

        //TODO: exchange amount in given currency
        double creditAmount = CurrencyExchanger.exchange(transactionDto.getCurrency(), transactionDto.getReceiverAccount().getCurrency(), transactionDto.getAmount());

        accountService.debit(transactionDto.getInitiatorAccount(), transactionDto.getAmount());
        accountService.credit(transactionDto.getReceiverAccount(), creditAmount);

        //TODO: update transactionDto info and save it
        transactionDto.setStatus(Status.APPROVED.code());
        transactionDto.setFailureReason("Approved");
        transactionDto.setUpdatedBy(currentUsername());

        transactionService.save(transactionDto);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.DEPOSIT;
    }
}
