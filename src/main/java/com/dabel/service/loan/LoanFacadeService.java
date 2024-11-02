package com.dabel.service.loan;

import com.dabel.dto.LoanDto;
import com.dabel.dto.LoanRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanFacadeService {

    private final LoanService loanService;
    private final LoanRequestOperationService loanRequestOperationService;

    @Autowired
    public LoanFacadeService(LoanService loanService, LoanRequestOperationService loanRequestOperationService) {
        this.loanService = loanService;
        this.loanRequestOperationService = loanRequestOperationService;
    }

    public List<LoanDto> getAllLoans() {
        return this.loanService.findAll();
    }

    public LoanDto getLoanById(Long loanId) {
        return this.loanService.findById(loanId);
    }

    public List<LoanDto> getCustomerLoansByHisIdentityNumber(String customerIdentityNumber) {
        return this.loanService.findAllByCustomerIdentity(customerIdentityNumber);
    }

    public void repayLoan(Long loanId, double amount) {
        loanRequestOperationService.repay(loanId, amount);
    }

    /**
     * For loan requests
     */

    public List<LoanRequestDto> getAllLoanRequests() {
        return loanRequestOperationService.getLoanRequestService().findAll();
    }

    public void initLoanRequest(LoanRequestDto requestDto) {
        loanRequestOperationService.init(requestDto);
    }

    public LoanRequestDto getLoanRequestById(Long requestId) {
        return loanRequestOperationService.getLoanRequestService().findById(requestId);
    }

    public void approveLoanRequest(Long requestId) {
        LoanRequestDto loanRequestDto = loanRequestOperationService.getLoanRequestService().findById(requestId);
        loanRequestOperationService.approve(loanRequestDto);
    }

    public void rejectLoanRequest(Long requestId, String rejectReason) {
        LoanRequestDto loanRequestDto = loanRequestOperationService.getLoanRequestService().findById(requestId);
        loanRequestOperationService.reject(loanRequestDto, rejectReason);
    }
}
