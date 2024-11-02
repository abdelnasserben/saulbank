package com.dabel.service.transaction;

import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.AccountDto;
import com.dabel.dto.TransactionDto;
import com.dabel.service.account.AccountService;
import org.springframework.stereotype.Service;

@Service
public class Deposit extends Transaction {

    public Deposit(TransactionService transactionService, AccountService accountService) {
        super(transactionService, accountService);
    }

    @Override
    public void init(TransactionDto transactionDto) {

        validateActiveAccount(transactionDto);

        //TODO: for deposit, initiator account is the beneficiary account so we interchange initiator as the vault and beneficiary as receiver
        AccountDto initiatorAccount = accountService.findVaultByBranchAndCurrency(transactionDto.getBranch(), transactionDto.getCurrency());
        AccountDto receiverAccount = transactionDto.getInitiatorAccount();

        transactionDto.setInitiatorAccount(initiatorAccount);
        transactionDto.setReceiverAccount(receiverAccount);

        setInitiator(transactionDto);

        updateTransactionStatus(transactionDto, Status.PENDING.code(), null, false);
    }

    @Override
    public void approve(TransactionDto transactionDto) {

        if(isNotPendingStatus(transactionDto))
            return;

        adjustInitiatorAndBeneficiaryAccounts(transactionDto);

        updateTransactionStatus(transactionDto, Status.APPROVED.code(), "Approved", true);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.DEPOSIT;
    }
}
