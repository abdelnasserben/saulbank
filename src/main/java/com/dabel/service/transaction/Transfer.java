package com.dabel.service.transaction;

import com.dabel.app.CurrencyExchanger;
import com.dabel.app.Fee;
import com.dabel.app.Helper;
import com.dabel.constant.BankFees;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.account.AccountOperationService;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;
import com.dabel.service.fee.FeeService;
import org.springframework.stereotype.Service;

@Service
public class Transfer extends Transaction{

    private final FeeService feeService;
    private final CustomerService customerService;
    private final AccountFacadeService accountFacadeService;

    public Transfer(FeeService feeService, TransactionService transactionService, AccountService accountService, AccountOperationService accountOperationService, CustomerService customerService, AccountFacadeService accountFacadeService) {
        super(transactionService, accountService, accountOperationService);
        this.feeService = feeService;
        this.customerService = customerService;
        this.accountFacadeService = accountFacadeService;
    }

    @Override
    public void init(TransactionDto transactionDto) {

        if(Helper.isInactiveAccount(transactionDto.getInitiatorAccount()))
            throw new IllegalOperationException("Initiator account must be active");

        if(!transactionDto.getInitiatorAccount().getCurrency().equals(transactionDto.getCurrency()))
            throw new IllegalOperationException(String.format("The transaction currency must match that of the account (%s)", transactionDto.getInitiatorAccount().getCurrency()));

        //TODO: check if initiator customer is affiliate on the account
        CustomerDto customerDto = customerService.findByIdentity(transactionDto.getCustomerIdentity());
        accountFacadeService.findTrunkByCustomerAndAccountNumber(customerDto, transactionDto.getInitiatorAccount().getAccountNumber());


        if(transactionDto.getInitiatorAccount().getBalance() < transactionDto.getAmount() + BankFees.Basic.TRANSFER) {

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
        Fee fee = new Fee(transactionDto.getBranch(), BankFees.Basic.TRANSFER, "Transfer");
        feeService.apply(transactionDto.getInitiatorAccount(), LedgerType.TRANSFER, fee);

        //TODO: update transaction info and save it
        transactionDto.setStatus(Status.APPROVED.code());
        transactionDto.setFailureReason("Approved");
        //we'll set updatedBy later...

        transactionService.save(transactionDto);
    }

    @Override
    TransactionType getType() {
        return TransactionType.TRANSFER;
    }
}
