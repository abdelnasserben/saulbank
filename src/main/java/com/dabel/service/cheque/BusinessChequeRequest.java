package com.dabel.service.cheque;

import com.dabel.app.Fee;
import com.dabel.app.Helper;
import com.dabel.constant.AccountType;
import com.dabel.constant.BankFees;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.ChequeDto;
import com.dabel.dto.ChequeRequestDto;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.fee.FeeService;
import org.springframework.stereotype.Service;

@Service
public class BusinessChequeRequest extends ChequeRequest {


    public BusinessChequeRequest(ChequeService chequeService, ChequeRequestService chequeRequestService, FeeService feeService) {
        super(chequeService, chequeRequestService, feeService);
    }

    @Override
    public void init(ChequeRequestDto chequeRequestDto) {

        AccountDto accountDto = chequeRequestDto.getTrunk().getAccount();

        if(!Helper.isBusinessAccount(accountDto))
            throw new IllegalOperationException("Only business account is eligible for this operation");

        if(!Helper.isActiveStatedObject(accountDto) || !Helper.isActiveStatedObject(chequeRequestDto.getTrunk().getCustomer()))
            throw new IllegalOperationException("The account and its owner must be active for this operation");

        if(accountDto.getBalance() < BankFees.Basic.BUSINESS_CHEQUE) {
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

        if(!chequeRequestDto.getStatus().equals(Status.PENDING.code()))
            return;

        chequeRequestDto.setStatus(Status.APPROVED.code());
        //we'll make update by info later...

        //TODO: apply fees
        Fee fee = new Fee(chequeRequestDto.getBranch(), BankFees.Basic.BUSINESS_CHEQUE, "Cheque application request");
        feeService.apply(chequeRequestDto.getTrunk().getAccount(), LedgerType.CHEQUE_REQUEST, fee);

        chequeRequestService.save(chequeRequestDto);

        //TODO: generate 25 cheques for this trunk
        for (int i = 0; i < 50; i++) {
            ChequeDto chequeDto = ChequeDto.builder()
                    .trunk(chequeRequestDto.getTrunk())
                    .chequeNumber(Helper.generateChequeNumber())
                    .status(Status.ACTIVE.code())
                    .branch(chequeRequestDto.getBranch())
                    .build();

            chequeService.save(chequeDto);
        }
    }

    @Override
    AccountType getType() {
        return AccountType.BUSINESS;
    }
}
