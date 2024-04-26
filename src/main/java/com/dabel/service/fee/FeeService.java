package com.dabel.service.fee;

import com.dabel.app.Fee;
import com.dabel.constant.LedgerType;
import com.dabel.dto.AccountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeeService {

    private final FeeContext feeContext;

    @Autowired
    public FeeService(FeeContext feeContext) {
        this.feeContext = feeContext;
    }

    public void apply(AccountDto accountDto, LedgerType ledgerType, Fee fee) {
        feeContext.setContext(ledgerType.name()).apply(accountDto, fee);
    }
}
