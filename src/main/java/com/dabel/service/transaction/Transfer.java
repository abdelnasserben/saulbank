package com.dabel.service.transaction;

import com.dabel.app.Checker;
import com.dabel.app.CurrencyExchanger;
import com.dabel.app.Fee;
import com.dabel.constant.Bank;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountOperationService;
import com.dabel.service.account.AccountService;
import com.dabel.service.fee.FeeService;
import org.springframework.stereotype.Service;

@Service
public class Transfer extends Transaction{

    private final FeeService feeService;

    public Transfer(FeeService feeService, TransactionService transactionService, AccountService accountService, AccountOperationService accountOperationService) {
        super(transactionService, accountService, accountOperationService);
        this.feeService = feeService;
    }

    @Override
    public void init(TransactionDto transactionDto) {

        if(Checker.isInactiveAccount(transactionDto.getInitiatorAccount()))
            throw new IllegalOperationException("Initiator account must be active");

        if(transactionDto.getInitiatorAccount().getBalance() < transactionDto.getAmount() + Bank.Fees.Transfer.ONLINE) {

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

        //TODO: exchange amount in given currency
        double creditAmount = CurrencyExchanger.exchange(transactionDto.getInitiatorAccount().getCurrency(), transactionDto.getReceiverAccount().getCurrency(), transactionDto.getAmount());

        accountOperationService.debit(transactionDto.getInitiatorAccount(), transactionDto.getAmount());
        accountOperationService.credit(transactionDto.getReceiverAccount(), creditAmount);

        //TODO: apply transfer fees
        Fee fee = new Fee(transactionDto.getBranch(), Bank.Fees.Transfer.ONLINE, "Transfer");
        feeService.apply(transactionDto.getInitiatorAccount(), LedgerType.TRANSFER, fee);

        //TODO: update transaction info and save it
        transactionDto.setStatus(Status.APPROVED.code());
        //we'll set updatedBy later...

        transactionService.save(transactionDto);
    }

    @Override
    TransactionType getType() {
        return TransactionType.TRANSFER;
    }
}
