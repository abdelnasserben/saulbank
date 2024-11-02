package com.dabel.service.transaction;

import com.dabel.constant.Status;
import com.dabel.constant.TransactionType;
import com.dabel.dto.ChequeDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.service.account.AccountService;
import com.dabel.service.cheque.ChequeService;
import com.dabel.service.customer.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class ChequePayment extends AbstractTransfer {

    private final ChequeService chequeService;

    public ChequePayment(TransactionService transactionService, CustomerService customerService, AccountService accountService, ChequeService chequeService) {
        super(transactionService, accountService, customerService);
        this.chequeService = chequeService;
    }

    @Override
    public void approve(TransactionDto transactionDto) {

        super.approve(transactionDto);

        saveUsedCheque(transactionDto.getSourceValue());
    }

    private void saveUsedCheque(String chequeNumber) {
        ChequeDto chequeDto = chequeService.findByChequeNumber(chequeNumber);
        chequeDto.setStatus(Status.USED.code());
        chequeService.save(chequeDto);
    }

    @Override
    TransactionType getType() {
        return TransactionType.CHEQUE_PAYMENT;
    }
}
