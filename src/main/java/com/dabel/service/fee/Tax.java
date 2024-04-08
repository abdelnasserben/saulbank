package com.dabel.service.fee;

import com.dabel.app.Fee;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.LedgerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.transaction.TransactionService;

public abstract class Tax {

    private final AccountFacadeService accountFacadeService;
    private final TransactionService transactionService;

    public Tax(AccountFacadeService accountFacadeService, TransactionService transactionService) {
        this.accountFacadeService = accountFacadeService;
        this.transactionService = transactionService;
    }

    public void apply(AccountDto accountDto, Fee fee) {
        LedgerDto ledgerDto = accountFacadeService.findLedgerByBranchAndType(fee.branchDto(), getLedgerType().name());

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

        accountFacadeService.debit(accountDto, fee.value());
        accountFacadeService.credit(ledgerDto.getAccount(), fee.value());
    }

    abstract LedgerType getLedgerType();
}
