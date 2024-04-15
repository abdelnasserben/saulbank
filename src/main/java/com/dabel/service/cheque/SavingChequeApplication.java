package com.dabel.service.cheque;

import com.dabel.app.Helper;
import com.dabel.constant.AccountType;
import com.dabel.constant.BankFees;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.ChequeRequestDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.fee.FeeService;
import org.springframework.stereotype.Service;

@Service
public class SavingChequeApplication extends ChequeApplication {

    public SavingChequeApplication(ChequeRequestService chequeRequestService, FeeService feeService) {
        super(chequeRequestService, feeService);
    }

    @Override
    public void init(ChequeRequestDto chequeRequestDto) {

        AccountDto accountDto = chequeRequestDto.getTrunk().getAccount();

        if(!Helper.isSavingAccount(accountDto))
            throw new IllegalOperationException("Only saving account is eligible for this operation");

        if(Helper.isInactiveAccount(accountDto) || Helper.isInactiveCustomer(chequeRequestDto.getTrunk().getCustomer()))
            throw new IllegalOperationException("The account and its owner must be active for this operation");

        if(accountDto.getBalance() < BankFees.Basic.SAVING_CHEQUE) {
            chequeRequestDto.setStatus(Status.FAILED.code());
            chequeRequestDto.setFailureReason("Account balance is insufficient for cheque request fees");
            chequeRequestService.save(chequeRequestDto);
            throw new BalanceInsufficientException("Account balance is insufficient for application fees");
        }

        chequeRequestDto.setStatus(Status.PENDING.code());
        chequeRequestService.save(chequeRequestDto);
    }

    @Override
    public void approve(ChequeRequestDto chequeRequestDto) {

    }

    @Override
    AccountType getType() {
        return AccountType.SAVING;
    }
}
