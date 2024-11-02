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
public class SavingChequeRequest extends ChequeRequest {

    private static final int NUMBER_OF_CHEQUES = 25;

    public SavingChequeRequest(ChequeService chequeService, ChequeRequestService chequeRequestService, FeeService feeService) {
        super(chequeService, chequeRequestService, feeService);
    }

    @Override
    public void init(ChequeRequestDto chequeRequestDto) {

        AccountDto account = chequeRequestDto.getTrunk().getAccount();

        if (!isEligibleForSavingCheque(account, chequeRequestDto)) {
            throw new IllegalOperationException("Only active saving accounts are eligible for this operation.");
        }

        chequeRequestDto.setInitiatedBy(getCurrentUsername());
        processChequeRequestInit(chequeRequestDto, account);
    }

    @Override
    public void approve(ChequeRequestDto chequeRequestDto) {

        if (!Status.PENDING.code().equals(chequeRequestDto.getStatus()))
            return;  // Only pending requests can be approved

        approveChequeRequest(chequeRequestDto);
    }

    private boolean isEligibleForSavingCheque(AccountDto account, ChequeRequestDto chequeRequestDto) {
        return Helper.isSavingAccount(account)
                && Helper.isActiveStatedObject(account)
                && Helper.isActiveStatedObject(chequeRequestDto.getTrunk().getCustomer());
    }

    private void processChequeRequestInit(ChequeRequestDto chequeRequestDto, AccountDto account) {
        if (account.getBalance() < BankFees.Basic.SAVING_CHEQUE) {
            saveFailedRequest(chequeRequestDto);
            throw new BalanceInsufficientException(ACCOUNT_BALANCE_IS_INSUFFICIENT_FOR_CHEQUE_APPLICATION_FEES);
        }

        chequeRequestDto.setStatus(Status.PENDING.code());
        chequeRequestService.save(chequeRequestDto);
    }

    private void approveChequeRequest(ChequeRequestDto chequeRequestDto) {
        chequeRequestDto.setStatus(Status.APPROVED.code());
        chequeRequestDto.setUpdatedBy(getCurrentUsername());

        applyChequeRequestFee(chequeRequestDto, BankFees.Basic.SAVING_CHEQUE);
        chequeRequestService.save(chequeRequestDto);

        generateCheques(chequeRequestDto, NUMBER_OF_CHEQUES);
    }

    @Override
    AccountType getAccountType() {
        return AccountType.SAVING;
    }
}
