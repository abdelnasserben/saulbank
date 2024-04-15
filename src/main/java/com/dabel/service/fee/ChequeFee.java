package com.dabel.service.fee;

import com.dabel.constant.LedgerType;
import com.dabel.service.account.AccountService;
import com.dabel.service.transaction.TransactionService;
import org.springframework.stereotype.Service;

@Service
public class ChequeFee extends Tax {

    public ChequeFee(AccountService accountService, TransactionService transactionService) {
        super(accountService, transactionService);
    }

    @Override
    LedgerType getLedgerType() {
        return LedgerType.CHEQUE_REQUEST;
    }
}
