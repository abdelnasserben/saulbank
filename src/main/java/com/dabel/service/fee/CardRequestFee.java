package com.dabel.service.fee;

import com.dabel.constant.LedgerType;
import com.dabel.service.account.AccountService;
import com.dabel.service.transaction.TransactionService;
import org.springframework.stereotype.Service;

@Service
public class CardRequestFee extends Tax {

    public CardRequestFee(AccountService accountService, TransactionService transactionService) {
        super(accountService, transactionService);
    }

    @Override
    LedgerType getLedgerType() {
        return LedgerType.CARD_REQUEST;
    }
}
