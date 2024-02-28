package com.dabel.service.fee;

import com.dabel.constant.LedgerType;
import com.dabel.service.account.AccountOperationService;
import com.dabel.service.account.AccountService;
import com.dabel.service.transaction.TransactionService;
import org.springframework.stereotype.Service;

@Service
public class WithdrawFee extends Tax {

    public WithdrawFee(AccountService accountService, AccountOperationService accountOperationService, TransactionService transactionService) {
        super(accountService, accountOperationService, transactionService);
    }

    @Override
    LedgerType getLedgerType() {
        return LedgerType.WITHDRAW;
    }
}
