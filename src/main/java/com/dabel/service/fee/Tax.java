package com.dabel.service.fee;

import com.dabel.app.Fee;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.LedgerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.service.account.AccountService;
import com.dabel.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Tax {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public Tax(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public void apply(AccountDto accountDto, Fee fee) {
        LedgerDto ledgerDto = accountService.findLedgerByBranchAndType(fee.branchDto(), getLedgerType().name());

        transactionService.save(
                TransactionDto.builder()
                        .transactionType(TransactionType.FEE.name())
                        .initiatorAccount(accountDto)
                        .receiverAccount(ledgerDto.getAccount())
                        .currency(Currency.KMF.name())
                        .amount(fee.value())
                        .sourceType(SourceType.ONLINE.name())
                        .sourceValue("System")
                        .reason(String.format("System charge @ %s fee.", fee.description()))
                        .status(Status.APPROVED.code())
                        .build());

        accountService.debit(accountDto, fee.value());
        accountService.credit(ledgerDto.getAccount(), fee.value());
    }

    abstract LedgerType getLedgerType();
}
