package com.dabel.service.loan;

import com.dabel.dto.LoanDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanFacadeService {

    private final LoanOperationService loanOperationService;

    public LoanFacadeService(LoanOperationService loanOperationService) {
        this.loanOperationService = loanOperationService;
    }

    public void init(LoanDto loanDto) {
        loanOperationService.init(loanDto);
    }

    public void approve(Long operationId) {
        loanOperationService.approve(this.findById(operationId));
    }

    public void reject(Long operationId, String remarks) {
        loanOperationService.reject(this.findById(operationId), remarks);
    }

    public List<LoanDto> findAll() {
        return this.loanOperationService.getLoanService().findAll();
    }

    public LoanDto findById(Long loanId) {
        return this.loanOperationService.getLoanService().findById(loanId);
    }

    public List<LoanDto> findAllByCustomerIdentityNumber(String customerIdentityNumber) {
        return this.loanOperationService.getLoanService().findAllByCustomerIdentity(customerIdentityNumber);
    }
}
