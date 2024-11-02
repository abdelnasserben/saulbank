package com.dabel.service.cheque;

import com.dabel.app.Fee;
import com.dabel.app.Helper;
import com.dabel.constant.AccountType;
import com.dabel.constant.LedgerType;
import com.dabel.constant.Status;
import com.dabel.dto.ChequeDto;
import com.dabel.dto.ChequeRequestDto;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.fee.FeeService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ChequeRequest implements EvaluableOperation<ChequeRequestDto> {

    protected static final String ACCOUNT_BALANCE_IS_INSUFFICIENT_FOR_CHEQUE_APPLICATION_FEES = "Account balance is insufficient for cheque application fees.";

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
        chequeRequestDto.setUpdatedBy(getCurrentUsername());

        chequeRequestService.save(chequeRequestDto);
    }

    abstract AccountType getAccountType();

    protected String getCurrentUsername() {
        return Helper.getAuthenticated().getName();
    }

    protected void generateCheques(ChequeRequestDto chequeRequestDto, int quantity) {
        String baseChequeNumber = Helper.generateChequeNumber();
        for (int i = 1; i <= quantity; i++) {
            String chequeNumber = formatChequeNumber(baseChequeNumber, i);

            ChequeDto chequeDto = ChequeDto.builder()
                    .trunk(chequeRequestDto.getTrunk())
                    .serial(chequeRequestDto)
                    .chequeNumber(chequeNumber)
                    .status(Status.ACTIVE.code())
                    .branch(chequeRequestDto.getBranch())
                    .initiatedBy(getCurrentUsername())
                    .build();

            chequeService.save(chequeDto);
        }
    }

    protected void applyChequeRequestFee(ChequeRequestDto chequeRequestDto, double amount) {
        Fee fee = new Fee(
                chequeRequestDto.getBranch(),
                amount,
                "Cheque application"
        );
        feeService.apply(chequeRequestDto.getTrunk().getAccount(), LedgerType.CHEQUE_REQUEST, fee);
    }

    protected void saveFailedRequest(ChequeRequestDto chequeRequestDto) {
        chequeRequestDto.setStatus(Status.FAILED.code());
        chequeRequestDto.setFailureReason(ACCOUNT_BALANCE_IS_INSUFFICIENT_FOR_CHEQUE_APPLICATION_FEES);
        chequeRequestService.save(chequeRequestDto);
    }

    private String formatChequeNumber(String baseNumber, int index) {
        return baseNumber + (index <= 9 ? "0" + index : index);
    }
}
