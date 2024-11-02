package com.dabel.service.transaction;

import com.dabel.app.Fee;
import com.dabel.constant.BankFees;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.AccountDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;
import com.dabel.service.fee.FeeService;
import org.springframework.stereotype.Service;

@Service
public class Withdraw extends OwnerBasedTransaction {

    private final FeeService feeService;

    public Withdraw(FeeService feeService, TransactionService transactionService, CustomerService customerService, AccountService accountService) {
        super(transactionService, accountService, customerService);
        this.feeService = feeService;
    }

    @Override
    public void init(TransactionDto transactionDto) {

        validateActiveAccount(transactionDto);
        validateCurrencyMatch(transactionDto);
        validateTransactionOwnership(transactionDto);

        //TODO: for withdraw, debit account is the initiator account of transaction so we interchange nothing, we set only the receiver
        AccountDto receiverAccount = this.accountService.findVaultByBranchAndCurrency(transactionDto.getBranch(), transactionDto.getCurrency());

        transactionDto.setReceiverAccount(receiverAccount);

        setInitiator(transactionDto);

        if(isInSufficientBalance(transactionDto)) {
            updateTransactionStatus(transactionDto, Status.FAILED.code(), "Insufficient balance", false);
            throw new BalanceInsufficientException();
        }

        transactionDto.setStatus(Status.PENDING.code());
        transactionService.save(transactionDto);
    }

    private boolean isInSufficientBalance(TransactionDto transactionDto) {
        return transactionDto.getInitiatorAccount().getBalance() < transactionDto.getAmount() + BankFees.Basic.WITHDRAW;
    }

    @Override
    public void approve(TransactionDto transactionDto) {

        if(isNotPendingStatus(transactionDto))
            return;

        adjustInitiatorAndBeneficiaryAccounts(transactionDto);

        applyWithdrawFees(transactionDto);

        updateTransactionStatus(transactionDto, Status.APPROVED.code(), "Approved", true);
    }

    private void applyWithdrawFees(TransactionDto transactionDto) {
        Fee fee = new Fee(transactionDto.getBranch(), BankFees.Basic.WITHDRAW, "Withdraw");
        feeService.apply(transactionDto.getInitiatorAccount(), LedgerType.WITHDRAW, fee);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.WITHDRAW;
    }
}
