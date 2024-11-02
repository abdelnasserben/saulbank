package com.dabel.service.transaction;

import com.dabel.app.Fee;
import com.dabel.constant.BankFees;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.service.account.AccountService;
import com.dabel.service.customer.CustomerService;
import com.dabel.service.fee.FeeService;
import org.springframework.stereotype.Service;

@Service
public class Transfer extends AbstractTransfer {

    private final FeeService feeService;

    public Transfer(FeeService feeService, TransactionService transactionService, CustomerService customerService, AccountService accountService) {
        super(transactionService, accountService, customerService);
        this.feeService = feeService;
    }

    @Override
    public void approve(TransactionDto transactionDto) {

        super.approve(transactionDto);

        applyTransferFees(transactionDto);
    }

    private void applyTransferFees(TransactionDto transactionDto) {
        Fee fee = new Fee(transactionDto.getBranch(), BankFees.Basic.TRANSFER, "Transfer");
        feeService.apply(transactionDto.getInitiatorAccount(), LedgerType.TRANSFER, fee);
    }

    @Override
    TransactionType getType() {
        return TransactionType.TRANSFER;
    }
}
