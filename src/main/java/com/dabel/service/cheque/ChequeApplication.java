package com.dabel.service.cheque;

import com.dabel.constant.AccountType;
import com.dabel.constant.Status;
import com.dabel.dto.ChequeRequestDto;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.fee.FeeService;

public abstract class ChequeApplication implements EvaluableOperation<ChequeRequestDto> {

    protected ChequeService chequeService;
    protected ChequeRequestService chequeRequestService;
    protected FeeService feeService;

    public ChequeApplication(ChequeService chequeService, ChequeRequestService chequeRequestService, FeeService feeService) {
        this.chequeService = chequeService;
        this.chequeRequestService = chequeRequestService;
        this.feeService = feeService;
    }

    @Override
    public void reject(ChequeRequestDto chequeRequestDto, String remarks) {
        chequeRequestDto.setStatus(Status.REJECTED.code());
        chequeRequestDto.setFailureReason(remarks);
        //we'll make update by info later...

        chequeRequestService.save(chequeRequestDto);
    }

    abstract AccountType getType();
}
