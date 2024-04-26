package com.dabel.service.cheque;

import com.dabel.constant.AccountType;
import com.dabel.constant.Status;
import com.dabel.dto.ChequeRequestDto;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.fee.FeeService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ChequeRequest implements EvaluableOperation<ChequeRequestDto> {

    protected ChequeService chequeService;
    protected ChequeRequestService chequeRequestService;
    protected FeeService feeService;

    @Autowired
    public ChequeRequest(ChequeService chequeService, ChequeRequestService chequeRequestService, FeeService feeService) {
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
