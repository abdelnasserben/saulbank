package com.dabel.service.transaction;

import com.dabel.app.Checker;
import com.dabel.app.Fee;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountOperationService;
import com.dabel.service.account.AccountService;
import com.dabel.service.fee.FeeService;
import org.springframework.stereotype.Service;

@Service
public class Withdraw extends Transaction {

    private final FeeService feeService;

    public Withdraw(FeeService feeService, TransactionService transactionService, AccountService accountService, AccountOperationService accountOperationService) {
        super(transactionService, accountService, accountOperationService);
        this.feeService = feeService;
    }

    @Override
    public void init(TransactionDto transactionDto) {


        if(Checker.isInactiveAccount(transactionDto.getInitiatorAccount()))
            throw new IllegalOperationException("Account must be active");

        //TODO: for withdraw, debit account is the initiator account of transaction so we interchange nothing, we set only the receiver
        AccountDto receiverAccount = accountService.findVault(transactionDto.getBranch(), transactionDto.getCurrency());

        //TODO: set receiver account of transaction
        transactionDto.setReceiverAccount(receiverAccount);

        if(transactionDto.getSourceType().equals(SourceType.ONLINE.name()) && transactionDto.getInitiatorAccount().getBalance() < transactionDto.getAmount() + Bank.Fees.Withdraw.ONLINE) {

            transactionDto.setStatus(Status.FAILED.code());
            transactionDto.setFailureReason("Insufficient balance");
            transactionService.save(transactionDto);

            throw new BalanceInsufficientException();
        }

        transactionDto.setStatus(Status.PENDING.code());
        transactionService.save(transactionDto);
    }

    @Override
    public void approve(TransactionDto transactionDto) {

        if(!transactionDto.getStatus().equals(Status.PENDING.code()))
            return;

        //TODO: debit and credit accounts
        accountOperationService.debit(transactionDto.getInitiatorAccount(), transactionDto.getAmount());
        accountOperationService.credit(transactionDto.getReceiverAccount(), transactionDto.getAmount());

        //TODO: apply withdraw fees
        Fee fee = new Fee(transactionDto.getBranch(), Bank.Fees.Withdraw.ONLINE, "Withdraw");
        feeService.apply(transactionDto.getInitiatorAccount(), LedgerType.WITHDRAW, fee);

        //TODO: update transaction info and save it
        transactionDto.setStatus(Status.APPROVED.code());
        //we'll set updatedBy later...

        transactionService.save(transactionDto);
    }


    @Override
    public TransactionType getType() {
        return TransactionType.WITHDRAW;
    }
}
