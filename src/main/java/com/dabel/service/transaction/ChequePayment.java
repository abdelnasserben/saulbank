package com.dabel.service.transaction;

import com.dabel.app.CurrencyExchanger;
import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.ChequeDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.cheque.ChequeService;
import com.dabel.service.customer.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class ChequePayment extends Transaction{

    private final ChequeService chequeService;
    private final CustomerService customerService;

    public ChequePayment(TransactionService transactionService, CustomerService customerService, AccountService accountService, ChequeService chequeService) {
        super(transactionService, accountService);
        this.customerService = customerService;
        this.chequeService = chequeService;
    }

    @Override
    public void init(TransactionDto transactionDto) {

        if(!Helper.isActiveStatedObject(transactionDto.getInitiatorAccount()))
            throw new IllegalOperationException("Initiator account must be active");

        if(!transactionDto.getInitiatorAccount().getCurrency().equals(transactionDto.getCurrency()))
            throw new IllegalOperationException(String.format("The transaction currency must match that of the account (%s)", transactionDto.getInitiatorAccount().getCurrency()));

        //TODO: check if initiator customer is affiliate on the account
        CustomerDto customerDto = customerService.findByIdentity(transactionDto.getCustomerIdentity());
        accountService.findTrunkByCustomerAndAccountNumber(customerDto, transactionDto.getInitiatorAccount().getAccountNumber());

        if(transactionDto.getInitiatorAccount().getAccountNumber().equals(transactionDto.getReceiverAccount().getAccountNumber()))
            throw new IllegalOperationException("Can't make self transfer");


        if(transactionDto.getInitiatorAccount().getBalance() < transactionDto.getAmount()) {

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

        accountService.debit(transactionDto.getInitiatorAccount(), transactionDto.getAmount());
        accountService.credit(transactionDto.getReceiverAccount(), creditAmount);

        //TODO: update transaction info and save it
        transactionDto.setStatus(Status.APPROVED.code());
        transactionDto.setFailureReason("Approved");
        //we'll set updatedBy later...

        transactionService.save(transactionDto);

        //TODO: save used cheque
        ChequeDto chequeDto = chequeService.findByChequeNumber(transactionDto.getSourceValue());
        chequeDto.setStatus(Status.USED.code());
        chequeService.save(chequeDto);
    }

    @Override
    TransactionType getType() {
        return TransactionType.CHEQUE_PAYMENT;
    }
}
