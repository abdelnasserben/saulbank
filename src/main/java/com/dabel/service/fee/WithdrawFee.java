package com.dabel.service.fee;

import com.dabel.constant.LedgerType;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.transaction.TransactionService;
import org.springframework.stereotype.Service;

@Service
public class WithdrawFee extends Tax {

    public WithdrawFee(AccountFacadeService accountFacadeService, TransactionService transactionService) {
        super(accountFacadeService, transactionService);
    }

    @Override
    LedgerType getLedgerType() {
        return LedgerType.WITHDRAW;
    }
}
