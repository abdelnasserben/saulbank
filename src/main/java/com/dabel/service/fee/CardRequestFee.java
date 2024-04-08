package com.dabel.service.fee;

import com.dabel.constant.LedgerType;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.transaction.TransactionService;
import org.springframework.stereotype.Service;

@Service
public class CardRequestFee extends Tax {

    public CardRequestFee(AccountFacadeService accountFacadeService, TransactionService transactionService) {
        super(accountFacadeService, transactionService);
    }

    @Override
    LedgerType getLedgerType() {
        return LedgerType.CARD_REQUEST;
    }
}
