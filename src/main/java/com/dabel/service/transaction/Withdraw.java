package com.dabel.service.transaction;

import com.dabel.app.Fee;
import com.dabel.app.Helper;
import com.dabel.constant.BankFees;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;
import com.dabel.service.fee.FeeService;
import org.springframework.stereotype.Service;

@Service
public class Withdraw extends Transaction {

    private final FeeService feeService;
    private final CustomerService customerService;

    public Withdraw(FeeService feeService, TransactionService transactionService, CustomerService customerFacadeService, AccountService accountService) {
        super(transactionService, accountService);
        this.feeService = feeService;
        this.customerService = customerFacadeService;
    }

    @Override
    public void init(TransactionDto transactionDto) {


        if(!Helper.isActiveStatedObject(transactionDto.getInitiatorAccount()))
            throw new IllegalOperationException("Account must be active");

        if(!transactionDto.getInitiatorAccount().getCurrency().equals(transactionDto.getCurrency()))
            throw new IllegalOperationException(String.format("The transaction currency must match that of the account (%s)", transactionDto.getInitiatorAccount().getCurrency()));

        //TODO: check if initiator customer is affiliate on the account
        CustomerDto customerDto = customerService.findByIdentity(transactionDto.getCustomerIdentity());
        accountService.findTrunk(customerDto, transactionDto.getInitiatorAccount().getAccountNumber());

        //TODO: for withdraw, debit account is the initiator account of transaction so we interchange nothing, we set only the receiver
        AccountDto receiverAccount = this.accountService.findVault(transactionDto.getBranch(), transactionDto.getCurrency());

        //TODO: set receiver account of transaction and initiator
        transactionDto.setReceiverAccount(receiverAccount);
        transactionDto.setInitiatedBy(currentUsername());

        if(transactionDto.getInitiatorAccount().getBalance() < transactionDto.getAmount() + BankFees.Basic.WITHDRAW) {

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
        accountService.debit(transactionDto.getInitiatorAccount(), transactionDto.getAmount());
        accountService.credit(transactionDto.getReceiverAccount(), transactionDto.getAmount());

        //TODO: apply withdraw fees
        Fee fee = new Fee(transactionDto.getBranch(), BankFees.Basic.WITHDRAW, "Withdraw");
        feeService.apply(transactionDto.getInitiatorAccount(), LedgerType.WITHDRAW, fee);

        //TODO: update transaction info and save it
        transactionDto.setStatus(Status.APPROVED.code());
        transactionDto.setFailureReason("Approved");
        transactionDto.setUpdatedBy(currentUsername());

        transactionService.save(transactionDto);
    }


    @Override
    public TransactionType getType() {
        return TransactionType.WITHDRAW;
    }
}
